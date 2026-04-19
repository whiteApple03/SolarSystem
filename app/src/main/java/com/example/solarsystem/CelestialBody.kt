package com.example.solarsystem

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class CelestialBody(val radius: Float) {
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec3 vColor;
        attribute vec2 vTexCoord;
        varying vec3 fragColor;
        varying vec2 texCoord;

        void main() {
            gl_Position = uMVPMatrix * vPosition;
            fragColor = vColor;
            texCoord = vTexCoord;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec3 fragColor;
        varying vec2 texCoord;
        uniform sampler2D uTexture;
        uniform bool uUseTexture;

        void main() {
            if (uUseTexture) {
                gl_FragColor = texture2D(uTexture, texCoord);
            } else {
                gl_FragColor = vec4(fragColor, 1.0);
            }
        }
    """.trimIndent()

    private var program = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var texCoordHandle = 0
    private var mvpMatrixHandle = 0
    private var textureHandle = 0
    private var useTextureHandle = 0

    private var vertexBuffer: FloatBuffer
    private var colorBuffer: FloatBuffer
    private var texCoordBuffer: FloatBuffer
    private var vertexCount = 0
    private var textureId = 0
    private var useTexture = false

    init {
        val (vertices, texCoords) = generateSphere(radius, 12, 12)
        vertexCount = vertices.size / 3

        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        val colors = FloatArray(vertices.size)
        for (i in colors.indices step 3) {
            colors[i] = 1f
            colors[i + 1] = 1f
            colors[i + 2] = 1f
        }

        val colorBb = ByteBuffer.allocateDirect(colors.size * 4)
        colorBb.order(ByteOrder.nativeOrder())
        colorBuffer = colorBb.asFloatBuffer()
        colorBuffer.put(colors)
        colorBuffer.position(0)

        val texBb = ByteBuffer.allocateDirect(texCoords.size * 4)
        texBb.order(ByteOrder.nativeOrder())
        texCoordBuffer = texBb.asFloatBuffer()
        texCoordBuffer.put(texCoords)
        texCoordBuffer.position(0)

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also { prg ->
            GLES20.glAttachShader(prg, vertexShader)
            GLES20.glAttachShader(prg, fragmentShader)
            GLES20.glLinkProgram(prg)
        }

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        colorHandle = GLES20.glGetAttribLocation(program, "vColor")
        texCoordHandle = GLES20.glGetAttribLocation(program, "vTexCoord")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        useTextureHandle = GLES20.glGetUniformLocation(program, "uUseTexture")
    }

    fun setColor(r: Float, g: Float, b: Float) {
        val colors = FloatArray(vertexCount * 3) { i ->
            when (i % 3) {
                0 -> r
                1 -> g
                else -> b
            }
        }

        val colorBb = ByteBuffer.allocateDirect(colors.size * 4)
        colorBb.order(ByteOrder.nativeOrder())
        colorBuffer = colorBb.asFloatBuffer()
        colorBuffer.put(colors)
        colorBuffer.position(0)

        useTexture = false
    }

    fun setTexture(textureId: Int) {
        this.textureId = textureId
        this.useTexture = true
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)

        GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_FLOAT, false, 12, colorBuffer)
        GLES20.glEnableVertexAttribArray(colorHandle)

        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer)
        GLES20.glEnableVertexAttribArray(texCoordHandle)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform1i(useTextureHandle, if (useTexture) 1 else 0)

        if (useTexture) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glUniform1i(textureHandle, 0)
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun generateSphere(radius: Float, lats: Int, lons: Int): Pair<FloatArray, FloatArray> {
        val vertices = mutableListOf<Float>()
        val texCoords = mutableListOf<Float>()

        for (i in 0 until lats) {
            val lat0 = Math.PI * (-0.5 + (i.toFloat() / lats))
            val lat1 = Math.PI * (-0.5 + ((i + 1).toFloat() / lats))

            val sinLat0 = Math.sin(lat0).toFloat()
            val cosLat0 = Math.cos(lat0).toFloat()
            val sinLat1 = Math.sin(lat1).toFloat()
            val cosLat1 = Math.cos(lat1).toFloat()

            for (j in 0 until lons) {
                val lon0 = 2 * Math.PI * (j.toFloat() / lons)
                val lon1 = 2 * Math.PI * ((j + 1).toFloat() / lons)

                val sinLon0 = Math.sin(lon0).toFloat()
                val cosLon0 = Math.cos(lon0).toFloat()
                val sinLon1 = Math.sin(lon1).toFloat()
                val cosLon1 = Math.cos(lon1).toFloat()

                val u0 = j.toFloat() / lons
                val u1 = (j + 1).toFloat() / lons
                val v0 = i.toFloat() / lats
                val v1 = (i + 1).toFloat() / lats

                // First triangle
                vertices.add(cosLat0 * cosLon0 * radius)
                vertices.add(sinLat0 * radius)
                vertices.add(cosLat0 * sinLon0 * radius)
                texCoords.add(u0)
                texCoords.add(v0)

                vertices.add(cosLat1 * cosLon0 * radius)
                vertices.add(sinLat1 * radius)
                vertices.add(cosLat1 * sinLon0 * radius)
                texCoords.add(u0)
                texCoords.add(v1)

                vertices.add(cosLat1 * cosLon1 * radius)
                vertices.add(sinLat1 * radius)
                vertices.add(cosLat1 * sinLon1 * radius)
                texCoords.add(u1)
                texCoords.add(v1)

                // Second triangle
                vertices.add(cosLat0 * cosLon0 * radius)
                vertices.add(sinLat0 * radius)
                vertices.add(cosLat0 * sinLon0 * radius)
                texCoords.add(u0)
                texCoords.add(v0)

                vertices.add(cosLat1 * cosLon1 * radius)
                vertices.add(sinLat1 * radius)
                vertices.add(cosLat1 * sinLon1 * radius)
                texCoords.add(u1)
                texCoords.add(v1)

                vertices.add(cosLat0 * cosLon1 * radius)
                vertices.add(sinLat0 * radius)
                vertices.add(cosLat0 * sinLon1 * radius)
                texCoords.add(u1)
                texCoords.add(v0)
            }
        }

        return Pair(vertices.toFloatArray(), texCoords.toFloatArray())
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
