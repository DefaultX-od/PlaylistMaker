package com.example.playlistmaker

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 1, entities = [FavoriteTrackEntity::class])
abstract class Database : RoomDatabase() {
    abstract fun favoriteTrackDao(): FavoriteTrackDao
}