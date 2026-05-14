package com.example.thelastoutpost.model
    
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class Skill(
    @PrimaryKey
    val id: String,
    val category: String, // "Vida", "Daño", "Velocidad", "Especial"
    val name: String,
    val description: String,
    val cost: Int,
    val isUnlocked: Boolean = false,
    val requiredSkillId: String? = null // Para asegurar que se compran en orden
)
