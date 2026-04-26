package com.notesync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.notesync.navigation.NavGraph
import com.notesync.ui.theme.NoteSyncTheme
import com.notesync.util.TokenManager
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val tokenManager: TokenManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteSyncTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    tokenManager = tokenManager
                )
            }
        }
    }
}