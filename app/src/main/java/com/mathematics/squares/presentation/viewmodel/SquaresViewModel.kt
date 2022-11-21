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

    val squaresMap = MutableLiveData(mapOf<Cords, Square>())

    fun updateSquaresMap(squaresMap: Map<Cords, Square>) {
        this.squaresMap.value = squaresMap
    }

    suspend fun updateSquaresMap(
        squareSideLength: Int,
        squareCounts: IntArray,
        recalculate: Boolean = true
    ): Boolean {
        return if (recalculate) {
            calculateSquaresPositions(squareSideLength, squareCounts).also {
                if (it != null) this.squaresMap.value = it
            } != null
        } else {
            this.squaresMap.value = updateSquaresPositions(squareSideLength)
            true
        }
    }


    private suspend fun updateSquaresPositions(
        squareSideLength: Int,
    ): Map<Cords, Square> {
        val squaresMap = squaresMap.value ?: mapOf()
        return squaresRepository.updateSquaresPositions(
            squareSideLength = squareSideLength,
            squaresList = squaresMap.toSquaresList(squareSideLength)
        )
    }


    private fun getRandomColor() = squaresRepository.getRandomColor()
    private fun getRandomColors(count: Int) =
        squaresRepository.shuffleColors().sliceArray(0 until count)


    private fun Map<Cords, Square>.toSquaresList(squareSideLength: Int): List<Square> {
        val newColors = mapOf(
            5 to getRandomColor(),
            4 to getRandomColor(),
            3 to getRandomColor(),
            2 to getRandomColor(),
            1 to getRandomColor(),
        )
        return this.keys.sorted()
            .map { cords ->
                val size = this[cords]?.sideLength ?: 1
                Square(size, color = newColors[size] ?: getRandomColor())
            }
            .ifEmpty {
                buildList {
                    repeat(squareSideLength * squareSideLength) {
                        add(Square(sideLength = 1, color = getRandomColor()))
                    }
                }
            }
    }


    suspend fun calculateSquaresPositions(
        squareLength: Int,
        x5: Int = 0,
        x4: Int = 0,
        x3: Int = 0,
        x2: Int = 0
    ) = calculateSquaresPositions(squareLength, intArrayOf(x5, x4, x3, x2))


    private suspend fun calculateSquaresPositions(
        squareLength: Int,
        squareCounts: IntArray,
    ): Map<Cords, Square>? {
        val colors = getRandomColors(squareCounts.size)
        val squaresList = buildList {
            squareCounts.forEachIndexed { index, count ->
                repeat(count) {
                    this += Square(sideLength = 5 - index, color = colors[index])
                }
            }
        }
        return squaresRepository.placeSquares(squareLength, squaresList)
    }
}