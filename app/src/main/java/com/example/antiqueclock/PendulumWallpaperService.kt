package com.example.antiqueclock

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.*
import android.os.Handler
import android.os.Looper
import java.util.Calendar
import kotlin.math.*

class PendulumWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = ClockEngine()

    inner class ClockEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var width = 0
        private var height = 0
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private var background: Bitmap? = null
        private var startTime = System.currentTimeMillis()

        private val drawFrame = object : Runnable {
            override fun run() {
                draw()
                if (visible) handler.postDelayed(this, 33L)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            super.onSurfaceChanged(holder, format, w, h)
            width = w
            height = h
            draw()
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible
            handler.removeCallbacks(drawFrame)
            if (visible) handler.post(drawFrame)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            visible = false
            handler.removeCallbacks(drawFrame)
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            visible = false
            handler.removeCallbacks(drawFrame)
            super.onDestroy()
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) render(canvas)
            } catch (_: Exception) {
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }
        }

        private fun render(canvas: Canvas) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            canvas.drawColor(Color.rgb(18, 13, 10))

            // Scale/crop the supplied portrait clock artwork to fill the screen.
            val bmp = background ?: BitmapFactory.decodeResource(resources, R.drawable.clock_preview)
                .also { background = it }
            val scale = max(w / bmp.width, h / bmp.height)
            val dstW = bmp.width * scale
            val dstH = bmp.height * scale
            val left = (w - dstW) / 2f
            val top = (h - dstH) / 2f
            canvas.drawBitmap(bmp, null, RectF(left, top, left + dstW, top + dstH), paint)

            // Overlay animated pendulum bob in the clock's lower case area.
            // Its position is proportional to the generated portrait artwork.
            val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
            val angle = Math.toRadians(11.0 * sin(2.0 * PI * elapsed / 2.2))
            val pivotX = w * 0.50f
            val pivotY = h * 0.585f
            val rodLength = h * 0.145f
            val bobX = pivotX + (sin(angle) * rodLength).toFloat()
            val bobY = pivotY + (cos(angle) * rodLength).toFloat()

            val rodPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(218, 164, 68); strokeWidth = max(2f, w * 0.004f)
                style = Paint.Style.STROKE
            }
            canvas.drawLine(pivotX, pivotY, bobX, bobY, rodPaint)
            val bobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(bobX - w * .012f, bobY - w * .012f, w * .075f,
                    intArrayOf(Color.rgb(255, 225, 145), Color.rgb(190, 111, 25), Color.rgb(74, 35, 10)),
                    null, Shader.TileMode.CLAMP)
            }
            canvas.drawCircle(bobX, bobY, w * .043f, bobPaint)

            // Real-time hands: placed over the clock dial in the artwork.
            val now = Calendar.getInstance()
            val seconds = now.get(Calendar.SECOND) + now.get(Calendar.MILLISECOND) / 1000f
            val minutes = now.get(Calendar.MINUTE) + seconds / 60f
            val hours = (now.get(Calendar.HOUR) % 12) + minutes / 60f
            val cx = w * 0.515f
            val cy = h * 0.335f
            val radius = min(w, h) * 0.105f
            drawHand(canvas, cx, cy, radius * .55f, Math.toRadians(hours * 30.0), w * .010f, Color.rgb(255, 204, 105))
            drawHand(canvas, cx, cy, radius * .82f, Math.toRadians(minutes * 6.0), w * .006f, Color.rgb(255, 220, 145))
            drawHand(canvas, cx, cy, radius * .88f, Math.toRadians(seconds * 6.0), w * .0025f, Color.rgb(205, 75, 35))
            canvas.drawCircle(cx, cy, w * .009f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(255, 205, 110) })
        }

        private fun drawHand(c: Canvas, cx: Float, cy: Float, length: Float, angle: Double, stroke: Float, color: Int) {
            val a = angle - PI / 2.0
            val x = cx + (cos(a) * length).toFloat()
            val y = cy + (sin(a) * length).toFloat()
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color; strokeWidth = stroke; strokeCap = Paint.Cap.ROUND
                style = Paint.Style.STROKE
            }
            c.drawLine(cx, cy, x, y, p)
        }
    }
}
