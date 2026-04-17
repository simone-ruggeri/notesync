package com.notesync.testutils

import org.jetbrains.exposed.sql.Database

object TestDatabase {
    fun connect() {
        Database.connect(
            url = "jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "test",
            password = ""
        )
    }
}