package com.example.thelastoutpost.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.thelastoutpost.model.*
import com.example.thelastoutpost.repository.GameRepository

class GameViewModel(private val repository: GameRepository) : ViewModel() {
    
    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                return GameViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    
    private val _gameState = MutableLiveData(GameState(
        player = PlayerState(positionX = 1250f),
        buildings = listOf(
            // ── Zona 2 (Defensas Oeste: 150-550dp) ──────────────────
            Building("tower_w1", BuildingType.ARCHER_TOWER, positionX = 180f),
            Building("slot_w1",  BuildingType.BUILDING_SLOT, positionX = 280f),
            Building("slot_w2",  BuildingType.BUILDING_SLOT, positionX = 380f),
            Building("tower_w2", BuildingType.ARCHER_TOWER, positionX = 490f),

            // ── Zona 3 (Pueblo Oeste: 550-1100dp) ───────────────────
            Building("house_w1", BuildingType.SHOP_FOOD,   positionX = 600f),
            Building("house_w2", BuildingType.SHOP_WEAPON, positionX = 750f),
            Building("house_w3", BuildingType.SHOP_FOOD,   positionX = 880f),
            Building("house_w4", BuildingType.SHOP_WEAPON, positionX = 1010f),

            // ── Zona 4 (Castillo: 1100-1300dp) ──────────────────────
            Building("castle",   BuildingType.CASTLE,      positionX = 1150f),

            // ── Zona 5 (Pueblo Este: 1300-1850dp) ───────────────────
            Building("house_e1", BuildingType.SHOP_FOOD,   positionX = 1360f),
            Building("house_e2", BuildingType.SHOP_WEAPON, positionX = 1500f),
            Building("house_e3", BuildingType.SHOP_FOOD,   positionX = 1630f),
            Building("house_e4", BuildingType.SHOP_WEAPON, positionX = 1780f),

            // ── Zona 6 (Defensas Este: 1850-2250dp) ─────────────────
            Building("tower_e1", BuildingType.ARCHER_TOWER, positionX = 1870f),
            Building("slot_e1",  BuildingType.BUILDING_SLOT, positionX = 1970f),
            Building("slot_e2",  BuildingType.BUILDING_SLOT, positionX = 2080f),
            Building("tower_e2", BuildingType.ARCHER_TOWER, positionX = 2200f)
        ),
        villagers = listOf(
            // Pueblo Oeste
            Villager("v1", 2, 650f,  700f),
            Villager("v2", 3, 800f,  850f),
            Villager("v3", 2, 950f,  900f),
            Villager("v4", 3, 1070f, 1000f),
            // Pueblo Este
            Villager("v5", 2, 1380f, 1450f),
            Villager("v6", 3, 1550f, 1600f),
            Villager("v7", 2, 1700f, 1650f),
            Villager("v8", 3, 1830f, 1780f)
        )
    ))
    val gameState: LiveData<GameState> = _gameState

    var isLoaded = false
    private var isCurrentlyLoading = false

