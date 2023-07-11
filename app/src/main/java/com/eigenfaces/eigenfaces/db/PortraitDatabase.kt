package com.eigenfaces.eigenfaces.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Portrait::class], version = 1, exportSchema = false)
abstract class PortraitDatabase : RoomDatabase() {

    abstract fun portraitDao() : PortraitDao

    companion object {
        @Volatile
        private var INSTANCE : PortraitDatabase? = null

        fun getInstance(context: Context) : PortraitDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PortraitDatabase::class.java,
                        "portrait_data_database"
                    ).build()
                }
                return instance
            }
        }
    }

}