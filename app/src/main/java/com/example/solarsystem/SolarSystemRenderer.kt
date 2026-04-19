package com.example.solarsystem

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SolarSystemRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private lateinit var square: Square

    private lateinit var sun: CelestialBody
    private lateinit var mercury: Planet
    private lateinit var venus: Planet
    private lateinit var earth: Planet
    private lateinit var mars: Planet
    private lateinit var moon: Moon

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)

    private var screenWidth = 0
    private var screenHeight = 0
    private var startTime = 0L
    private var galaxyTextureId = 0
    private var sunTextureId = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)

        square = Square()
        galaxyTextureId = createGalaxyTexture()
        square.setTextureId(galaxyTextureId)

        // Инициализируем небесные тела
        sun = CelestialBody(0.5f)
        sunTextureId = createSunTexture()
        sun.setTexture(sunTextureId)

        mercury = Planet(
            body = CelestialBody(0.08f).apply { setColor(0.8f, 0.8f, 0.8f) },
            orbitalRadius = 1.5f,
            orbitalSpeed = 0.05f
        )

        venus = Planet(
            body = CelestialBody(0.15f).apply { setColor(0.9f, 0.7f, 0.1f) },
            orbitalRadius = 2.3f,
            orbitalSpeed = 0.03f
        )

        earth = Planet(
            body = CelestialBody(0.15f).apply { setColor(0.2f, 0.6f, 1f) },
            orbitalRadius = 3.1f,
            orbitalSpeed = 0.02f
        )

        moon = Moon(
            body = CelestialBody(0.05f).apply { setColor(0.7f, 0.7f, 0.7f) },
            orbitalRadius = 0.4f,
            orbitalSpeed = 2f,
            tiltAngle = 90f // Перпендикулярно плоскости эклиптики
        )

        mars = Planet(
            body = CelestialBody(0.1f).apply { setColor(1f, 0.3f, 0.1f) },
            orbitalRadius = 4.2f,
            orbitalSpeed = 0.01f
        )

        startTime = System.currentTimeMillis()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        GLES20.glViewport(0, 0, width, height)

        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, aspect, 0.1f, 200f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Set up camera - отдалены от центра солнечной системы
        Matrix.setLookAtM(viewMatrix, 0, 0f, 5f, 12f, 0f, 0f, 0f, 0f, 1f, 0f)

        // Draw background square (far away)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, -30f)
        Matrix.scaleM(modelMatrix, 0, 30f, 30f, 1f)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        square.draw(mvpMatrix)

        val time = System.currentTimeMillis() - startTime

        // Рисуем Солнце в центре
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        sun.draw(mvpMatrix)

        // Рисуем планеты
        drawPlanet(mercury, time)
        drawPlanet(venus, time)
        drawPlanetWithMoon(earth, moon, time)
        drawPlanet(mars, time)
    }

    private fun drawPlanet(planet: Planet, time: Long) {
        val planetModel = planet.getModelMatrix(time)
        val mvMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)

        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, planetModel, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        planet.body.draw(mvpMatrix)
    }

    private fun drawPlanetWithMoon(planet: Planet, moon: Moon, time: Long) {
        val planetModel = planet.getModelMatrix(time)
        val mvMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)

        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, planetModel, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        planet.body.draw(mvpMatrix)

        // Рисуем Луну
        val moonModel = moon.getModelMatrix(planetModel, time)
        val moonMvMatrix = FloatArray(16)
        val moonMvpMatrix = FloatArray(16)

        Matrix.multiplyMM(moonMvMatrix, 0, viewMatrix, 0, moonModel, 0)
        Matrix.multiplyMM(moonMvpMatrix, 0, projectionMatrix, 0, moonMvMatrix, 0)

        moon.body.draw(moonMvpMatrix)
    }

    private fun createSunTexture(): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)

        val bitmap = createSunBitmap(256, 256)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        bitmap.recycle()
        return textureIds[0]
    }

    private fun createSunBitmap(width: Int, height: Int): android.graphics.Bitmap {
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val centerX = width / 2f
        val centerY = height / 2f
        val maxRadius = Math.min(width, height) / 2f

        for (y in 0 until height) {
            for (x in 0 until width) {
                val dx = x - centerX
                val dy = y - centerY
                val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                val normalized = Math.min(distance / maxRadius, 1f)

                // Солнечный градиент: белый центр -> жёлтый -> оранжевый -> чёрный
                val (r, g, b) = when {
                    normalized < 0.3f -> Triple(255, 255, 100) // Белый-жёлтый центр
                    normalized < 0.6f -> Triple(255, 200, 50)  // Жёлтый
                    normalized < 0.8f -> Triple(255, 150, 0)   // Оранжевый
                    else -> Triple((255 * (1 - normalized)).toInt(), (100 * (1 - normalized)).toInt(), 0) // Затухание
                }

                bitmap.setPixel(x, y, android.graphics.Color.rgb(r, g, b))
            }
        }

        return bitmap
    }

    private fun createGalaxyTexture(): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)

        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.galaxy)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        bitmap.recycle()
        return textureIds[0]
    }
}
