package com.kamaraj.akashvanipitch

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val btnPowerOn = findViewById<Button>(R.id.btnPowerOn)
        btnPowerOn.setOnClickListener { startMainRadio() }

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) startMainRadio()
        }, 2500)
    }

    private fun startMainRadio() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}