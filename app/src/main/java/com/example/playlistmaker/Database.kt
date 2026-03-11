package com.example.playlistmaker

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 2, entities = [FavoriteTrackEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class])
abstract class Database : RoomDatabase() {
    abstract fun favoriteTrackDao(): FavoriteTrackDao
    abstract fun playlistDao(): PlaylistDao
}