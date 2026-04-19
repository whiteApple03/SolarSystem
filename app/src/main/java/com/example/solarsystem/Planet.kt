package com.example.solarsystem

import android.opengl.Matrix

data class Planet(
    val body: CelestialBody,
    val orbitalRadius: Float,
    val orbitalSpeed: Float,
    val rotationSpeed: Float = 0f
) {
    private val modelMatrix = FloatArray(16)

    fun getModelMatrix(time: Long): FloatArray {
        Matrix.setIdentityM(modelMatrix, 0)

        // Вращение вокруг центра (Солнца)
        val angle = (time * orbitalSpeed) % 360f
        Matrix.rotateM(modelMatrix, 0, angle, 0f, 1f, 0f)
        Matrix.translateM(modelMatrix, 0, orbitalRadius, 0f, 0f)

        // Собственное вращение планеты
        Matrix.rotateM(modelMatrix, 0, (time * rotationSpeed) % 360f, 0f, 1f, 0f)

        return modelMatrix.copyOf()
    }
}

data class Moon(
    val body: CelestialBody,
    val orbitalRadius: Float,
    val orbitalSpeed: Float,
    val tiltAngle: Float = 0f // Угол наклона плоскости орбиты
) {
    private val modelMatrix = FloatArray(16)

    fun getModelMatrix(parentModelMatrix: FloatArray, time: Long): FloatArray {
        Matrix.setIdentityM(modelMatrix, 0)

        // Применяем матрицу родительской планеты
        Matrix.multiplyMM(modelMatrix, 0, parentModelMatrix, 0, modelMatrix, 0)

        // Наклон плоскости орбиты
        Matrix.rotateM(modelMatrix, 0, tiltAngle, 1f, 0f, 0f)

        // Вращение вокруг планеты
        val angle = (time * orbitalSpeed) % 360f
        Matrix.rotateM(modelMatrix, 0, angle, 0f, 1f, 0f)
        Matrix.translateM(modelMatrix, 0, orbitalRadius, 0f, 0f)

        return modelMatrix.copyOf()
    }
}
