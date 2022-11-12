package com.mathematics.squares.data.repository

import com.mathematics.squares.data.room.SettingsDao
import com.mathematics.squares.presentation.model.Settings
import com.mathematics.squares.presentation.view.theme.Themes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val settingsDao: SettingsDao) {
    private suspend fun addSettings(settings: Settings) {
        settingsDao.addSettings(settings)
    }

    suspend fun updateTheme(theme: Themes) {
        settingsDao.updateTheme(theme)
    }

    fun getSettings(): Flow<Settings> =
        settingsDao.getSettings().map {
            if (it == null) addSettings(Settings())
            it ?: Settings()
        }
}