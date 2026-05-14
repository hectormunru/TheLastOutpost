package com.example.thelastoutpost.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.annotation.DrawableRes

/**
 * Animator de sprites que soporta múltiples animaciones (idle, walk, attack, etc.)
 * y volteo horizontal para cambiar de dirección.
 *
 * Uso:
 *   1. Crear instancia con attachTo(imageView)
 *   2. Registrar animaciones con addAnimation(name, drawableRes, frameCount)
 *   3. Llamar play("idle") para iniciar un loop o playOnce("attack") para una sola vez
 */
class SpriteAnimator(
    private val context: Context,
    var fps: Int = 10
) {
    /** Datos internos de cada animación registrada */
    private data class AnimationData(
        val frames: List<android.graphics.drawable.Drawable>,
        val flippedFrames: List<android.graphics.drawable.Drawable>,
        val frameCount: Int
    )

    private val animations = mutableMapOf<String, AnimationData>()
    private var currentAnimName: String? = null
    private var currentAnim: AnimationData? = null

    private val handler = Handler(Looper.getMainLooper())
    private var currentFrame = 0
    private var isPlaying = false
    private var targetView: ImageView? = null
    private var onAnimationEnd: (() -> Unit)? = null
    private var playOnce = false

    // true = mirando a la derecha, false = mirando a la izquierda
    var facingRight = true
        set(value) {
            if (field != value) {
                field = value
                // Forzar actualización inmediata de la vista al girarse
                if (isPlaying && currentAnim != null) {
                    val anim = currentAnim!!
                    val frameList = if (value) anim.frames else anim.flippedFrames
                    val safeFrame = maxOf(0, currentFrame - 1)
                    targetView?.setImageDrawable(frameList[safeFrame])
                }
            }
        }

    /**
     * Registra una animación a partir de un sprite sheet.
     */
    fun addAnimation(
        name: String, 
        @DrawableRes drawableRes: Int, 
        cols: Int, 
        frameCount: Int = cols, 
        rowIndex: Int = 0, 
        totalRows: Int = 1,
        offsetY: Int = 0
    ) {
        val sheet = BitmapFactory.decodeResource(context.resources, drawableRes)
        val flipMatrix = Matrix().apply { preScale(-1f, 1f) }

        // El grid real de este asset pack es de 144x92 (o un máximo de 92px de alto)
        // El problema de "deslizar adelante" se daba porque las imágenes PNG están
        // recortadas (crop) sin rellenar la transparencia de la última columna.
        val baseFrameWidth = 144
        val baseFrameHeight = 92
        val rowHeight = sheet.height / totalRows
        
        val frames = (0 until frameCount).map { i ->
            val startX = i * baseFrameWidth
            val extractWidth = minOf(baseFrameWidth, sheet.width - startX)
            val extractHeight = minOf(rowHeight, sheet.height - (rowIndex * rowHeight))
            
            // Creamos un lienzo uniforme estándar para que todas las animaciones encajen igual en el UI
            val uniformBitmap = Bitmap.createBitmap(baseFrameWidth, baseFrameHeight, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(uniformBitmap)
            
            if (extractWidth > 0 && extractHeight > 0 && startX < sheet.width) {
                // Recorte crudo
                val slice = Bitmap.createBitmap(sheet, startX, rowIndex * rowHeight, extractWidth, extractHeight)
                // Centrado modificado: al estar la imagen recortada solo por la derecha en el último frame,
                // debemos mantener su anclaje a la izquierda en 0 para que no salte.
                val destRect = android.graphics.Rect(0, baseFrameHeight - extractHeight + offsetY, extractWidth, baseFrameHeight + offsetY)
                canvas.drawBitmap(slice, android.graphics.Rect(0, 0, extractWidth, extractHeight), destRect, null)
            }
            uniformBitmap
        }
        processFrames(name, frames, flipMatrix)
    }

    /**
     * Registra una animación a partir de una lista de frames individuales.
     */
    fun addAnimation(name: String, @DrawableRes drawableResList: List<Int>) {
        val flipMatrix = Matrix().apply { preScale(-1f, 1f) }
        val frames = drawableResList.map { resId ->
            BitmapFactory.decodeResource(context.resources, resId)
        }
        processFrames(name, frames, flipMatrix)
    }

    private fun processFrames(name: String, frames: List<Bitmap>, flipMatrix: Matrix) {
        val drawableFrames = frames.map { android.graphics.drawable.BitmapDrawable(context.resources, it) }
        val flippedFrames = frames.map { frame ->
            val flipped = Bitmap.createBitmap(frame, 0, 0, frame.width, frame.height, flipMatrix, false)
            // Ajustamos el sprite volteado para evitar que teletransporte al girar debido al offset gráfico
            val offsetFlipped = Bitmap.createBitmap(frame.width, frame.height, Bitmap.Config.ARGB_8888)
            val cv = android.graphics.Canvas(offsetFlipped)
            cv.drawBitmap(flipped, -16f, 0f, null) // Ajuste visual compensando el centro real
            android.graphics.drawable.BitmapDrawable(context.resources, offsetFlipped)
        }
        animations[name] = AnimationData(drawableFrames, flippedFrames, frames.size)
    }

    private val animRunnable = object : Runnable {
        override fun run() {
            if (!isPlaying || currentAnim == null) return

            val anim = currentAnim!!
            val frameList = if (facingRight) anim.frames else anim.flippedFrames
            targetView?.setImageDrawable(frameList[currentFrame])
            currentFrame++

            if (currentFrame >= anim.frameCount) {
                if (playOnce) {
                    isPlaying = false
                    currentFrame = 0
                    onAnimationEnd?.invoke()
                    return
                }
                currentFrame = 0
            }

            handler.postDelayed(this, (1000L / fps))
        }
    }

    /** Vincula el animator a un ImageView. */
    fun attachTo(imageView: ImageView) {
        targetView = imageView
    }

    /**
     * Reproduce una animación en bucle (ej. idle, walk).
     * Si ya está reproduciendo esa misma animación, no reinicia.
     */
    fun play(name: String) {
        if (currentAnimName == name && isPlaying && !playOnce) return
        val anim = animations[name] ?: return

        stop()
        currentAnim = anim
        currentAnimName = name
        playOnce = false
        isPlaying = true
        currentFrame = 0
        animRunnable.run()
    }

    /**
     * Reproduce una animación UNA sola vez (ej. attack).
     * @param name  Nombre de la animación
     * @param onEnd Callback al terminar
     */
    fun playOnce(name: String, onEnd: (() -> Unit)? = null) {
        val anim = animations[name] ?: return

        stop()
        currentAnim = anim
        currentAnimName = name
        playOnce = true
        onAnimationEnd = onEnd
        isPlaying = true
        currentFrame = 0
        animRunnable.run()
    }

    /** Para la animación actual. */
    fun stop() {
        isPlaying = false
        handler.removeCallbacks(animRunnable)
        currentFrame = 0
        currentAnimName = null
    }

    /** Muestra el primer frame de una animación sin reproducirla. */
    fun showFirstFrame(name: String) {
        val anim = animations[name] ?: return
        val frameList = if (facingRight) anim.frames else anim.flippedFrames
        targetView?.setImageDrawable(frameList[0])
    }

    /** Libera recursos. Llamar en onDestroyView(). */
    fun release() {
        stop()
        targetView = null
        onAnimationEnd = null
    }
}
