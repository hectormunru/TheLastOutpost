package com.example.thelastoutpost.ui

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import android.view.View

/**
 * Vista ligera que renderiza un sprite animado de un enemigo.
 * Soporta idle, walk/run, attack y death como sprite-sheets horizontales.
 * Incluye una barra de vida opcional (para el Wizard).
 */
class EnemySpriteView(
    context: Context,
    private val frameWidth: Int,
    private val frameHeight: Int,
    private val fps: Int = 8
) : View(context) {

    private data class AnimData(
        val frames: List<Bitmap>,
        val flippedFrames: List<Bitmap>,
        val count: Int
    )

    private val anims = mutableMapOf<String, AnimData>()
    private var currentName: String? = null
    val currentAnimationName: String? get() = currentName
    private var currentAnim: AnimData? = null
    private var frame = 0
    private var playing = false
    private var looping = true
    private var onEnd: (() -> Unit)? = null
    var facingRight = true

    // Health bar
    var showHealthBar = false
    var healthPercent = 1f
    private val healthBarBgPaint = Paint().apply { color = Color.DKGRAY }
    private val healthBarFgPaint = Paint().apply { color = Color.RED }
    private val healthBarBorderPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val handler = Handler(Looper.getMainLooper())
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)

    private val ticker = object : Runnable {
        override fun run() {
            if (!playing || currentAnim == null) return
            frame++
            if (frame >= currentAnim!!.count) {
                if (!looping) {
                    playing = false
                    frame = currentAnim!!.count - 1
                    invalidate()
                    onEnd?.invoke()
                    return
                }
                frame = 0
            }
            invalidate()
            handler.postDelayed(this, 1000L / fps)
        }
    }

    fun addAnim(name: String, sheet: Bitmap, cols: Int, frameCount: Int = cols) {
        val flipMat = Matrix().apply { preScale(-1f, 1f) }
        val fw = sheet.width / cols
        val fh = sheet.height

        val frames = (0 until frameCount).map { i ->
            val sx = i * fw
            val w = minOf(fw, sheet.width - sx)
            if (w > 0) Bitmap.createBitmap(sheet, sx, 0, w, fh)
            else Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
        val flipped = frames.map { Bitmap.createBitmap(it, 0, 0, it.width, it.height, flipMat, false) }
        anims[name] = AnimData(frames, flipped, frameCount)
    }

    fun play(name: String) {
        if (currentName == name && playing && looping) return
        val a = anims[name] ?: return
        handler.removeCallbacks(ticker)
        currentAnim = a
        currentName = name
        looping = true
        playing = true
        frame = 0
        invalidate()
        handler.postDelayed(ticker, 1000L / fps)
    }

    fun playOnce(name: String, cb: (() -> Unit)? = null) {
        val a = anims[name] ?: return
        handler.removeCallbacks(ticker)
        currentAnim = a
        currentName = name
        looping = false
        onEnd = cb
        playing = true
        frame = 0
        invalidate()
        handler.postDelayed(ticker, 1000L / fps)
    }

    fun stop() {
        playing = false
        handler.removeCallbacks(ticker)
    }

    fun release() {
        stop()
        onEnd = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val a = currentAnim ?: return
        val list = if (facingRight) a.frames else a.flippedFrames
        val bmp = list[frame.coerceIn(0, list.size - 1)]

        // Escalar sprite dejando espacio arriba para la barra de vida
        val availableHeight = height.toFloat() - 30f
        val scale = minOf(width.toFloat() / bmp.width, availableHeight / bmp.height)
        val dw = bmp.width * scale
        val dh = bmp.height * scale
        val left = (width - dw) / 2f
        val top = height - dh // Alineado a la parte inferior

        canvas.drawBitmap(bmp, null, RectF(left, top, left + dw, top + dh), paint)

        // Barra de vida (arriba del sprite, estilo mini)
        if (showHealthBar) {
            val barW = width * 0.35f // Menos ancha
            val barH = 10f           // Más gordita
            val barX = (width - barW) / 2f
            val barY = top - 12f     // Más pegada al enemigo

            canvas.drawRect(barX, barY, barX + barW, barY + barH, healthBarBgPaint)
            canvas.drawRect(barX, barY, barX + barW * healthPercent, barY + barH, healthBarFgPaint)
            canvas.drawRect(barX, barY, barX + barW, barY + barH, healthBarBorderPaint)
        }
    }
}
