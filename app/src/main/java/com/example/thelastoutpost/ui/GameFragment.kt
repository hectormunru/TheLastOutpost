package com.example.thelastoutpost.ui

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import com.example.thelastoutpost.R
import com.example.thelastoutpost.databinding.FragmentGameBinding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.thelastoutpost.model.BuildingType
import com.example.thelastoutpost.model.EnemyType
import timber.log.Timber
import com.example.thelastoutpost.viewmodel.GameViewModel
import android.widget.ImageView
import android.widget.FrameLayout
import android.view.Gravity
import com.example.thelastoutpost.repository.GameRepository

/**
 * Fragment de Juego con animación de sprites del caballero
 * y movimiento continuo al mantener pulsados los botones.
 */
class GameFragment : Fragment() {

    private val gameViewModel: GameViewModel by activityViewModels {
        val application = requireActivity().application as com.example.thelastoutpost.TheLastOutpostApplication
        GameViewModel.Factory(application.repository)
    }
    private val args: GameFragmentArgs by navArgs()

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private var spriteAnimator: SpriteAnimator? = null

    // Mapa de vistas de enemigos activos para reciclar/actualizar/eliminar
    private val enemyViews = mutableMapOf<String, EnemySpriteView>()
    

    // Movimiento continuo
    private val moveHandler = Handler(Looper.getMainLooper())
    private var isMovingLeft = false
    private var isMovingRight = false
    private var isAttacking = false
    private var isDashing = false
    private var dashTicksLeft = 0
    private var dashDirection = 1f
    private val moveIntervalMs = 16L     // ~60fps

