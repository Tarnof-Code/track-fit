package com.sport.gymtracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN loadKg REAL")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `workout_templates` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT,
                `createdAtMillis` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `template_exercises` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `templateId` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `sets` INTEGER NOT NULL,
                `repsPerSet` INTEGER,
                `durationSecondsPerSet` INTEGER,
                `loadKg` REAL,
                `difficulty` TEXT NOT NULL,
                `level` TEXT NOT NULL,
                `equipment` TEXT NOT NULL,
                `muscleGroupsCsv` TEXT NOT NULL,
                `restBetweenSetsSeconds` INTEGER NOT NULL,
                `orderIndex` INTEGER NOT NULL,
                FOREIGN KEY(`templateId`) REFERENCES `workout_templates`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercises_templateId` ON `template_exercises` (`templateId`)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE workout_sessions ADD COLUMN sourceTemplateId INTEGER")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `template_exercises_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `templateId` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `sets` INTEGER NOT NULL,
                `repsPerSet` INTEGER,
                `durationSecondsPerSet` INTEGER,
                `loadKg` REAL,
                `equipment` TEXT NOT NULL,
                `muscleGroupsCsv` TEXT NOT NULL,
                `restBetweenSetsSeconds` INTEGER NOT NULL,
                `orderIndex` INTEGER NOT NULL,
                FOREIGN KEY(`templateId`) REFERENCES `workout_templates`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `template_exercises_new` (
                `id`, `templateId`, `name`, `sets`, `repsPerSet`, `durationSecondsPerSet`,
                `loadKg`, `equipment`, `muscleGroupsCsv`, `restBetweenSetsSeconds`, `orderIndex`
            )
            SELECT `id`, `templateId`, `name`, `sets`, `repsPerSet`, `durationSecondsPerSet`,
                `loadKg`, `equipment`, `muscleGroupsCsv`, `restBetweenSetsSeconds`, `orderIndex`
            FROM `template_exercises`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `template_exercises`")
        db.execSQL("ALTER TABLE `template_exercises_new` RENAME TO `template_exercises`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercises_templateId` ON `template_exercises` (`templateId`)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `workout_sessions_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `startTimeMillis` INTEGER NOT NULL,
                `endTimeMillis` INTEGER,
                `title` TEXT NOT NULL,
                `estimatedCalories` REAL,
                `sourceTemplateId` INTEGER
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `workout_sessions_new` (
                `id`, `startTimeMillis`, `endTimeMillis`, `title`, `estimatedCalories`, `sourceTemplateId`
            )
            SELECT `id`, `startTimeMillis`, `endTimeMillis`, `title`, `estimatedCalories`, `sourceTemplateId`
            FROM `workout_sessions`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `workout_sessions`")
        db.execSQL("ALTER TABLE `workout_sessions_new` RENAME TO `workout_sessions`")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN durationMinutesPerSet INTEGER")
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN machineLevel INTEGER")
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN rowResistance TEXT")
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN workMode TEXT NOT NULL DEFAULT 'REPS_LOAD'")
        db.execSQL(
            "UPDATE exercise_entries SET workMode = 'TIME_SECONDS' WHERE durationSecondsPerSet IS NOT NULL",
        )

        db.execSQL("ALTER TABLE template_exercises ADD COLUMN durationMinutesPerSet INTEGER")
        db.execSQL("ALTER TABLE template_exercises ADD COLUMN machineLevel INTEGER")
        db.execSQL("ALTER TABLE template_exercises ADD COLUMN rowResistance TEXT")
        db.execSQL("ALTER TABLE template_exercises ADD COLUMN workMode TEXT NOT NULL DEFAULT 'REPS_LOAD'")
        db.execSQL(
            "UPDATE template_exercises SET workMode = 'TIME_SECONDS' WHERE durationSecondsPerSet IS NOT NULL",
        )
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "UPDATE exercise_entries SET rowResistance = CAST(machineLevel AS TEXT) " +
                "WHERE workMode = 'STAIR_LEVEL' AND machineLevel IS NOT NULL " +
                "AND (rowResistance IS NULL OR rowResistance = '')",
        )
        db.execSQL(
            "UPDATE exercise_entries SET workMode = 'DURATION_AND_LEVEL' " +
                "WHERE workMode IN ('STAIR_LEVEL', 'ROW_RESISTANCE')",
        )
        db.execSQL("UPDATE exercise_entries SET machineLevel = NULL WHERE workMode = 'DURATION_AND_LEVEL'")
        db.execSQL(
            "UPDATE template_exercises SET rowResistance = CAST(machineLevel AS TEXT) " +
                "WHERE workMode = 'STAIR_LEVEL' AND machineLevel IS NOT NULL " +
                "AND (rowResistance IS NULL OR rowResistance = '')",
        )
        db.execSQL(
            "UPDATE template_exercises SET workMode = 'DURATION_AND_LEVEL' " +
                "WHERE workMode IN ('STAIR_LEVEL', 'ROW_RESISTANCE')",
        )
        db.execSQL("UPDATE template_exercises SET machineLevel = NULL WHERE workMode = 'DURATION_AND_LEVEL'")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN loadSpec TEXT")
        db.execSQL("ALTER TABLE template_exercises ADD COLUMN loadSpec TEXT")
        db.execSQL(
            "UPDATE exercise_entries SET loadSpec = TRIM(CAST(loadKg AS TEXT)) " +
                "WHERE loadKg IS NOT NULL AND (loadSpec IS NULL OR loadSpec = '')",
        )
        db.execSQL(
            "UPDATE template_exercises SET loadSpec = TRIM(CAST(loadKg AS TEXT)) " +
                "WHERE loadKg IS NOT NULL AND (loadSpec IS NULL OR loadSpec = '')",
        )
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE exercise_entries ADD COLUMN doneInSession INTEGER NOT NULL DEFAULT 0",
        )
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `workout_sessions_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `startTimeMillis` INTEGER NOT NULL,
                `endTimeMillis` INTEGER,
                `title` TEXT NOT NULL,
                `sourceTemplateId` INTEGER
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `workout_sessions_new` (
                `id`, `startTimeMillis`, `endTimeMillis`, `title`, `sourceTemplateId`
            )
            SELECT `id`, `startTimeMillis`, `endTimeMillis`, `title`, `sourceTemplateId`
            FROM `workout_sessions`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `workout_sessions`")
        db.execSQL("ALTER TABLE `workout_sessions_new` RENAME TO `workout_sessions`")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `user_profile`")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `exercise_blueprints` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `sets` INTEGER NOT NULL,
                `repsPerSet` INTEGER,
                `durationSecondsPerSet` INTEGER,
                `durationMinutesPerSet` INTEGER,
                `loadSpec` TEXT,
                `loadKg` REAL,
                `machineLevel` INTEGER,
                `rowResistance` TEXT,
                `workMode` TEXT NOT NULL,
                `equipment` TEXT NOT NULL,
                `muscleGroupsCsv` TEXT NOT NULL,
                `restBetweenSetsSeconds` INTEGER NOT NULL,
                `createdAtMillis` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }
}

/** Import des exercices existants dans la bibliothèque (sans doublons de prescription). */
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO exercise_blueprints (
                name, sets, repsPerSet, durationSecondsPerSet, durationMinutesPerSet,
                loadSpec, loadKg, machineLevel, rowResistance, workMode, equipment,
                muscleGroupsCsv, restBetweenSetsSeconds, createdAtMillis
            )
            SELECT
                te.name, te.sets, te.repsPerSet, te.durationSecondsPerSet, te.durationMinutesPerSet,
                te.loadSpec, te.loadKg, te.machineLevel, te.rowResistance, te.workMode, te.equipment,
                te.muscleGroupsCsv, te.restBetweenSetsSeconds,
                COALESCE(
                    (SELECT t.createdAtMillis FROM workout_templates t WHERE t.id = te.templateId),
                    te.id
                )
            FROM template_exercises te
            WHERE NOT EXISTS (
                SELECT 1 FROM exercise_blueprints b
                WHERE b.name = te.name
                  AND b.sets = te.sets
                  AND b.workMode = te.workMode
                  AND b.repsPerSet IS te.repsPerSet
                  AND b.durationSecondsPerSet IS te.durationSecondsPerSet
                  AND b.durationMinutesPerSet IS te.durationMinutesPerSet
                  AND b.loadKg IS te.loadKg
                  AND b.machineLevel IS te.machineLevel
                  AND b.restBetweenSetsSeconds = te.restBetweenSetsSeconds
                  AND IFNULL(b.loadSpec, '') = IFNULL(te.loadSpec, '')
                  AND IFNULL(b.rowResistance, '') = IFNULL(te.rowResistance, '')
                  AND b.equipment = te.equipment
                  AND b.muscleGroupsCsv = te.muscleGroupsCsv
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO exercise_blueprints (
                name, sets, repsPerSet, durationSecondsPerSet, durationMinutesPerSet,
                loadSpec, loadKg, machineLevel, rowResistance, workMode, equipment,
                muscleGroupsCsv, restBetweenSetsSeconds, createdAtMillis
            )
            SELECT
                e.name, e.sets, e.repsPerSet, e.durationSecondsPerSet, e.durationMinutesPerSet,
                e.loadSpec, e.loadKg, e.machineLevel, e.rowResistance, e.workMode, e.equipment,
                e.muscleGroupsCsv, e.restBetweenSetsSeconds,
                s.startTimeMillis
            FROM exercise_entries e
            INNER JOIN workout_sessions s ON s.id = e.sessionId
            WHERE NOT EXISTS (
                SELECT 1 FROM exercise_blueprints b
                WHERE b.name = e.name
                  AND b.sets = e.sets
                  AND b.workMode = e.workMode
                  AND b.repsPerSet IS e.repsPerSet
                  AND b.durationSecondsPerSet IS e.durationSecondsPerSet
                  AND b.durationMinutesPerSet IS e.durationMinutesPerSet
                  AND b.loadKg IS e.loadKg
                  AND b.machineLevel IS e.machineLevel
                  AND b.restBetweenSetsSeconds = e.restBetweenSetsSeconds
                  AND IFNULL(b.loadSpec, '') = IFNULL(e.loadSpec, '')
                  AND IFNULL(b.rowResistance, '') = IFNULL(e.rowResistance, '')
                  AND b.equipment = e.equipment
                  AND b.muscleGroupsCsv = e.muscleGroupsCsv
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE template_exercises ADD COLUMN blueprintId INTEGER")
        db.execSQL("ALTER TABLE exercise_entries ADD COLUMN blueprintId INTEGER")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_template_exercises_blueprintId` ON `template_exercises` (`blueprintId`)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_exercise_entries_blueprintId` ON `exercise_entries` (`blueprintId`)",
        )
        db.execSQL(
            """
            UPDATE template_exercises SET blueprintId = (
                SELECT b.id FROM exercise_blueprints b
                WHERE b.name = template_exercises.name
                  AND b.sets = template_exercises.sets
                  AND b.workMode = template_exercises.workMode
                  AND b.repsPerSet IS template_exercises.repsPerSet
                  AND b.durationSecondsPerSet IS template_exercises.durationSecondsPerSet
                  AND b.durationMinutesPerSet IS template_exercises.durationMinutesPerSet
                  AND b.loadKg IS template_exercises.loadKg
                  AND b.machineLevel IS template_exercises.machineLevel
                  AND b.restBetweenSetsSeconds = template_exercises.restBetweenSetsSeconds
                  AND IFNULL(b.loadSpec, '') = IFNULL(template_exercises.loadSpec, '')
                  AND IFNULL(b.rowResistance, '') = IFNULL(template_exercises.rowResistance, '')
                  AND b.equipment = template_exercises.equipment
                  AND b.muscleGroupsCsv = template_exercises.muscleGroupsCsv
                ORDER BY b.id ASC
                LIMIT 1
            )
            WHERE blueprintId IS NULL
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE exercise_entries SET blueprintId = (
                SELECT b.id FROM exercise_blueprints b
                WHERE b.name = exercise_entries.name
                  AND b.sets = exercise_entries.sets
                  AND b.workMode = exercise_entries.workMode
                  AND b.repsPerSet IS exercise_entries.repsPerSet
                  AND b.durationSecondsPerSet IS exercise_entries.durationSecondsPerSet
                  AND b.durationMinutesPerSet IS exercise_entries.durationMinutesPerSet
                  AND b.loadKg IS exercise_entries.loadKg
                  AND b.machineLevel IS exercise_entries.machineLevel
                  AND b.restBetweenSetsSeconds = exercise_entries.restBetweenSetsSeconds
                  AND IFNULL(b.loadSpec, '') = IFNULL(exercise_entries.loadSpec, '')
                  AND IFNULL(b.rowResistance, '') = IFNULL(exercise_entries.rowResistance, '')
                  AND b.equipment = exercise_entries.equipment
                  AND b.muscleGroupsCsv = exercise_entries.muscleGroupsCsv
                ORDER BY b.id ASC
                LIMIT 1
            )
            WHERE blueprintId IS NULL
            """.trimIndent(),
        )
    }
}
