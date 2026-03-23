package com.example.playlistmaker.mediaLibrary.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.playlistmaker.mediaLibrary.data.db.dao.FavoriteTrackDao
import com.example.playlistmaker.mediaLibrary.data.db.dao.PlaylistDao
import com.example.playlistmaker.mediaLibrary.data.db.dao.PlaylistTrackDao
import com.example.playlistmaker.mediaLibrary.data.db.entity.FavoriteTrackEntity
import com.example.playlistmaker.mediaLibrary.data.db.entity.PlaylistEntity
import com.example.playlistmaker.mediaLibrary.data.db.entity.PlaylistTrackEntity

@Database(
    entities = [
        FavoriteTrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteTrackDao(): FavoriteTrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackDao(): PlaylistTrackDao

    companion object {
        const val DATABASE_NAME = "playlistmaker.db"

        fun getInstance(app: android.app.Application): AppDatabase {
            return Room.databaseBuilder(
                app,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}