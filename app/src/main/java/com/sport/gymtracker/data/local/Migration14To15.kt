package com.sport.gymtracker.data.local

import android.database.Cursor
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteStatement

/**
 * [template_exercises] et [exercise_entries] ne stockent plus la prescription :
 * uniquement une référence [exerciseId] vers [exercise_blueprints].
 */
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        migrateTemplateExercises(db)
        migrateExerciseEntries(db)
    }
}

private fun insertBlueprintFromLegacyExerciseRow(db: SupportSQLiteDatabase, c: Cursor): Long {
    val stmt = db.compileStatement(
        """
        INSERT INTO exercise_blueprints (
            name, sets, repsPerSet, durationSecondsPerSet, durationMinutesPerSet,
            loadSpec, loadKg, machineLevel, rowResistance, workMode, equipment,
            muscleGroupsCsv, restBetweenSetsSeconds, createdAtMillis
        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """.trimIndent(),
    )
    try {
        var i = 1
        stmt.bindString(i++, c.getString(c.getColumnIndexOrThrow("name")))
        stmt.bindLong(i++, c.getLong(c.getColumnIndexOrThrow("sets")))
        bindOptLong(stmt, i++, c, "repsPerSet")
        bindOptLong(stmt, i++, c, "durationSecondsPerSet")
        bindOptLong(stmt, i++, c, "durationMinutesPerSet")
        bindOptString(stmt, i++, c, "loadSpec")
        bindOptDouble(stmt, i++, c, "loadKg")
        bindOptLong(stmt, i++, c, "machineLevel")
        bindOptString(stmt, i++, c, "rowResistance")
        stmt.bindString(i++, c.getString(c.getColumnIndexOrThrow("workMode")))
        stmt.bindString(i++, c.getString(c.getColumnIndexOrThrow("equipment")))
        stmt.bindString(i++, c.getString(c.getColumnIndexOrThrow("muscleGroupsCsv")))
        stmt.bindLong(i++, c.getLong(c.getColumnIndexOrThrow("restBetweenSetsSeconds")))
        stmt.bindLong(i, System.currentTimeMillis())
        return stmt.executeInsert()
    } finally {
        stmt.close()
    }
}

private fun bindOptLong(stmt: SupportSQLiteStatement, index: Int, c: Cursor, col: String) {
    val idx = c.getColumnIndex(col)
    if (idx < 0 || c.isNull(idx)) stmt.bindNull(index) else stmt.bindLong(index, c.getLong(idx))
}

private fun bindOptString(stmt: SupportSQLiteStatement, index: Int, c: Cursor, col: String) {
    val idx = c.getColumnIndex(col)
    if (idx < 0 || c.isNull(idx)) stmt.bindNull(index) else stmt.bindString(index, c.getString(idx))
}

private fun bindOptDouble(stmt: SupportSQLiteStatement, index: Int, c: Cursor, col: String) {
    val idx = c.getColumnIndex(col)
    if (idx < 0 || c.isNull(idx)) stmt.bindNull(index) else stmt.bindDouble(index, c.getDouble(idx))
}

private fun migrateTemplateExercises(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `template_exercises_new` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `templateId` INTEGER NOT NULL,
            `orderIndex` INTEGER NOT NULL,
            `exerciseId` INTEGER NOT NULL,
            FOREIGN KEY(`templateId`) REFERENCES `workout_templates`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent(),
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercises_new_templateId` ON `template_exercises_new` (`templateId`)")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercises_new_exerciseId` ON `template_exercises_new` (`exerciseId`)")

    db.query("SELECT * FROM `template_exercises`").use { c ->
        while (c.moveToNext()) {
            val rowId = c.getLong(c.getColumnIndexOrThrow("id"))
            val templateId = c.getLong(c.getColumnIndexOrThrow("templateId"))
            val orderIndex = c.getLong(c.getColumnIndexOrThrow("orderIndex"))
            val bpCol = c.getColumnIndex("blueprintId")
            val exerciseId = if (bpCol >= 0 && !c.isNull(bpCol)) c.getLong(bpCol) else insertBlueprintFromLegacyExerciseRow(db, c)
            db.execSQL(
                "INSERT INTO `template_exercises_new` (`id`,`templateId`,`orderIndex`,`exerciseId`) VALUES (?,?,?,?)",
                arrayOf<Any?>(rowId, templateId, orderIndex, exerciseId),
            )
        }
    }
    db.execSQL("DROP TABLE `template_exercises`")
    db.execSQL("ALTER TABLE `template_exercises_new` RENAME TO `template_exercises`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercises_templateId` ON `template_exercises` (`templateId`)")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercises_exerciseId` ON `template_exercises` (`exerciseId`)")
}

private fun migrateExerciseEntries(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `exercise_entries_new` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `sessionId` INTEGER NOT NULL,
            `orderIndex` INTEGER NOT NULL,
            `exerciseId` INTEGER NOT NULL,
            `difficulty` TEXT NOT NULL,
            `level` TEXT NOT NULL,
            `doneInSession` INTEGER NOT NULL,
            FOREIGN KEY(`sessionId`) REFERENCES `workout_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent(),
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_entries_new_sessionId` ON `exercise_entries_new` (`sessionId`)")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_entries_new_exerciseId` ON `exercise_entries_new` (`exerciseId`)")

    db.query("SELECT * FROM `exercise_entries`").use { c ->
        while (c.moveToNext()) {
            val rowId = c.getLong(c.getColumnIndexOrThrow("id"))
            val sessionId = c.getLong(c.getColumnIndexOrThrow("sessionId"))
            val orderIndex = c.getLong(c.getColumnIndexOrThrow("orderIndex"))
            val difficulty = c.getString(c.getColumnIndexOrThrow("difficulty"))
            val level = c.getString(c.getColumnIndexOrThrow("level"))
            val doneInSession = c.getLong(c.getColumnIndexOrThrow("doneInSession"))
            val bpCol = c.getColumnIndex("blueprintId")
            val exerciseId = if (bpCol >= 0 && !c.isNull(bpCol)) c.getLong(bpCol) else insertBlueprintFromLegacyExerciseRow(db, c)
            db.execSQL(
                """
                INSERT INTO `exercise_entries_new` (`id`,`sessionId`,`orderIndex`,`exerciseId`,`difficulty`,`level`,`doneInSession`)
                VALUES (?,?,?,?,?,?,?)
                """.trimIndent(),
                arrayOf<Any?>(rowId, sessionId, orderIndex, exerciseId, difficulty, level, doneInSession),
            )
        }
    }
    db.execSQL("DROP TABLE `exercise_entries`")
    db.execSQL("ALTER TABLE `exercise_entries_new` RENAME TO `exercise_entries`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_entries_sessionId` ON `exercise_entries` (`sessionId`)")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_entries_exerciseId` ON `exercise_entries` (`exerciseId`)")
}
