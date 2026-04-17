package com.sport.gymtracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Deux exercices de séance peuvent partager un [ExerciseEntryEntity.comboGroupId] (combinaison / enchaînement). */
val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN comboGroupId INTEGER")
    }
}
