package com.example.playlistmaker.mediaLibrary.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.playlistmaker.mediaLibrary.data.db.dao.FavoriteTrackDao
import com.example.playlistmaker.mediaLibrary.data.db.dao.PlaylistDao
import com.example.playlistmaker.mediaLibrary.data.db.dao.PlaylistTrackDao
import com.example.playlistmaker.mediaLibrary.data.db.dao.TrackDao
import com.example.playlistmaker.mediaLibrary.data.db.entity.FavoriteTrackEntity
import com.example.playlistmaker.mediaLibrary.data.db.entity.PlaylistEntity
import com.example.playlistmaker.mediaLibrary.data.db.entity.PlaylistTrackEntity
import com.example.playlistmaker.mediaLibrary.data.db.entity.TrackEntity

@Database(
    entities = [
        FavoriteTrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        TrackEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteTrackDao(): FavoriteTrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackDao(): PlaylistTrackDao
    abstract fun trackDao(): TrackDao

    companion object {
        const val DATABASE_NAME = "playlistmaker.db"

        fun getInstance(app: android.app.Application): AppDatabase {
            return Room.databaseBuilder(
                app,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .build()
        }
    }
}