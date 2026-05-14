package com.example.thelastoutpost.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.example.thelastoutpost.R
import timber.log.Timber
import kotlin.random.Random

/**
 * Custom View que decora el mundo colocando elementos de hierba aleatorios
 * y otros objetos decorativos (barriles, rocas, arbustos) sin superponerse.
 */
class EnvironmentDecorationsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private data class Decoration(val bitmap: Bitmap, val x: Float, val y: Float)

    private val decorations = mutableListOf<Decoration>()
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private var groundPaint: Paint? = null
    private var groundBaselinePx = 0f
    private var isInitialized = false
    private var randomSeed: Long = 0L

    // Propiedades para la hoguera y troncos
    private val bonfireFrames = mutableListOf<Bitmap>()
    private var bonfireX = 0f
    private var bonfireY = 0f
    private val logs = mutableListOf<Decoration>()
    private var lastBonfireTime = 0L
    private val bonfireFrameDurationMs = 100L
    private var currentBonfireFrame = 0

    fun setSeed(seed: Long) {
        this.randomSeed = seed
        // Si ya se inicializó, re-inicializamos con la nueva semilla
        if (isInitialized) {
            initDecorations(width.toFloat(), height.toFloat())
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!isInitialized && w > 0 && h > 0) {
            initDecorations(w.toFloat(), h.toFloat())
            isInitialized = true
        }
    }

    private fun initDecorations(width: Float, height: Float) {
        val seededRandom = kotlin.random.Random(randomSeed)
        decorations.clear()
        logs.clear()
        bonfireFrames.clear()
        
        val options = BitmapFactory.Options().apply { inScaled = false }
        val density = resources.displayMetrics.density
        val scale = density * 2.5f

        // --- 1. CARGA Y ESCALADO DE RECURSOS ---
        
        val decoScale = scale * 0.7f
        fun scaleBmp(bmp: Bitmap): Bitmap = Bitmap.createScaledBitmap(bmp, (bmp.width * decoScale).toInt(), (bmp.height * decoScale).toInt(), false)

        val specialDecosRaw = listOf(
            R.drawable.barril, R.drawable.caja, 
            R.drawable.arbusto1, R.drawable.arbusto2, R.drawable.arbusto3, 
            R.drawable.piedra_grande,
            R.drawable.piedra1, R.drawable.piedra2, R.drawable.piedra3, R.drawable.piedra4, R.drawable.piedra5
        )

        val specialDecos = specialDecosRaw.map { id ->
            scaleBmp(BitmapFactory.decodeResource(resources, id, options))
        }

        val grassScale = scale * 0.70f
        fun scaleGrass(id: Int): Bitmap {
            val bmp = BitmapFactory.decodeResource(resources, id, options)
            return Bitmap.createScaledBitmap(bmp, (bmp.width * grassScale).toInt(), (bmp.height * grassScale).toInt(), false)
        }
        val grassShort1 = scaleGrass(R.drawable.hierba1)
        val grassShort2 = scaleGrass(R.drawable.hierba2)
        val grassTall = scaleGrass(R.drawable.hierba3)

        val sueloRaw = BitmapFactory.decodeResource(resources, R.drawable.img_suelo, options)
        val hogueraSheetRaw = BitmapFactory.decodeResource(resources, R.drawable.hoguera, options)
        val troncoRaw = BitmapFactory.decodeResource(resources, R.drawable.tronco, options)
        
        if (hogueraSheetRaw != null) {
            val cols = 5
            val rows = 8
            val frameWidth = hogueraSheetRaw.width / cols
            val frameHeight = hogueraSheetRaw.height / rows
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val x = col * frameWidth
                    val y = row * frameHeight
                    val frame = Bitmap.createBitmap(hogueraSheetRaw, x, y, frameWidth, frameHeight)
                    val bonfireScale = scale * 0.75f
                    val scaledFrame = Bitmap.createScaledBitmap(frame, (frameWidth * bonfireScale).toInt(), (frameHeight * bonfireScale).toInt(), false)
                    bonfireFrames.add(scaledFrame)
                }   
            }
        }
        
        val scaledTronco = troncoRaw?.let {
            val sTronco = scale * 0.5f
            Bitmap.createScaledBitmap(it, (it.width * sTronco).toInt(), (it.height * sTronco).toInt(), false)
        }

        // --- 2. CONFIGURACIÓN DEL SUELO (SHADER) ---
        groundBaselinePx = 120f * density
        val surfaceY = height - groundBaselinePx
        val groundScale = scale * 1.15f
        val scaledSuelo = Bitmap.createScaledBitmap(sueloRaw, (sueloRaw.width * groundScale).toInt(), (sueloRaw.height * groundScale).toInt(), false)
        
        val shader = BitmapShader(scaledSuelo, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
        val matrix = android.graphics.Matrix()
        matrix.setTranslate(0f, surfaceY)
        shader.setLocalMatrix(matrix)
        groundPaint = Paint().apply { this.shader = shader }

        // --- 3. SPAWN DE HIERBA (RELLENO CONTINUO) ---
        var currentX = 0f
        while (currentX < width) {
            val isTall = seededRandom.nextFloat() < 0.25f
            val grassBmp = if (isTall) grassTall else if (seededRandom.nextBoolean()) grassShort1 else grassShort2
            decorations.add(Decoration(grassBmp, currentX, surfaceY - grassBmp.height + (25f * density)))
            currentX += grassBmp.width.toFloat()
        }

        // --- 4. SPAWN DE OBJETOS ESPECIALES (CON COLISIÓN) ---
        val occupiedSpaces = mutableListOf<RectF>()
        
        if (bonfireFrames.isNotEmpty()) {
            val hFrame = bonfireFrames[0]
            bonfireX = (width / 2f) - (hFrame.width / 2f)
            bonfireY = surfaceY - hFrame.height
            occupiedSpaces.add(RectF(bonfireX, bonfireY, bonfireX + hFrame.width, bonfireY + hFrame.height))
            
            scaledTronco?.let { td ->
                val txLeft = bonfireX - td.width - 20f * density
                val tyLeft = surfaceY - td.height
                logs.add(Decoration(td, txLeft, tyLeft))
                occupiedSpaces.add(RectF(txLeft, tyLeft, txLeft + td.width, tyLeft + td.height))
                
                val txRight = bonfireX + hFrame.width + 20f * density
                val tyRight = surfaceY - td.height
                logs.add(Decoration(td, txRight, tyRight))
                occupiedSpaces.add(RectF(txRight, tyRight, txRight + td.width, tyRight + td.height))
            }
        }
        
        val maxDecos = (width / (100 * scale)).toInt() 

        for (i in 0 until maxDecos) {
            val bmp = specialDecos[seededRandom.nextInt(specialDecos.size)]
            var placed = false
            var attempts = 0
            
            while (!placed && attempts < 20) {
                val x = seededRandom.nextFloat() * (width - bmp.width)
                val y = surfaceY - bmp.height 
                val newRect = RectF(x, y, x + bmp.width, y + bmp.height)
                val hasOverlap = occupiedSpaces.any { RectF.intersects(it, newRect) }
                
                if (!hasOverlap) {
                    decorations.add(Decoration(bmp, x, y))
                    occupiedSpaces.add(newRect)
                    placed = true
                }
                attempts++
            }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        groundPaint?.let {
            canvas.drawRect(0f, height - groundBaselinePx, width.toFloat(), height.toFloat(), it)
        }
        for (deco in decorations) {
            canvas.drawBitmap(deco.bitmap, deco.x, deco.y, paint)
        }
        for (log in logs) {
            canvas.drawBitmap(log.bitmap, log.x, log.y, paint)
        }
        if (bonfireFrames.isNotEmpty()) {
            val now = System.currentTimeMillis()
            if (now - lastBonfireTime >= bonfireFrameDurationMs) {
                currentBonfireFrame = (currentBonfireFrame + 1) % bonfireFrames.size
                lastBonfireTime = now
            }
            val frame = bonfireFrames[currentBonfireFrame]
            canvas.drawBitmap(frame, bonfireX, bonfireY, paint)
            postInvalidateOnAnimation()
        }
    }
}