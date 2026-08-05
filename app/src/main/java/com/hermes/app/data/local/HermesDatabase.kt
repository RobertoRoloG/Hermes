package com.hermes.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hermes.app.data.local.dao.TaskDao
import com.hermes.app.data.local.entity.TaskEntity

@Database(entities = [TaskEntity::class], version = 3, exportSchema = false)
abstract class HermesDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: HermesDatabase? = null

        fun getDatabase(context: Context): HermesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HermesDatabase::class.java,
                    "hermes_database",
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
