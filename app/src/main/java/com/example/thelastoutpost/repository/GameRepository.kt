package com.example.thelastoutpost.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.example.thelastoutpost.database.dao.GameDao
import com.example.thelastoutpost.model.Achievement
import com.example.thelastoutpost.model.SaveSlot
import com.example.thelastoutpost.model.Skill
import com.example.thelastoutpost.firebase.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class GameRepository(
    context: Context,
    private val gameDao: GameDao
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("game_stats", Context.MODE_PRIVATE)
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // --- Gestión del Nombre Global ---
    fun setPlayerName(name: String) {
        prefs.edit().putString("global_player_name", name).apply()
    }

    fun getPlayerName(): String = prefs.getString("global_player_name", "") ?: ""

    // --- Persistencia de Ranuras de Guardado (ROOM) --- 
    fun getSaveSlots(): LiveData<List<SaveSlot>> = gameDao.getAllSaveSlots()

    suspend fun getSaveSlotById(id: String): SaveSlot? = gameDao.getSaveSlotById(id)

    suspend fun createNewGame(slotName: String): SaveSlot {
        val nextId = "slot_${System.currentTimeMillis()}"
        val playerName = getPlayerName()
        val newSlot = SaveSlot(id = nextId, slotName = slotName, playerName = playerName, day = 1, positionX = 2500f)
        
        gameDao.insertSaveSlot(newSlot)
        
        // Resetear solo el current_gold para la nueva partida.
        // total_gold, kills, max_wave, y las skills son globales (progreso meta)
        val editor = prefs.edit()
        
        // current_gold es el oro de la partida/reserva, si el oro local debe reiniciarse:
        // Cuidado: si "current_gold" se usa para comprar skills en el cuartel general,
        // entonces reiniciar current_gold también es malo. Lo dejaremos intacto por ahora.
        // O lo limpiaremos si es intencional que la "reserva de oro" se pierda.
        // Analizando la economía del juego: current_gold se mantiene entre partidas.
        
        editor.putString("active_slot", nextId)
        editor.apply()
        editor.apply()
        
        // Sincronizar nueva partida con Firebase
        syncWithFirebase(newSlot)
            
        return newSlot
    }

    suspend fun deleteSaveSlot(slot: SaveSlot) {
        gameDao.deleteSaveSlot(slot)
    }

    suspend fun saveGameProgress(slotId: String, day: Int, positionX: Float, timeOfDay: Float) {
        val slot = gameDao.getSaveSlotById(slotId)
        if (slot != null) {
            val updatedSlot = slot.copy(day = day, positionX = positionX, timeOfDay = timeOfDay)
            gameDao.updateSaveSlot(updatedSlot)
            
            // Sincronizar progreso con Firebase
            syncWithFirebase(updatedSlot)
        }
    }

    /**
     * Helper para enviar todos los datos actuales a la nube
     */
    private fun syncWithFirebase(slot: SaveSlot) {
        val firebaseData = mapOf(
            "slotInfo" to slot,
            "stats" to mapOf(
                "current_gold" to getCurrentGold(),
                "total_gold" to getTotalGold(),
                "kills" to getKills(),
                "max_wave" to getMaxWave()
            ),
            "lastUpdated" to System.currentTimeMillis()
        )
        FirebaseManager.saveGameProgress(slot.id, firebaseData)
    }

    // --- Estadísticas en SharedPreferences ---
    fun addKills(count: Int = 1) {
        val current = getKills()
        val newValue = current + count
        prefs.edit().putInt("kills", newValue).apply()
        timber.log.Timber.d("Estadísticas: Bajas actualizadas: $newValue")
        checkAchievements() // Comprobar logros al subir estadísticas
    }

    fun getKills(): Int = prefs.getInt("kills", 0)

    fun addGold(count: Int) {
        val currentTotal = getTotalGold()
        val currentReserva = getCurrentGold()
        
        val newTotal = currentTotal + count
        val newReserva = currentReserva + count
        
        prefs.edit()
            .putInt("total_gold", newTotal)
            .putInt("current_gold", newReserva)
            .apply()
            
        timber.log.Timber.d("Estadísticas: Oro sumado +$count. Total: $newTotal, Reserva: $newReserva")
        checkAchievements()
    }

    fun spendGold(amount: Int): Boolean {
        val current = getCurrentGold()
        if (current >= amount) {
            prefs.edit().putInt("current_gold", current - amount).apply()
            return true
        }
        return false
    }

    fun getCurrentGold(): Int = prefs.getInt("current_gold", 0)

    fun getTotalGold(): Int = prefs.getInt("total_gold", 0)

    fun updateMaxWave(wave: Int) {
        val current = getMaxWave()
        if (wave > current) {
            prefs.edit().putInt("max_wave", wave).apply()
            checkAchievements()
        }
    }

    fun getMaxWave(): Int = prefs.getInt("max_wave", 0)

    fun updateDay(day: Int) {
        val current = prefs.getInt("max_day", 0)
        if (day > current) {
            prefs.edit().putInt("max_day", day).apply()
            checkAchievements()
        }
    }

    // --- Persistencia de Habilidades (ROOM + Prefs por ahora) ---
    // Idealmente migraríamos todo a Room, pero mantenemos parte en Prefs para simplicidad en la transición.
    fun isSkillUnlocked(skillId: String): Boolean {
        return prefs.getBoolean("skill_$skillId", false)
    }

    fun unlockSkill(skillId: String) {
        prefs.edit().putBoolean("skill_$skillId", true).apply()
    }
    
    // --- Logros (ROOM) ---
    fun getAchievements(): LiveData<List<Achievement>> = gameDao.getAllAchievements()

    suspend fun initializeAchievements() {
        if (gameDao.getAchievementsCount() == 0) {
            val initialAchievements = listOf(
                Achievement("1", "Primeros Pasos", "Sobrevive a tu primer día en el puesto.", false),
                Achievement("2", "Ahorrador", "Acumula 100 de oro en total.", false),
                Achievement("3", "Exterminador", "Derrota a 10 enemigos.", false),
                Achievement("4", "Héroe de Guerra", "Derrota a 50 enemigos.", false),
                Achievement("5", "Veterano", "Alcanza la oleada 5.", false)
            )
            gameDao.insertAchievements(initialAchievements)
        }
        // Sincronizar estado actual basado en las estadísticas guardadas
        checkAchievements()
    }

    private fun checkAchievements() {
        repositoryScope.launch {
            val kills = getKills()
            val totalGold = getTotalGold()
            val maxWave = getMaxWave()
            val maxDay = prefs.getInt("max_day", 0)

            // 1. Primeros Pasos: Sobrevive a tu primer día
            if (maxDay >= 2) {
                unlockAchievementInternal("1")
            }
            // 2. Ahorrador: 100 de oro total
            if (totalGold >= 100) {
                unlockAchievementInternal("2")
            }
            // 3. Exterminador: 10 enemigos
            if (kills >= 10) {
                unlockAchievementInternal("3")
            }
            // 4. Héroe de Guerra: 50 enemigos
            if (kills >= 50) {
                unlockAchievementInternal("4")
            }
            // 5. Veterano: Oleada 5
            if (maxWave >= 5) {
                unlockAchievementInternal("5")
            }
        }
    }

    private suspend fun unlockAchievementInternal(id: String) {
        val isAlreadyUnlocked = prefs.getBoolean("achievement_unlocked_$id", false)
        if (!isAlreadyUnlocked) {
            prefs.edit().putBoolean("achievement_unlocked_$id", true).apply()
            
            val title = when(id) {
                "1" -> "Primeros Pasos"
                "2" -> "Ahorrador"
                "3" -> "Exterminador"
                "4" -> "Héroe de Guerra"
                "5" -> "Veterano"
                else -> ""
            }
            val desc = when(id) {
                "1" -> "Sobrevive a tu primer día en el puesto."
                "2" -> "Acumula 100 de oro en total."
                "3" -> "Derrota a 10 enemigos."
                "4" -> "Derrota a 50 enemigos."
                "5" -> "Alcanza la oleada 5."
                else -> ""
            }
            gameDao.insertAchievement(Achievement(id, title, desc, true))
        }
    }
}
