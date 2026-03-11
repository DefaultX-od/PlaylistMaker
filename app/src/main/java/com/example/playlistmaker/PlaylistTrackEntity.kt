package com.example.playlistmaker

import androidx.room.Entity

@Entity(tableName = "playlist_tracks_table", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrackEntity(
    val trackId: Int,
    val playlistId: Int,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Int,
    val artworkUrl100: String,
    val collectionName: String,
    val releaseDate: String,
    val primaryGenreName: String,
    val country: String,
    val previewUrl: String,
    val createdAt: Long
)