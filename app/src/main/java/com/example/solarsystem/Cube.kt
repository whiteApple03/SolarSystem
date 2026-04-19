package com.example.solarsystem

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Cube {
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec3 vColor;
        varying vec3 fragColor;

        void main() {
            gl_Position = uMVPMatrix * vPosition;
            fragColor = vColor;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec3 fragColor;

        void main() {
            gl_FragColor = vec4(fragColor, 1.0);
        }
    """.trimIndent()

    private var program = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var mvpMatrixHandle = 0

    private val vertexCoords = floatArrayOf(
        // Front face
        -0.5f, -0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,
        -0.5f, 0.5f, 0.5f,
        // Back face
        -0.5f, -0.5f, -0.5f,
        -0.5f, 0.5f, -0.5f,
        0.5f, 0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        // Top face
        -0.5f, 0.5f, -0.5f,
        -0.5f, 0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,
        0.5f, 0.5f, -0.5f,
        // Bottom face
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, 0.5f,
        -0.5f, -0.5f, 0.5f,
        // Right face
        0.5f, -0.5f, -0.5f,
        0.5f, 0.5f, -0.5f,
        0.5f, 0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        // Left face
        -0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f, 0.5f,
        -0.5f, 0.5f, 0.5f,
        -0.5f, 0.5f, -0.5f
    )

    private val colors = floatArrayOf(
        // Red
        1f, 0f, 0f,
        1f, 0f, 0f,
        1f, 0f, 0f,
        1f, 0f, 0f,
        // Green
        0f, 1f, 0f,
        0f, 1f, 0f,
        0f, 1f, 0f,
        0f, 1f, 0f,
        // Blue
        0f, 0f, 1f,
        0f, 0f, 1f,
        0f, 0f, 1f,
        0f, 0f, 1f,
        // Yellow
        1f, 1f, 0f,
        1f, 1f, 0f,
        1f, 1f, 0f,
        1f, 1f, 0f,
        // Cyan
        0f, 1f, 1f,
        0f, 1f, 1f,
        0f, 1f, 1f,
        0f, 1f, 1f,
        // Magenta
        1f, 0f, 1f,
        1f, 0f, 1f,
        1f, 0f, 1f,
        1f, 0f, 1f
    )

    private val drawOrder = byteArrayOf(
        0, 1, 2, 0, 2, 3,
        4, 5, 6, 4, 6, 7,
        8, 9, 10, 8, 10, 11,
        12, 13, 14, 12, 14, 15,
        16, 17, 18, 16, 18, 19,
        20, 21, 22, 20, 22, 23
    )

    private var vertexBuffer: FloatBuffer
    private var colorBuffer: FloatBuffer
    private var drawListBuffer: ByteBuffer

    init {
        val bb = ByteBuffer.allocateDirect(vertexCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(vertexCoords)
        vertexBuffer.position(0)

        val colorBb = ByteBuffer.allocateDirect(colors.size * 4)
        colorBb.order(ByteOrder.nativeOrder())
        colorBuffer = colorBb.asFloatBuffer()
        colorBuffer.put(colors)
        colorBuffer.position(0)

        drawListBuffer = ByteBuffer.allocateDirect(drawOrder.size)
        drawListBuffer.put(drawOrder)
        drawListBuffer.position(0)

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also { prg ->
            GLES20.glAttachShader(prg, vertexShader)
            GLES20.glAttachShader(prg, fragmentShader)
            GLES20.glLinkProgram(prg)
        }

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        colorHandle = GLES20.glGetAttribLocation(program, "vColor")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)

        GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_FLOAT, false, 12, colorBuffer)
        GLES20.glEnableVertexAttribArray(colorHandle)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_BYTE, drawListBuffer)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
