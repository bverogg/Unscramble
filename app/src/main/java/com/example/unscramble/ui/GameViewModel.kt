package com.example.unscramble.ui

import androidx.lifecycle.ViewModel
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.unscramble.data.allWords
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    // Game UI state
    // Propiedad de respaldo para evitar actualizaciones de estado de otras clases
    private val _uiState = MutableStateFlow(GameUiState())
    //agrega una propiedad de copia de seguridad
    //El asStateFlow() hace que este flujo de estado mutable sea de solo lectura.
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    //agrega una propiedad llamada currentWord del tipo String para guardar la palabra desordenada actual.
    private lateinit var currentWord: String
    // Set of words used in the game
    //conjunto mutable y almacene las palabras usadas en el juego
    private var usedWords: MutableSet<String> = mutableSetOf()

    //agrega una propiedad var denominada userGuess. Usa mutableStateOf()
    // de modo que Compose observe este valor y establezca el valor inicial en "".
    var userGuess by mutableStateOf("")
        private set

    init {
        resetGame()
    }

    // Función para elegir una palabra aleatoria de la lista y desordenarla.
    private fun pickRandomWordAndShuffle(): String {
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord = allWords.random()
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    // Función para desordenar la palabra actual
    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        // Scramble the word
        tempWord.shuffle()
        while (String(tempWord).equals(word)) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    // Función para inicializar el juego
    //En esta función, borra todas las palabras del conjunto usedWords, e inicializa el _uiState.
    // Elige una palabra nueva para currentScrambledWord con pickRandomWordAndShuffle()
    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }



    //En el archivo GameViewModel.kt, agrega un método llamado updateUserGuess() que tome un argumento String,
    // que sería la palabra propuesta por el usuario.
    // Dentro de la función, actualiza el elemento userGuess con el elemento guessedWord que se pasó.
    fun updateUserGuess(guessedWord: String){
        userGuess = guessedWord
    }

    //verificar la palabra que propone un usuario y, luego,
    // actualizarás la puntuación del juego o harás que se muestre un error.
    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            // User's guess is correct, increase the score
            // and call updateGameState() to prepare the game for next round
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        } else {
            // User's guess is wrong, show an error
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // Reset user guess
        updateUserGuess("")
    }

    //para actualizar la puntuación, aumenta la cantidad actual de palabras y elige una palabra nueva
    // del archivo WordsData.kt. Agrega un Int llamado updatedScore como parámetro.
    // Actualiza las variables de la IU del estado del juego
    private fun updateGameState(updatedScore: Int) {
        //para verificar que el tamaño del elemento usedWords sea igual a MAX_NO_OF_WORDS.
        if (usedWords.size == MAX_NO_OF_WORDS){
            //Last round in the game, update isGameOver to true, don't pick a new word
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else {
            // Normal round in the game
            // Usa la función copy() para copiar un objeto, lo que te permite modificar algunas
            // de sus propiedades sin modificar el resto.
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    score = updatedScore,
                    //aumenta la cantidad de palabras
                    currentWordCount = currentState.currentWordCount.inc(),
                )
            }
        }
    }

    //Cuando el usuario omite una palabra
    fun skipWord() {
        updateGameState(_uiState.value.score)
        // Reset user guess
        updateUserGuess("")
    }

}