    fun loadSlot(slotId: String) {
        if (isLoaded || isCurrentlyLoading) return
        isCurrentlyLoading = true
        viewModelScope.launch {
            val slot = repository.getSaveSlotById(slotId)
            
            // Al cargar una partida, usamos la posición guardada pero el mapa completo de 7 zonas
            val savedX = slot?.positionX ?: 2500f

            // Calcular atributos basados en mejoras desbloqueadas
            var maxHp = 100
            if (repository.isSkillUnlocked("hp_1")) maxHp += 20
            if (repository.isSkillUnlocked("hp_2")) maxHp += 20
            if (repository.isSkillUnlocked("hp_3")) maxHp += 40

            var damageMultiplier = 1.0f
            if (repository.isSkillUnlocked("dmg_3")) damageMultiplier = 2.0f
            else if (repository.isSkillUnlocked("dmg_1")) damageMultiplier = 1.4f

            var speed = 5f
            if (repository.isSkillUnlocked("spd_2")) speed = 10f
            else if (repository.isSkillUnlocked("spd_1")) speed = 7f

            val hasArchers = repository.isSkillUnlocked("dmg_2")
            val hasGoldenSkin = repository.isSkillUnlocked("golden_skin")

            val newState = GameState(
                day = slot?.day ?: 1,
                timeOfDay = slot?.timeOfDay ?: 0f,
                player = PlayerState(
                    positionX = savedX,
                    maxHp = maxHp,
                    currentHp = maxHp,
                    damageMultiplier = damageMultiplier,
                    speed = speed,
                    hasArchers = hasArchers,
                    hasGoldenSkin = hasGoldenSkin,
                    gold = repository.getCurrentGold()
                ),
                buildings = listOf(
                    // ── Zona 2 (Defensas Oeste) ─────────────────────────
                    Building("tower_w1", BuildingType.ARCHER_TOWER, positionX = 180f),
                    Building("slot_w1",  BuildingType.BUILDING_SLOT, positionX = 280f),
                    Building("slot_w2",  BuildingType.BUILDING_SLOT, positionX = 380f),
                    Building("tower_w2", BuildingType.ARCHER_TOWER, positionX = 490f),
                    // ── Zona 3 (Pueblo Oeste) ───────────────────────────
                    Building("house_w1", BuildingType.SHOP_FOOD,   positionX = 600f),
                    Building("house_w2", BuildingType.SHOP_WEAPON, positionX = 750f),
                    Building("house_w3", BuildingType.SHOP_FOOD,   positionX = 880f),
                    Building("house_w4", BuildingType.SHOP_WEAPON, positionX = 1010f),
                    // ── Zona 4 (Castillo Central) ───────────────────────
                    Building("castle",   BuildingType.CASTLE,      positionX = 1150f),
                    // ── Zona 5 (Pueblo Este) ────────────────────────────
                    Building("house_e1", BuildingType.SHOP_FOOD,   positionX = 1360f),
                    Building("house_e2", BuildingType.SHOP_WEAPON, positionX = 1500f),
                    Building("house_e3", BuildingType.SHOP_FOOD,   positionX = 1630f),
                    Building("house_e4", BuildingType.SHOP_WEAPON, positionX = 1780f),
                    // ── Zona 6 (Defensas Este) ──────────────────────────
                    Building("tower_e1", BuildingType.ARCHER_TOWER, positionX = 1870f),
                    Building("slot_e1",  BuildingType.BUILDING_SLOT, positionX = 1970f),
                    Building("slot_e2",  BuildingType.BUILDING_SLOT, positionX = 2080f),
                    Building("tower_e2", BuildingType.ARCHER_TOWER, positionX = 2200f)
                ),
                villagers = listOf(
                    Villager("v1", 2, 650f,  700f),
                    Villager("v2", 3, 800f,  850f),
                    Villager("v3", 2, 950f,  900f),
                    Villager("v4", 3, 1070f, 1000f),
                    Villager("v5", 2, 1380f, 1450f),
                    Villager("v6", 3, 1550f, 1600f),
                    Villager("v7", 2, 1700f, 1650f),
                    Villager("v8", 3, 1830f, 1780f)
                )
            )
            isLoaded = true
            isCurrentlyLoading = false
            _gameState.value = newState
            startGameLoop()
        }
    }

    fun finishGame() {
        // No resetear el estado inmediatamente para permitir que onStop guarde la posición real
        isLoaded = false
        isCurrentlyLoading = false
    }

    // Método para limpiar el estado definitivamente (llamado al volver al menú)
    fun clearState() {
        _gameState.value = GameState()
    }

