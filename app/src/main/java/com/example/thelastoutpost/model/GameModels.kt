package com.example.thelastoutpost.model

enum class BuildingType {
    CASTLE, ARCHER_TOWER, CANNON, BUILDING_SLOT, SHOP_FOOD, SHOP_WEAPON, SHOP_MOUNT
}

enum class EnemyType {
    GOBLIN, SKELETON, WIZARD
}

data class Building(
    val id: String,
    val type: BuildingType,
    val positionX: Float,
    var level: Int = 1,
    var health: Int = 100
)

data class PlayerState(
    val positionX: Float = 0f,
    val gold: Int = 0,
    val isMoving: Boolean = false,
    val direction: Int = 1,
    val currentHp: Int = 100,
    val maxHp: Int = 100,
    val speed: Float = 5f, // Cambiado de 10f a 5f, que es usado por MoveSpeed
    val damageMultiplier: Float = 1.0f,
    val hasArchers: Boolean = false,
    val hasGoldenSkin: Boolean = false
)

data class Villager(
    val id: String,
    val type: Int, // 1, 2 o 3 para el sprite
    var positionX: Float,
    var targetX: Float,
    var isMoving: Boolean = false
)

data class Enemy(
    val id: String,
    val positionX: Float,
    val health: Int = 20,
    val maxHealth: Int = 20,
    val speed: Float = 5f,
    val type: EnemyType = EnemyType.GOBLIN,
    val isDying: Boolean = false,
    val isAttacking: Boolean = false,
    val attackType: Int = 1, // 1 para ataque básico, 2 para invocaciones/otro ataque
    val facingRight: Boolean = true,
    val attackTimer: Int = 0 // Ticks restantes para el siguiente impacto o fase de ataque
)

data class GameState(
    val player: PlayerState = PlayerState(),
    val buildings: List<Building> = emptyList(),
    val enemies: List<Enemy> = emptyList(),
    val villagers: List<Villager> = emptyList(),
    val day: Int = 1,
    val wave: Int = 1,
    val timeOfDay: Float = 0f, // 0 a 120 (0=amanecer, 50=noche, 120=nuevo día)
    val isGameOver: Boolean = false,
    val wizardKills: Int = 0
)