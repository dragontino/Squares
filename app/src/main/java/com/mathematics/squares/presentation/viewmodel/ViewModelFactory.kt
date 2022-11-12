package com.mathematics.squares.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.mathematics.squares.data.repository.SettingsRepository
import com.mathematics.squares.data.repository.SquaresRepository
import com.mathematics.squares.data.room.SquaresDatabase

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras) = when {
        modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(
            settingsRepository = SettingsRepository(
                settingsDao = SquaresDatabase.getDatabase(application).settingsDao
            )
        ) as T
        modelClass.isAssignableFrom(SquaresViewModel::class.java) -> SquaresViewModel(
            squaresRepository = SquaresRepository()
        ) as T
        else -> throw IllegalArgumentException("Unknown ViewModel class")
    }
}