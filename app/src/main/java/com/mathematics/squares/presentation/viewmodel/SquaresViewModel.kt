package com.mathematics.squares.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.mathematics.squares.data.repository.SquaresRepository
import com.mathematics.squares.presentation.model.Cords
import com.mathematics.squares.presentation.model.Square

class SquaresViewModel(private val squaresRepository: SquaresRepository) : ViewModel() {
    companion object {
        @Volatile
        private var INSTANCE: SquaresViewModel? = null

        fun getInstance(
            owner: ViewModelStoreOwner,
            factory: ViewModelFactory
        ): SquaresViewModel {
            val temp = INSTANCE
            if (temp != null)
                return temp

            synchronized(this) {
                val instance = ViewModelProvider(owner, factory)[SquaresViewModel::class.java]
                INSTANCE = instance
                return instance
            }
        }
    }

    val square = MutableLiveData(mapOf<Cords, Square>())

    fun updateSquare(square: Map<Cords, Square>) {
        this.square.value = square
    }


    fun getRandomColor() = squaresRepository.getRandomColor()


    suspend fun updateSquare(squareLength: Int, squareCounts: IntArray) =
        calculateSquaresPositions(squareLength, squareCounts).also {
//            Log.d("SquaresViewModel", "free space = ${it?.freeSpace}")
            if (it != null) this.square.value = it
        } != null


    suspend fun calculateSquaresPositions(
        squareLength: Int,
        x5: Int = 0,
        x4: Int = 0,
        x3: Int = 0,
        x2: Int = 0
    ) = squaresRepository.calculateSquaresPositions(squareLength, x5, x4, x3, x2)


    private suspend fun calculateSquaresPositions(squareLength: Int, squareCounts: IntArray) =
        calculateSquaresPositions(
            squareLength = squareLength,
            x5 = squareCounts[0],
            x4 = squareCounts[1],
            x3 = squareCounts[2],
            x2 = squareCounts[3]
        )
}