    // Método invocado al volver al juego desde menús para aplicar las mejoras recién compradas
    fun refreshPlayerStats() {
        val currentState = _gameState.value ?: return
        
        var maxHp = 100
        if (repository.isSkillUnlocked("hp_1")) maxHp += 20
        if (repository.isSkillUnlocked("hp_2")) maxHp += 20
        if (repository.isSkillUnlocked("hp_3")) maxHp += 40

        var damageMultiplier = 1.0f
        if (repository.isSkillUnlocked("dmg_3")) damageMultiplier = 2.0f
        else if (repository.isSkillUnlocked("dmg_1")) damageMultiplier = 1.4f

        var speed = 5f
        if (repository.isSkillUnlocked("spd_2")) speed = 10f
        else if (repository.isSkillUnlocked("spd_1")) speed = 7f

        val hasArchers = repository.isSkillUnlocked("dmg_2")
        val hasGoldenSkin = repository.isSkillUnlocked("golden_skin")
        
        // Conservar su vida actual a menos que su vida actual quede por encima de maxHp al quitar un buff
        // Si ganó maxHp extra por una compra, se le aplica como cura.
        val oldMaxHp = currentState.player.maxHp
        val hpDiff = if (maxHp > oldMaxHp) maxHp - oldMaxHp else 0
        var currentHp = currentState.player.currentHp + hpDiff
        if (currentHp > maxHp) currentHp = maxHp

        _gameState.value = currentState.copy(
            player = currentState.player.copy(
                maxHp = maxHp,
                currentHp = currentHp,
                damageMultiplier = damageMultiplier,
                speed = speed,
                hasArchers = hasArchers,
                hasGoldenSkin = hasGoldenSkin,
                gold = repository.getCurrentGold()
            )
        )
    }

    private var isPaused = false
    fun setPaused(paused: Boolean) {
        isPaused = paused
    }

