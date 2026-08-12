package com.ysompo.englishwords.ui.spelling

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ysompo.englishwords.logic.LetterState
import com.ysompo.englishwords.logic.WordleEvaluator

data class GuessResult(val guess: String, val letterStates: List<LetterState>)

data class SpellingChallengeState(
    val targetWord: String,
    val guesses: List<GuessResult>,
    val solved: Boolean,
    val attemptsRemaining: Int
) {
    val gameOver: Boolean get() = solved || attemptsRemaining <= 0
}

class SpellingChallengeViewModel : ViewModel() {
    companion object {
        const val MAX_ATTEMPTS = 6
    }

    val state = MutableLiveData<SpellingChallengeState>()

    fun start(targetWord: String) {
        state.value = SpellingChallengeState(
            targetWord = targetWord.lowercase(),
            guesses = emptyList(),
            solved = false,
            attemptsRemaining = MAX_ATTEMPTS
        )
    }

    fun submitGuess(guess: String) {
        val current = state.value ?: return
        if (current.gameOver) return
        if (guess.length != current.targetWord.length) return

        val normalizedGuess = guess.lowercase()
        val letterStates = WordleEvaluator.evaluate(normalizedGuess, current.targetWord)
        val solved = normalizedGuess == current.targetWord

        state.value = current.copy(
            guesses = current.guesses + GuessResult(normalizedGuess, letterStates),
            solved = solved,
            attemptsRemaining = current.attemptsRemaining - 1
        )
    }
}
