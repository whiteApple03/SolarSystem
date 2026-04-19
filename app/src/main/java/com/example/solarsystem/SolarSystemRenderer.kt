package com.example.solarsystem

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

data class PlanetData(val name: String, val planet: Planet)

class SolarSystemRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private lateinit var square: Square
    private lateinit var selectionCube: SelectionCube

    private lateinit var sun: CelestialBody
    private val planetsList = mutableListOf<PlanetData>()
    private lateinit var moon: Moon

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)

    private var selectedPlanetIndex = 0
    private var startTime = 0L

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)

        square = Square()
        val textureId = createGalaxyTexture()
        square.setTextureId(textureId)

        sun = CelestialBody(0.5f)
        sun.setColor(1f, 1f, 0f)

        // Создаём планеты
        val mercury = Planet(
            body = CelestialBody(0.08f).apply { setColor(0.8f, 0.8f, 0.8f) },
            orbitalRadius = 1.5f,
            orbitalSpeed = 0.05f
        )

        val venus = Planet(
            body = CelestialBody(0.15f).apply { setColor(0.9f, 0.7f, 0.1f) },
            orbitalRadius = 2.3f,
            orbitalSpeed = 0.03f
        )

        val earth = Planet(
            body = CelestialBody(0.15f).apply { setColor(0.2f, 0.6f, 1f) },
            orbitalRadius = 3.1f,
            orbitalSpeed = 0.02f
        )

        moon = Moon(
            body = CelestialBody(0.05f).apply { setColor(0.7f, 0.7f, 0.7f) },
            orbitalRadius = 0.4f,
            orbitalSpeed = 2f,
            tiltAngle = 90f
        )

        val mars = Planet(
            body = CelestialBody(0.1f).apply { setColor(1f, 0.3f, 0.1f) },
            orbitalRadius = 4.2f,
            orbitalSpeed = 0.01f
        )

        planetsList.add(PlanetData("Mercury", mercury))
        planetsList.add(PlanetData("Venus", venus))
        planetsList.add(PlanetData("Earth", earth))
        planetsList.add(PlanetData("Mars", mars))

        selectionCube = SelectionCube(0.3f)
        startTime = System.currentTimeMillis()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, aspect, 0.1f, 200f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setLookAtM(viewMatrix, 0, 0f, 5f, 12f, 0f, 0f, 0f, 0f, 1f, 0f)

        // Фон
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, -30f)
        Matrix.scaleM(modelMatrix, 0, 30f, 30f, 1f)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        square.draw(mvpMatrix)

        val time = System.currentTimeMillis() - startTime

        // Солнце
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        sun.draw(mvpMatrix)

        // Планеты
        for ((index, planetData) in planetsList.withIndex()) {
            val isSelected = (index == selectedPlanetIndex)
            drawPlanetWithSelection(planetData.planet, time, isSelected)
        }

        // Земля с Луной
        if (selectedPlanetIndex == 2) {
            drawMoonForEarth(planetsList[2].planet, time)
        }
    }

    private fun drawPlanetWithSelection(planet: Planet, time: Long, isSelected: Boolean) {
        val planetModel = planet.getModelMatrix(time)
        val mvMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)

        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, planetModel, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        planet.body.draw(mvpMatrix)

        if (isSelected) {
            selectionCube.draw(mvpMatrix)
        }
    }

    private fun drawMoonForEarth(earth: Planet, time: Long) {
        val earthModel = earth.getModelMatrix(time)
        val moonModel = moon.getModelMatrix(earthModel, time)
        val mvMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)

        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, moonModel, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        moon.body.draw(mvpMatrix)
    }

    fun selectPlanet(index: Int) {
        selectedPlanetIndex = index.coerceIn(0, planetsList.size - 1)
    }

    private fun createGalaxyTexture(): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)

        val bitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.galaxy)
        } catch (e: Exception) {
            // Fallback to procedural texture if file not found
            createGalaxyBitmap(512, 512)
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()

        return textureIds[0]
    }

    private fun createGalaxyBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val pixels = IntArray(width * height)

        for (i in pixels.indices) {
            val random = (i * 73856093) xor (i * 19349663)
            val value = (random and 0xFF)
            pixels[i] = android.graphics.Color.rgb(value / 2, value / 2, value)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}
