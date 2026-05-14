package com.example.thelastoutpost.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.thelastoutpost.model.SaveSlot
import com.example.thelastoutpost.model.Achievement
import com.example.thelastoutpost.model.Skill

@Dao
interface GameDao {
    // --- SaveSlot ---
    @Query("SELECT * FROM save_slots")
    fun getAllSaveSlots(): LiveData<List<SaveSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaveSlot(slot: SaveSlot)

    @Update
    suspend fun updateSaveSlot(slot: SaveSlot)

    @Delete
    suspend fun deleteSaveSlot(slot: SaveSlot)

    @Query("SELECT * FROM save_slots WHERE id = :id LIMIT 1")
    suspend fun getSaveSlotById(id: String): SaveSlot?

    // --- Achievement ---
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): LiveData<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getAchievementsCount(): Int

    // --- Skill ---
    @Query("SELECT * FROM skills")
    fun getAllSkills(): LiveData<List<Skill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: Skill)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<Skill>)
}
