package com.mathematics.squares.presentation.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mathematics.squares.presentation.view.theme.Themes

@Entity(tableName = "SettingsTable")
data class Settings(
    @PrimaryKey var id: Int = 1,
    var theme: Themes? = null
)