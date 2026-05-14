package com.example.thelastoutpost.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Modelo para un logro desbloqueable.
 */
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val iconResId: Int? = null
)