    private val moveRunnable = object : Runnable {
        override fun run() {
            val state = gameViewModel.gameState.value ?: return
            val speed = state.player.speed
            val playerX = state.player.positionX
            val dSpeed = speed * 3f

            if (isDashing) {
                if (dashTicksLeft > 0) {
                    gameViewModel.movePlayer(dashDirection * dSpeed)
                    dashTicksLeft--
                    moveHandler.postDelayed(this, moveIntervalMs)
                }
                return
            }
            if (isAttacking) return // No mover durante ataque

            if (isMovingLeft) {
                if (playerX > 0f) {
                    spriteAnimator?.facingRight = false
                    spriteAnimator?.play("walk")
                    gameViewModel.movePlayer(-speed)
                } else {
                    spriteAnimator?.play("idle")
                }
            } else if (isMovingRight) {
                if (playerX < 5000f) {
                    spriteAnimator?.facingRight = true
                    spriteAnimator?.play("walk")
                    gameViewModel.movePlayer(speed)
                } else {
                    spriteAnimator?.play("idle")
                }
            }

            if (isMovingLeft || isMovingRight) {
                moveHandler.postDelayed(this, moveIntervalMs)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        binding.viewModel = gameViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperar Identificador de Slot y Nombre
        val prefs = requireContext().getSharedPreferences("game_stats", android.content.Context.MODE_PRIVATE)
        val slotId = prefs.getString("active_slot", null)
        val playerName = args.playerName ?: "Héroe"
        


        if (slotId != null) {
            gameViewModel.loadSlot(slotId)
            // Usar el hash del slotId como semilla para que las piedras/hierba no cambien de sitio
            binding.vEnvironment.setSeed(slotId.hashCode().toLong())
        }

        // Inicializar el animator con las animaciones del Fire Warrior (Sprite Sheets individuales)
        spriteAnimator = SpriteAnimator(requireContext(), fps = 12).apply {
            addAnimation("idle", R.drawable.fw_idle, cols = 6, frameCount = 5)
            addAnimation("walk", R.drawable.fw_walk, cols = 8, offsetY = -10)
            addAnimation("attack", R.drawable.fw_attack, cols = 5, frameCount = 4, rowIndex = 0, totalRows = 3, offsetY = -20)
            addAnimation("dash", R.drawable.fw_dash, cols = 7)
            addAnimation("die", R.drawable.fw_die, cols = 11)
            attachTo(binding.ivPlayer)
            play("idle")
        }

        // Botón IZQUIERDA: mantener para mover continuamente
        binding.btnLeft.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (!isAttacking && !isDashing) {
                        isMovingLeft = true
                        moveHandler.removeCallbacks(moveRunnable)
                        moveHandler.post(moveRunnable)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isMovingLeft = false
                    if (!isMovingRight && !isAttacking && !isDashing) {
                        spriteAnimator?.play("idle")
                    }
                    true
                }
                else -> false
            }
        }

        // Botón DERECHA: mantener para mover continuamente
        binding.btnRight.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (!isAttacking && !isDashing) {
                        isMovingRight = true
                        moveHandler.removeCallbacks(moveRunnable)
                        moveHandler.post(moveRunnable)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isMovingRight = false
                    if (!isMovingLeft && !isAttacking && !isDashing) {
                        spriteAnimator?.play("idle")
                    }
                    true
                }
                else -> false
            }
        }

        // Botón ATACAR: reproduce la animación de ataque una sola vez
        binding.btnAttack.setOnClickListener {
            if (isAttacking || isDashing) return@setOnClickListener
            Timber.d("¡Ataque lanzado!")
            isAttacking = true
            binding.btnAttack.isEnabled = false
            gameViewModel.setPlayerAttacking(true)
            
            val originalFps = spriteAnimator?.fps ?: 12
            spriteAnimator?.fps = 36 // Animación 3 veces más rápida

            spriteAnimator?.playOnce("attack") {
                // Al terminar el ataque, volvemos a idle o walk
                spriteAnimator?.fps = originalFps
                
                isAttacking = false
                gameViewModel.setPlayerAttacking(false)
                binding.btnAttack.isEnabled = true
                when {
                    isMovingLeft || isMovingRight -> {
                        spriteAnimator?.play("walk")
                        moveHandler.post(moveRunnable)
                    }
                    else -> spriteAnimator?.play("idle")
                }
            }
        }

        // Botón DASH: reproduce dash y avanza de forma fluida
        binding.btnDash.setOnClickListener {
            if (isAttacking || isDashing) return@setOnClickListener
            isDashing = true
            binding.btnDash.isEnabled = false
            
            val state = gameViewModel.gameState.value
            // Dash más largo si tiene la mejora spd_3
            val hasDashUpgrade = state?.buildings?.any { false } ?: false // Error de lógica, buscar en repository o state
            // Mejor: consultar al repository a través del viewmodel si es necesario, 
            // pero ya tenemos flags en PlayerState!
            val dashTicks = if (state?.player?.speed ?: 5f > 10f || true) {
                 // Vamos a simplificar: si la velocidad es la máxima (10), dash largo
                 if (state?.player?.speed ?: 5f >= 10f) 22 else 14
            } else 14
            
            dashDirection = if (spriteAnimator?.facingRight == true) 1f else -1f
            dashTicksLeft = dashTicks

            // Iniciamos el bucle manual
            moveHandler.removeCallbacks(moveRunnable)
            moveHandler.post(moveRunnable)

            spriteAnimator?.playOnce("dash") {
                isDashing = false
                dashTicksLeft = 0
                binding.btnDash.isEnabled = true
                when {
                    isMovingLeft || isMovingRight -> {
                        spriteAnimator?.play("walk")
                        moveHandler.post(moveRunnable)
                    }
                    else -> spriteAnimator?.play("idle")
                }
            }
        }

        // Navegación desde el HUD del juego
        binding.btnAchievementsInGame.setOnClickListener {
            findNavController().navigate(GameFragmentDirections.actionGameFragmentToAchievementsFragment())
        }
        binding.btnStatsInGame.setOnClickListener {
            findNavController().navigate(GameFragmentDirections.actionGameFragmentToStatisticsFragment())
        }
        binding.btnSettingsInGame.setOnClickListener {
            findNavController().navigate(GameFragmentDirections.actionGameFragmentToSettingsFragment())
        }
        binding.btnSkillsInGame.setOnClickListener {
            findNavController().navigate(R.id.action_game_to_skills)
        }

        binding.btnSaveAndExit.setOnClickListener {
            val activeSlotId = requireContext().getSharedPreferences("game_stats", android.content.Context.MODE_PRIVATE)
                .getString("active_slot", null)
            
            if (activeSlotId != null) {
                gameViewModel.saveProgress(activeSlotId)
            }
            gameViewModel.finishGame()
            findNavController().navigateUp()
        }

        // Observar el estado del juego para actualizar la UI y entidades
        gameViewModel.gameState.observe(viewLifecycleOwner) { state ->
            renderGame(state)
        }
    }

    override fun onStart() {
        super.onStart()
        gameViewModel.refreshPlayerStats()
        gameViewModel.setPaused(false)
    }

    override fun onStop() {
        super.onStop()
        gameViewModel.setPaused(true)
        isMovingLeft = false
        isMovingRight = false
        moveHandler.removeCallbacks(moveRunnable)
        
        // Guardado de seguridad al salir o minimizar
        val activeSlotId = requireContext().getSharedPreferences("game_stats", android.content.Context.MODE_PRIVATE)
                .getString("active_slot", null)
        if (activeSlotId != null) {
            gameViewModel.saveProgress(activeSlotId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // No limpiamos el estado del ViewModel aquí porque al ir a Ajustes o Habilidades 
        // el Fragment se destruye y necesitamos conservar la partida. 
        // El ViewModel sobrevive (está asociado a la Activity).

        moveHandler.removeCallbacks(moveRunnable)
        spriteAnimator?.release()
        enemyViews.values.forEach { it.release() }
        enemyViews.clear()
        spriteAnimator = null
        _binding = null
    }

    private fun renderGame(state: com.example.thelastoutpost.model.GameState) {
        if (_binding == null) return
        if (state.isGameOver) {
            gameViewModel.finishGame()
            findNavController().navigateUp()
            return
        }

        val playerX = state.player.positionX

        // Evitar el "salto" a la esquina izquierda (x=0) antes de cargar el slot
        if (!gameViewModel.isLoaded) {
            binding.ivPlayer.visibility = View.INVISIBLE
            return
        } else {
            binding.ivPlayer.visibility = View.VISIBLE
        }

        // --- Renderizado Sincronizado ---
        val sw = binding.root.width
        if (sw > 0) {
            // Actualizar TODO junto para evitar lag entre player y cámara
            val playerXPx = playerX.dpToPx()
            binding.ivPlayer.translationX = playerXPx.toFloat()

            val playerSpriteCenterPx = playerXPx + (200.dpToPx() / 2) // Usamos valor fijo 200dp
            val targetTranslationX = (sw / 2f) - playerSpriteCenterPx
            
            val worldWidthPx = 5000.dpToPx()
            val minTranslationX = -(worldWidthPx - sw).toFloat()
            val finalTranslationX = targetTranslationX.coerceIn(minTranslationX, 0f)
            
            binding.flWorldContainer.translationX = finalTranslationX
            binding.ivMoon.translationX = finalTranslationX * 0.02f
            binding.vParallaxMountain.translationX = finalTranslationX * 0.05f
            binding.vParallaxBack.translationX = finalTranslationX * 0.15f
            binding.vParallaxMid.translationX = finalTranslationX * 0.30f
            binding.vParallaxShort.translationX = finalTranslationX * 0.60f

            // Minimapa
            val minimapWidth = binding.flMinimap.width
            if (minimapWidth > 0) {
                val relativePos = (playerX / 5000f) * (minimapWidth - binding.vMinimapPlayer.width)
                binding.vMinimapPlayer.translationX = relativePos
            }
        } else {
            // Si es el primer frame, forzamos un re-render en cuanto haya layout
            binding.root.post { 
                if (_binding != null) renderGame(state)
            }
        }

        // Aplicar Skin Dorada
        if (state.player.hasGoldenSkin) {
            binding.ivPlayer.setColorFilter(android.graphics.Color.parseColor("#44FFD700"), android.graphics.PorterDuff.Mode.SRC_ATOP)
        } else {
            binding.ivPlayer.clearColorFilter()
        }
        
        // Lógica de Noche
        val time = state.timeOfDay
        val nightAlpha = when {
            time < 20f -> 0f
            time < 30f -> (time - 20f) / 10f * 0.6f
            time < 130f -> 0.6f
            else -> 0.6f - (time - 130f) / 10f * 0.6f
        }
        binding.vNightOverlay.alpha = nightAlpha
        
        renderEnemies(state.enemies, playerX)
    }

    private fun renderEnemies(enemies: List<com.example.thelastoutpost.model.Enemy>, playerX: Float) {
        if (_binding == null) return
        val currentIds = enemies.map { it.id }.toSet()

        val toRemove = enemyViews.keys.filter { it !in currentIds }
        toRemove.forEach { id ->
            val view = enemyViews.remove(id)
            view?.release()
            binding.flEntities.removeView(view)
        }

        enemies.forEach { enemy ->
            var view = enemyViews[enemy.id]
            if (view == null) {
                view = createEnemyView(enemy)
                enemyViews[enemy.id] = view
                binding.flEntities.addView(view)
            }

            val xPx = enemy.positionX.dpToPx()
            view.translationX = xPx
            view.facingRight = enemy.facingRight
            view.showHealthBar = true
            view.healthPercent = enemy.health.toFloat() / enemy.maxHealth.toFloat()

            if (enemy.isAttacking) {
                if (view.currentAnimationName != "attack" && view.currentAnimationName != "attack1" && view.currentAnimationName != "attack2") {
                    val atkName = if (enemy.type == com.example.thelastoutpost.model.EnemyType.WIZARD) {
                        if (enemy.attackType == 2) "attack2" else "attack1"
                    } else "attack"
                    view.playOnce(atkName)
                }
            } else {
                val dist = kotlin.math.abs(enemy.positionX - playerX)
                if (dist > 60f) {
                    val walkName = when (enemy.type) {
                        com.example.thelastoutpost.model.EnemyType.WIZARD -> "run"
                        com.example.thelastoutpost.model.EnemyType.SKELETON -> "walk"
                        com.example.thelastoutpost.model.EnemyType.GOBLIN -> "run"
                    }
                    if (view.currentAnimationName != walkName) view.play(walkName)
                } else {
                    if (view.currentAnimationName != "idle") view.play("idle")
                }
            }
        }
    }

    private fun createEnemyView(enemy: com.example.thelastoutpost.model.Enemy): EnemySpriteView {
        val ctx = requireContext()
        val sizeDp: Int
        val marginBottomDp: Int

        when (enemy.type) {
            com.example.thelastoutpost.model.EnemyType.WIZARD -> {
                sizeDp = 240
                marginBottomDp = 40
            }
            com.example.thelastoutpost.model.EnemyType.GOBLIN -> {
                sizeDp = 160
                marginBottomDp = 69
            }
            com.example.thelastoutpost.model.EnemyType.SKELETON -> {
                sizeDp = 140
                marginBottomDp = 75     
            }
        }

        val sizePx = sizeDp.dpToPx().toInt()
        val marginBottomPx = marginBottomDp.dpToPx().toInt()

        val view = EnemySpriteView(ctx, sizePx, sizePx, fps = 6)
        val lp = FrameLayout.LayoutParams(sizePx, sizePx).apply {
            gravity = Gravity.BOTTOM or Gravity.START
            bottomMargin = marginBottomPx
        }
        view.layoutParams = lp

        when (enemy.type) {
            com.example.thelastoutpost.model.EnemyType.WIZARD -> {
                loadSheet(view, "idle", R.drawable.wizard_idle, 8)
                loadSheet(view, "attack1", R.drawable.wizard_attack1, 8)
                loadSheet(view, "attack2", R.drawable.wizard_attack2, 8)
                loadSheet(view, "run", R.drawable.wizard_run, 8)
                loadSheet(view, "death", R.drawable.wizard_death, 7)
                loadSheet(view, "takehit", R.drawable.wizard_takehit, 3)
            }
            com.example.thelastoutpost.model.EnemyType.GOBLIN -> {
                loadSheet(view, "idle", R.drawable.goblin_idle, 4)
                loadSheet(view, "run", R.drawable.goblin_run, 8)
                loadSheet(view, "attack", R.drawable.goblin_attack, 8)
                loadSheet(view, "death", R.drawable.goblin_death, 4)
            }
            com.example.thelastoutpost.model.EnemyType.SKELETON -> {
                loadSheet(view, "idle", R.drawable.skeleton_idle, 4)
                loadSheet(view, "walk", R.drawable.skeleton_walk, 4)
                loadSheet(view, "attack", R.drawable.skeleton_attack, 8)
                loadSheet(view, "death", R.drawable.skeleton_death, 4)
            }
        }

        view.play("idle")
        return view
    }

    private fun loadSheet(view: EnemySpriteView, name: String, resId: Int, cols: Int) {
        val sheet = BitmapFactory.decodeResource(resources, resId)
        view.addAnim(name, sheet, cols)
    }

    private fun Float.dpToPx(): Float {
        val density = resources.displayMetrics.density
        return this * density
    }

    private fun Int.dpToPx(): Float {
        val density = resources.displayMetrics.density
        return this * density
    }
}
