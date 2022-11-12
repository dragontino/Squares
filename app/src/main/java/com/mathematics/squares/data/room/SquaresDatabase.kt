package com.mathematics.squares.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mathematics.squares.presentation.model.Settings

@Database(entities = [Settings::class], version = 1, exportSchema = false)
abstract class SquaresDatabase : RoomDatabase() {
    abstract val settingsDao: SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: SquaresDatabase? = null

        fun getDatabase(context: Context): SquaresDatabase {
            val temp = INSTANCE
            if (temp != null)
                return temp

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SquaresDatabase::class.java,
                    "SquaresDatabase"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}