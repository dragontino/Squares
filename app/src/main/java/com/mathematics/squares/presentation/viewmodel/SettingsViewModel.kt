package com.mathematics.squares.presentation.viewmodel

import androidx.lifecycle.*
import com.mathematics.squares.data.repository.SettingsRepository
import com.mathematics.squares.presentation.view.theme.Themes
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    companion object {
        @Volatile
        private var INSTANCE: SettingsViewModel? = null

        fun getInstance(
            owner: ViewModelStoreOwner,
            factory: ViewModelFactory
        ): SettingsViewModel {
            val temp = INSTANCE
            if (temp != null)
                return temp

            synchronized(this) {
                val instance = ViewModelProvider(owner, factory)[SettingsViewModel::class.java]
                INSTANCE = instance
                return instance
            }
        }
    }

    val settingsLiveData = settingsRepository.getSettings().asLiveData()

    fun updateTheme(theme: Themes) {
        viewModelScope.launch {
            settingsRepository.updateTheme(theme)
        }
    }
}