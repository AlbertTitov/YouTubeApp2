package com.newfarm.youtubeapp2.ui.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.newfarm.youtubeapp2.R

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SHOW_LOGO_TIME = 500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        val handler = Handler()
        handler.postDelayed({

            val intent = Intent(applicationContext, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.open_next, R.anim.close_main)

        }, SHOW_LOGO_TIME)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    .or(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                    .or(View.SYSTEM_UI_FLAG_FULLSCREEN)
                    .or(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }
        }
    }
}
