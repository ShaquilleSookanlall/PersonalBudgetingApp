package com.example.personalbudgetingapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val jump1 = AnimationUtils.loadAnimation(this, R.anim.jump1)
        val jump2 = AnimationUtils.loadAnimation(this, R.anim.jump2)
        val jump3 = AnimationUtils.loadAnimation(this, R.anim.jump3)
        val jump4 = AnimationUtils.loadAnimation(this, R.anim.jump4)

        findViewById<View>(R.id.dot1).startAnimation(jump1)
        findViewById<View>(R.id.dot2).startAnimation(jump2)
        findViewById<View>(R.id.dot3).startAnimation(jump3)
        findViewById<View>(R.id.dot4).startAnimation(jump4)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 5000)
    }
}