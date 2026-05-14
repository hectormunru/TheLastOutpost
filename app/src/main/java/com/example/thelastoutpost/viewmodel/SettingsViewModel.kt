package com.example.thelastoutpost.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.thelastoutpost.repository.GameRepository

class SettingsViewModel(private val repository: GameRepository) : ViewModel() {

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    val soundEnabled = MutableLiveData(true)
    val playerSpeed = MutableLiveData(50)

    fun getPlayerName(): String = repository.getPlayerName()
    
    fun setPlayerName(name: String) {
        repository.setPlayerName(name)
    }
}
