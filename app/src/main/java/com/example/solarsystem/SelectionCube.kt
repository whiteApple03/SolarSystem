package com.example.solarsystem

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class SelectionCube(val size: Float) {
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;

        void main() {
            gl_Position = uMVPMatrix * vPosition;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;

        void main() {
            gl_FragColor = vec4(0.0, 1.0, 1.0, 0.3);
        }
    """.trimIndent()

    private var program = 0
    private var positionHandle = 0
    private var mvpMatrixHandle = 0
    private var vertexBuffer: FloatBuffer

    private val vertexCoords = floatArrayOf(
        -size, -size, size,
        size, -size, size,
        size, size, size,
        -size, size, size,
        -size, -size, -size,
        size, -size, -size,
        size, size, -size,
        -size, size, -size
    )

    private val drawOrder = byteArrayOf(
        0, 1, 1, 2, 2, 3, 3, 0,
        4, 5, 5, 6, 6, 7, 7, 4,
        0, 4, 1, 5, 2, 6, 3, 7
    )

    init {
        val bb = ByteBuffer.allocateDirect(vertexCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(vertexCoords)
        vertexBuffer.position(0)

        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertexShader, vertexShaderCode)
        GLES20.glCompileShader(vertexShader)

        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode)
        GLES20.glCompileShader(fragmentShader)

        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        GLES20.glUseProgram(program)

        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        val indexBuffer = ByteBuffer.allocateDirect(drawOrder.size)
        indexBuffer.put(drawOrder)
        indexBuffer.position(0)

        GLES20.glDrawElements(GLES20.GL_LINES, drawOrder.size, GLES20.GL_UNSIGNED_BYTE, indexBuffer)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisable(GLES20.GL_BLEND)
    }
}
