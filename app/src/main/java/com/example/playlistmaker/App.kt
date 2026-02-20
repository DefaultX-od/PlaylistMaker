package com.example.playlistmaker

import android.app.Application
import androidx.room.Room

class App : Application() {

    val database : Database by lazy {
        Room.databaseBuilder(this, Database::class.java, "database.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    override fun onCreate() {
        super.onCreate()
    }

}