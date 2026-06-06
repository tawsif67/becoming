package com.example.becoming.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CharacterEntity::class,
        QuestEntity::class,
        ChronicleEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BecomingDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun questDao(): QuestDao
    abstract fun chronicleDao(): ChronicleDao

    companion object {
        @Volatile
        private var INSTANCE: BecomingDatabase? = null

        fun getDatabase(context: Context): BecomingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BecomingDatabase::class.java,
                    "becoming_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
