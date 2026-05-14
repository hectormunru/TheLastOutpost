package com.example.thelastoutpost.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.thelastoutpost.model.SaveSlot
import com.example.thelastoutpost.repository.GameRepository
import kotlinx.coroutines.launch

class WelcomeViewModel(private val repository: GameRepository) : ViewModel() {

    val saveSlots: LiveData<List<SaveSlot>> = repository.getSaveSlots()

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WelcomeViewModel::class.java)) {
                return WelcomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    fun createNewGame(slotName: String, onGameCreated: (SaveSlot) -> Unit) {
        viewModelScope.launch {
            val newSlot = repository.createNewGame(slotName)
            onGameCreated(newSlot)
        }
    }

    fun deleteSaveSlot(slot: SaveSlot) {
        viewModelScope.launch {
            repository.deleteSaveSlot(slot)
        }
    }
}
