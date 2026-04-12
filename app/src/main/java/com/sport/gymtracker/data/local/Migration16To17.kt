package com.sport.gymtracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Notes libres sur la fiche exercice (bibliothèque). */
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE exercise_blueprints ADD COLUMN notes TEXT NOT NULL DEFAULT ''",
        )
    }
}
