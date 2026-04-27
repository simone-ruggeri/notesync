package com.notesync.data.repository

import android.content.Context
import android.util.Log
import com.notesync.data.local.NoteDao
import com.notesync.data.local.NoteEntity
import com.notesync.data.local.SyncStatus
import com.notesync.data.remote.ApiService
import com.notesync.data.remote.dto.CreateNoteRequest
import com.notesync.domain.model.Note
import com.notesync.domain.model.toDomain
import com.notesync.util.NetworkUtils
import com.notesync.util.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID

private const val TAG = "NoteRepository"

class NoteRepository(
    private val noteDao: NoteDao,
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val context: Context
) {
    val notes: Flow<List<Note>> = tokenManager.userIdFlow
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else noteDao.getAllNotes(userId).map { it.map(NoteEntity::toDomain) }
        }

    suspend fun getNoteById(id: String): Note? = noteDao.getNoteById(id)?.toDomain()

    suspend fun createNote(title: String, content: String): NoteEntity {
        val localId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val currentUserId = tokenManager.getUserId() ?: ""
        val entity = NoteEntity(
            id = localId, serverId = null,
            userId = currentUserId,
            title = title, content = content,
            createdAt = now, updatedAt = now,
            syncStatus = SyncStatus.PENDING_CREATE.name
        )
        noteDao.insertNote(entity)
        if (NetworkUtils.isOnline(context)) syncCreate(entity)
        return entity
    }

    suspend fun updateNote(id: String, title: String, content: String) {
        val existing = noteDao.getNoteById(id) ?: return
        val updated = existing.copy(
            title = title, content = content,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_UPDATE.name
        )
        noteDao.updateNote(updated)
        if (NetworkUtils.isOnline(context)) syncUpdate(updated)
    }

    suspend fun deleteNote(id: String) {
        val existing = noteDao.getNoteById(id) ?: return
        if (NetworkUtils.isOnline(context) && existing.serverId != null) {
            val deleted = syncDelete(existing)
            if (deleted) noteDao.deleteNote(existing)
            else noteDao.updateNote(existing.copy(syncStatus = SyncStatus.PENDING_DELETE.name))
        } else if (existing.serverId == null) {
            noteDao.deleteNote(existing)
        } else {
            noteDao.updateNote(existing.copy(syncStatus = SyncStatus.PENDING_DELETE.name))
        }
    }

    suspend fun syncPending() {
        if (!NetworkUtils.isOnline(context)) return
        val currentUserId = tokenManager.getUserId() ?: return
        noteDao.getPendingSync(currentUserId).forEach { entity ->
            when (entity.syncStatus) {
                SyncStatus.PENDING_CREATE.name -> syncCreate(entity)
                SyncStatus.PENDING_UPDATE.name -> syncUpdate(entity)
                SyncStatus.PENDING_DELETE.name -> {
                    if (syncDelete(entity)) noteDao.deleteNote(entity)
                }
            }
        }
    }

    suspend fun refreshFromServer() {
        if (!NetworkUtils.isOnline(context)) return
        val currentUserId = tokenManager.getUserId() ?: return
        val response = apiService.getNotes()
        if (response.isSuccessful) {
            val serverNotes = response.body() ?: return
            val entities = serverNotes.map { dto ->
                val existing = noteDao.getNoteByServerId(dto.id)
                NoteEntity(
                    id = existing?.id ?: dto.id,
                    serverId = dto.id,
                    userId = currentUserId,
                    title = dto.title, content = dto.content,
                    createdAt = dto.createdAt, updatedAt = dto.updatedAt,
                    syncStatus = SyncStatus.SYNCED.name
                )
            }
            noteDao.insertAll(entities)
        } else {
            throw IOException("Errore server: ${response.code()}")
        }
    }

    suspend fun logout() {
        val userId = tokenManager.getUserId()
        if (userId != null) noteDao.deleteSyncedForUser(userId)
        tokenManager.clearToken()
    }

    private suspend fun syncCreate(entity: NoteEntity) {
        try {
            val response = apiService.createNote(CreateNoteRequest(entity.title, entity.content))
            if (response.isSuccessful) {
                noteDao.updateNote(
                    entity.copy(
                        serverId = response.body()!!.id,
                        syncStatus = SyncStatus.SYNCED.name
                    )
                )
            } else {
                Log.w(TAG, "syncCreate fallita per ${entity.id}: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "syncCreate eccezione per ${entity.id}: ${e.message}")
        }
    }

    private suspend fun syncUpdate(entity: NoteEntity) {
        val serverId = entity.serverId ?: return
        try {
            val response = apiService.updateNote(serverId, CreateNoteRequest(entity.title, entity.content))
            if (response.isSuccessful) noteDao.updateNote(
                entity.copy(syncStatus = SyncStatus.SYNCED.name)
            )
        } catch (e: Exception) {
            Log.w(TAG, "syncUpdate fallita per ${entity.id}: ${e.message}")
        }
    }

    private suspend fun syncDelete(entity: NoteEntity): Boolean {
        val serverId = entity.serverId ?: return true
        return try {
            apiService.deleteNote(serverId).isSuccessful
        } catch (e: Exception) {
            Log.w(TAG, "syncDelete fallita per ${entity.id}: ${e.message}")
            false
        }
    }
}
