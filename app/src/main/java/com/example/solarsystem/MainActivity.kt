package com.example.solarsystem

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var selectedPlanet = 0
    private val planets = listOf("Mercury", "Venus", "Earth", "Mars")
    private lateinit var tvPlanetInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvPlanetInfo = findViewById(R.id.tvPlanetInfo)
        val btnLeft = findViewById<Button>(R.id.btnLeft)
        val btnRight = findViewById<Button>(R.id.btnRight)
        val btnInfo = findViewById<Button>(R.id.btnInfo)
        val glContainer = findViewById<FrameLayout>(R.id.glContainer)

        val solarView = SolarSystemView(this)
        glContainer.addView(solarView)

        val renderer = solarView.getRenderer()

        btnLeft.setOnClickListener {
            selectedPlanet = (selectedPlanet - 1 + planets.size) % planets.size
            tvPlanetInfo.text = planets[selectedPlanet]
            renderer?.selectPlanet(selectedPlanet)
        }

        btnRight.setOnClickListener {
            selectedPlanet = (selectedPlanet + 1) % planets.size
            tvPlanetInfo.text = planets[selectedPlanet]
            renderer?.selectPlanet(selectedPlanet)
        }

        btnInfo.setOnClickListener {
            showPlanetInfo()
        }

        tvPlanetInfo.text = planets[0]
    }

    private fun showPlanetInfo() {
        val info = when (planets[selectedPlanet]) {
            "Mercury" -> "Mercury\nДистанция: 57.9M км"
            "Venus" -> "Venus\nДистанция: 108.2M км"
            "Earth" -> "Earth\nДистанция: 149.6M км"
            "Mars" -> "Mars\nДистанция: 227.9M км"
            else -> planets[selectedPlanet]
        }
        tvPlanetInfo.text = info
    }
}