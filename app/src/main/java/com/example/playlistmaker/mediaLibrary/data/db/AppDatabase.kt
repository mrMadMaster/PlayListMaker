package com.example.playlistmaker.mediaLibrary.data.db

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.playlistmaker.mediaLibrary.data.db.dao.FavoriteTrackDao
import com.example.playlistmaker.mediaLibrary.data.db.entity.FavoriteTrackEntity

@Database(
    entities = [FavoriteTrackEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteTrackDao(): FavoriteTrackDao

    companion object {
        private const val DATABASE_NAME = "playlistmaker.db"

        fun getInstance(app: Application): AppDatabase {
            return Room.databaseBuilder(
                app,
                AppDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}