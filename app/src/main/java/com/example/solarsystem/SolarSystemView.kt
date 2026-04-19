package com.example.solarsystem

import android.content.Context
import android.opengl.GLSurfaceView

class SolarSystemView(context: Context) : GLSurfaceView(context) {
    init {
        setEGLContextClientVersion(2)
        setRenderer(SolarSystemRenderer(context))
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}