    private var gameLoopJob: kotlinx.coroutines.Job? = null

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (true) {
                delay(100) // 10 ticks por segundo para mayor fluidez
                if (!isPaused && isLoaded) {
                    updateWorld()
                }
            }
        }
    }

    // Temporizador interno para el ataque periódico del mago
    private var wizardAttackCooldown = 0

    // Si el jugador está atacando (se activa desde GameFragment)
    private var pendingPlayerAttack = false
    fun setPlayerAttacking(attacking: Boolean) { 
        if (attacking) pendingPlayerAttack = true
    }

    private fun updateWorld() {
        val currentState = _gameState.value ?: return
        if (currentState.isGameOver) return

        // 1. Avanzar el tiempo
        var nextTime = currentState.timeOfDay + 0.1f // Incremento acorde a 10fps
        var nextDay = currentState.day
        var nextWave = currentState.wave
        if (nextTime >= 140f) {
            nextTime = 0f
            nextDay++
            nextWave++
            repository.updateMaxWave(nextWave)
            repository.updateDay(nextDay)
        }

        val currentEnemies = currentState.enemies.toMutableList()
        val isNight = nextTime > 30f && nextTime < 130f
        val isDay = !isNight
        val centerX = 2500f

        // 2. Desaparecer enemigos si es de día
        if (isDay) {
            currentEnemies.removeAll { true } // Por ahora desaparecen todos al amanecer
        }

        // 3. Spawn del MAGO al inicio de la noche
        val hasWizard = currentEnemies.any { it.type == EnemyType.WIZARD && !it.isDying }
        if (isNight && !hasWizard) {
            val wizardHp = 500 + (currentState.wizardKills * 250)
            val spawnSide = if (kotlin.random.Random.nextBoolean()) 200f else 4800f
            currentEnemies.add(
                Enemy(
                    id = "wizard_${System.currentTimeMillis()}",
                    positionX = spawnSide,
                    health = wizardHp,
                    maxHealth = wizardHp,
                    speed = 3f, // Mago más rápido
                    type = EnemyType.WIZARD,
                    facingRight = spawnSide < centerX
                )
            )
            wizardAttackCooldown = 50 // 5 segundos a 10fps
        }

        // 4. El mago ataca periódicamente invocando un minion
        if (isNight) wizardAttackCooldown--
        if (isNight && wizardAttackCooldown <= 0) {
            val wizard = currentEnemies.find { it.type == EnemyType.WIZARD && !it.isDying }
            if (wizard != null) {
                val minionType = if (kotlin.random.Random.nextBoolean()) EnemyType.GOBLIN else EnemyType.SKELETON
                val minionHp = if (minionType == EnemyType.GOBLIN) 40 else 60
                val minionSpeed = if (minionType == EnemyType.GOBLIN) 7f else 5f
                val minionX = wizard.positionX + if (wizard.facingRight) 50f else -50f
                currentEnemies.add(
                    Enemy(
                        id = "minion_${System.currentTimeMillis()}_${kotlin.random.Random.nextInt(10000)}",
                        positionX = minionX,
                        health = minionHp,
                        maxHealth = minionHp,
                        speed = minionSpeed,
                        type = minionType,
                        facingRight = minionX < centerX
                    )
                )
                val wizIdx = currentEnemies.indexOfFirst { it.id == wizard.id }
                if (wizIdx >= 0) {
                    // Ponemos al mago a invocar con attackType = 2 y una duración de 15 ticks
                    currentEnemies[wizIdx] = wizard.copy(isAttacking = true, attackType = 2, attackTimer = 15)
                }
                wizardAttackCooldown = 50 + kotlin.random.Random.nextInt(40)
            }
        }

        // 5. Mover enemigos y ATACAR al jugador si está cerca
        val playerX = currentState.player.positionX
        val playerCenterX = playerX + 100f
        var totalPlayerDamage = 0
        
        val movedEnemies = currentEnemies.map { enemy ->
            if (enemy.isDying) return@map enemy
            
            val enemyWidth = when (enemy.type) {
                EnemyType.WIZARD -> 240f
                EnemyType.GOBLIN -> 160f
                EnemyType.SKELETON -> 140f
            }
            // Compensar centro visual: como el dibujo no simétrico tiene el arma y el frente
            // ocupando otro espacio distinto a la espalda, al girarlos cambia su punto central efectivo
            val centerOffset = if (enemy.facingRight) -25f else 25f
            val enemyCenterX = enemy.positionX + (enemyWidth / 2f) + centerOffset
            val distToPlayer = kotlin.math.abs(enemyCenterX - playerCenterX)
            val attackRange = (enemyWidth / 2f) - 15f
            
            var newAttackTimer = enemy.attackTimer
            var newIsAttacking = enemy.isAttacking
            var newAttackType = enemy.attackType
            var newPosX = enemy.positionX
            var newFacing = enemy.facingRight

            if (newIsAttacking) {
                // Ya está en proceso de ataque (wind-up o impacto)
                newAttackTimer--
                if (newAttackTimer == 3 && newAttackType == 1) { // El impacto ocurre casi al final de la animación de ataque cuerpo a cuerpo
                    if (distToPlayer < attackRange + 20f) { // Un poco de margen para el impacto
                        totalPlayerDamage += if (enemy.type == EnemyType.WIZARD) 30 else 15
                    }
                } else if (newAttackTimer <= 0) {
                    newIsAttacking = false
                    newAttackType = 1
                    newAttackTimer = -10 // Cooldown de 1 segundo antes de poder volver a atacar
                }
            } else if (newAttackTimer < 0) {
                // En cooldown
                newAttackTimer++
            } else if (distToPlayer < attackRange) {
                // Iniciar ataque cuerpo a cuerpo (tipo 1 y 15 ticks)
                newIsAttacking = true
                newAttackTimer = 15 // 1.5 segundos de animación a 10fps
                newAttackType = 1
            } else {
                // Moverse hacia el centro
                val targetX = centerX
                val distToTarget = kotlin.math.abs(enemy.positionX - targetX)
                if (distToTarget > 60f) {
                    val dir = if (targetX > enemy.positionX) 1 else -1
                    newPosX = enemy.positionX + (dir * enemy.speed)
                    newFacing = dir > 0
                }
            }

            enemy.copy(
                positionX = newPosX,
                facingRight = newFacing,
                isAttacking = newIsAttacking,
                attackType = newAttackType,
                attackTimer = newAttackTimer
            )
        }

        // 6. Lógica de Aldeanos
        val updatedVillagers = currentState.villagers.map { v ->
            var newPos = v.positionX
            var newTarget = v.targetX
            var moving = v.isMoving

            if (kotlin.math.abs(v.positionX - v.targetX) < 5f) {
                if (isDay) {
                    newTarget = 900f + (kotlin.random.Random.nextFloat() * 600f)
                    moving = true
                } else {
                    moving = false
                }
            } else {
                val dir = if (v.targetX > v.positionX) 1 else -1
                newPos += dir * 2f
                moving = true
            }
            v.copy(positionX = newPos, targetX = newTarget, isMoving = moving)
        }

        // 7. Combate: el jugador hace daño a enemigos cercanos cuando ataca
        val baseDamage = 15
        val archerBonus = if (currentState.player.hasArchers) 10 else 0
        val damage = ((baseDamage + archerBonus) * currentState.player.damageMultiplier).toInt()
        var killedCount = 0
        var wizardKilled = false
        val isPlayerAttacking = pendingPlayerAttack
        if (pendingPlayerAttack) pendingPlayerAttack = false // Se consume el ataque

        val afterCombat = movedEnemies.map { enemy ->
            if (enemy.isDying) return@map enemy
            
            val enemyWidth = when (enemy.type) {
                EnemyType.WIZARD -> 240f
                EnemyType.GOBLIN -> 160f
                EnemyType.SKELETON -> 140f
            }
            val centerOffset = if (enemy.facingRight) -25f else 25f
            val enemyCenterX = enemy.positionX + (enemyWidth / 2f) + centerOffset
            val dist = kotlin.math.abs(enemyCenterX - playerCenterX)
            val playerAttackRange = (enemyWidth / 2f) + 120f // Rango aumentado de 70 a 120
            
            if (dist < playerAttackRange && isPlayerAttacking) {
                val newHp = enemy.health - damage
                if (newHp <= 0) {
                    killedCount++
                    if (enemy.type == EnemyType.WIZARD) wizardKilled = true
                    enemy.copy(health = 0, isDying = true)
                } else {
                    enemy.copy(health = newHp)
                }
            } else {
                enemy
            }
        }

        if (killedCount > 0) {
            repository.addKills(killedCount)
            repository.addGold(5 * killedCount)
        }

        // Eliminar enemigos (los muertos desaparecen rápido por ahora)
        val remainingEnemies = afterCombat.filter { !it.isDying }

        // Actualizar vida del jugador
        var newPlayerHp = currentState.player.currentHp - totalPlayerDamage
        var gameOver = false
        if (newPlayerHp <= 0) {
            newPlayerHp = 0
            gameOver = true
        }

        val syncedGold = repository.getCurrentGold()

        _gameState.value = currentState.copy(
            timeOfDay = nextTime,
            day = nextDay,
            wave = nextWave,
            enemies = remainingEnemies,
            villagers = updatedVillagers,
            player = (_gameState.value?.player ?: currentState.player).copy(
                gold = syncedGold,
                currentHp = newPlayerHp
            ),
            isGameOver = gameOver,
            wizardKills = currentState.wizardKills + if (wizardKilled) 1 else 0
        )
    }

    fun movePlayer(deltaX: Float) {
        val currentState = _gameState.value ?: return
        
        // Límites sincronizados con el ancho real del mundo (5000dp)
        var newX = currentState.player.positionX + deltaX
        if (newX < 0f) newX = 0f
        if (newX > 5000f) newX = 5000f
        
        _gameState.value = currentState.copy(
            player = currentState.player.copy(
                positionX = newX,
                isMoving = deltaX != 0f,
                direction = if (deltaX > 0) 1 else -1
            )
        )
    }

    fun dashPlayer(direction: Float) {
        val currentState = _gameState.value ?: return
        
        viewModelScope.launch {
            val totalDistance = 150f
            val frames = 15
            val stepDistance = totalDistance / frames
            
            for (i in 0 until frames) {
                val state = _gameState.value ?: return@launch
                var newX = state.player.positionX + (direction * stepDistance)
                if (newX < 0f) newX = 0f
                if (newX > 5000f) newX = 5000f
                
                _gameState.value = state.copy(
                    player = state.player.copy(
                        positionX = newX,
                        isMoving = true,
                        direction = if (direction > 0) 1 else -1
                    )
                )
                kotlinx.coroutines.delay(16) // ~60fps
            }
        }
    }

    fun spendGold(amount: Int): Boolean {
        if (repository.spendGold(amount)) {
            val currentState = _gameState.value ?: return false
            _gameState.value = currentState.copy(
                player = currentState.player.copy(gold = repository.getCurrentGold())
            )
            return true
        }
        return false
    }

    fun saveProgress(slotId: String) {
        val state = _gameState.value ?: return
        viewModelScope.launch {
            repository.saveGameProgress(slotId, state.day, state.player.positionX, state.timeOfDay)
        }
    }
}