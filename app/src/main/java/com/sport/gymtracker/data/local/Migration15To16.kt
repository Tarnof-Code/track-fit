package com.sport.gymtracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Instantanés de prescription à la fin des séances (exercices validés), pour graphiques d’évolution. */
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN perfCapturedAtMillis INTEGER")
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN perfWorkMode TEXT")
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN perfSets INTEGER")
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN perfRepsPerSet INTEGER")
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN perfDurationSecondsPerSet INTEGER")
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN perfDurationMinutesPerSet INTEGER")
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN perfLoadKg REAL")
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN perfLoadSpec TEXT")
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN perfRowResistance TEXT")
    }
}
