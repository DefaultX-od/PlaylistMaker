package com.example.playlistmaker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: PlaylistTrackEntity): Long

    @Query("SELECT *, ( SELECT COUNT(*) FROM playlist_tracks_table WHERE playlistId = playlist_table.playlistId) as trackCount FROM playlist_table ORDER BY playlistId DESC")
    suspend fun getPlaylists(): List<PlaylistWithCount>

    @Query("SELECT * FROM playlist_tracks_table WHERE playlistId = :playlistId ORDER BY createdAt DESC")
    suspend fun getTracksByPlaylistId(playlistId: Int): List<PlaylistTrackEntity>
}