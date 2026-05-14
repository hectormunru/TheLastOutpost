package com.example.thelastoutpost.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "save_slots")
data class SaveSlot(
    @PrimaryKey
    val id: String,
    val slotName: String,
    val playerName: String,
    val day: Int,
    val positionX: Float = 0f,
    val timeOfDay: Float = 0f
)
