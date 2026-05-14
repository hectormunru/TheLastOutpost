package com.example.thelastoutpost.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.thelastoutpost.model.Achievement
import com.example.thelastoutpost.repository.GameRepository

class AchievementsViewModel(repository: GameRepository) : ViewModel() {

    val achievements: LiveData<List<Achievement>> = repository.getAchievements()

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AchievementsViewModel::class.java)) {
                return AchievementsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
