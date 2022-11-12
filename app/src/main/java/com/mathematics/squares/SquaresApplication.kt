package com.mathematics.squares

import android.app.Application
import com.mathematics.squares.presentation.viewmodel.ViewModelFactory

class SquaresApplication : Application() {
    val viewModelFactory: ViewModelFactory by lazy {
        ViewModelFactory(this)
    }
}