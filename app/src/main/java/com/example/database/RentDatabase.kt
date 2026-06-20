package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.*

@Database(
    entities = [
        User::class,
        Bike::class,
        Booking::class,
        Invoice::class,
        Payment::class,
        Agreement::class,
        Notification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RentDatabase : RoomDatabase() {
    abstract val appDao: AppDao

    companion object {
        @Volatile
        private var INSTANCE: RentDatabase? = null

        fun getDatabase(context: Context): RentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RentDatabase::class.java,
                    "ridedeal_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
