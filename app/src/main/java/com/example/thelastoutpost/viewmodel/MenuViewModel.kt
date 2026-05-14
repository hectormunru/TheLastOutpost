package com.example.thelastoutpost.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

/**
 * ViewModel para la pantalla de menú.
 * Usa LiveData para que la UI reaccione automáticamente a los cambios.
 */
class MenuViewModel : ViewModel() {

    // Campo editable: el nombre del jugador (two-way DataBinding con el EditText)
    val playerName = MutableLiveData("")

    // Campo derivado: el botón "Jugar" solo se activa si hay nombre escrito
    val isPlayEnabled: LiveData<Boolean> = playerName.map { name ->
        !name.isNullOrBlank()
    }
}
