package com.example.solarsystem

import android.content.Context
import android.opengl.GLSurfaceView

class SolarSystemView(context: Context) : GLSurfaceView(context) {
    private var renderer: SolarSystemRenderer? = null

    init {
        setEGLContextClientVersion(2)
        renderer = SolarSystemRenderer(context)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun getRenderer(): SolarSystemRenderer? = renderer
}
