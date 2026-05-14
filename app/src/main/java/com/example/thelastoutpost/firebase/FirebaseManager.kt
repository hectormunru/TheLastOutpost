package com.example.thelastoutpost.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import timber.log.Timber

object FirebaseManager {

    private val db = FirebaseFirestore.getInstance()
    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        // Configuración de Remote Config
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // Refrescar cada hora
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        // Valores por defecto para Remote Config
        val defaultValues = mapOf(
            "zombie_speed" to 2.0,
            "spawn_rate" to 5.0,
            "is_event_active" to false
        )
        remoteConfig.setDefaultsAsync(defaultValues)
    }

    /**
     * Guarda el progreso del jugador en Firestore
     */
    fun saveGameProgress(userId: String, data: Map<String, Any>) {
        db.collection("players").document(userId)
            .set(data)
            .addOnSuccessListener {
                Timber.d("Progreso guardado correctamente en Firestore para el usuario: $userId")
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Error al guardar progreso en Firestore")
            }
    }

    /**
     * Obtiene un valor de Remote Config
     */
    fun getRemoteConfigValue(key: String): String {
        return remoteConfig.getString(key)
    }

    /**
     * Sincroniza los valores de Remote Config con la nube
     */
    fun fetchRemoteConfig(onComplete: (Boolean) -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Remote Config actualizado")
                    onComplete(true)
                } else {
                    Timber.e("Error al actualizar Remote Config")
                    onComplete(false)
                }
            }
    }
}
