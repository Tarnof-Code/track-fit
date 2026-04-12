package com.sport.gymtracker

import android.app.Application
import com.sport.gymtracker.data.GymRepository
import com.sport.gymtracker.data.local.AppDatabase

class GymTrackerApp : Application() {
    lateinit var repository: GymRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.build(this)
        repository = GymRepository(db)
    }
}

fun Application.requireGymRepository(): GymRepository =
    (this as GymTrackerApp).repository
