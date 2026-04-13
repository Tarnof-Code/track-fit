package com.sport.gymtracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Progression des séries pendant une séance en cours (bitmask). */
val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE exercise_entries ADD COLUMN completedSetsMask INTEGER NOT NULL DEFAULT 0",
        )
    }
}
