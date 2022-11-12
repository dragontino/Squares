package com.mathematics.squares.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mathematics.squares.presentation.model.Settings
import com.mathematics.squares.presentation.view.theme.Themes
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert(entity = Settings::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSettings(settings: Settings)

    @Query("UPDATE SettingsTable SET theme = :theme")
    suspend fun updateTheme(theme: Themes)

    @Query("SELECT * FROM SettingsTable WHERE id = 1")
    fun getSettings(): Flow<Settings?>
}