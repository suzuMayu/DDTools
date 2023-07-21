package com.fmq.ddtools

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class StartActivity : AppCompatActivity() {
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        handler = Handler(Looper.getMainLooper())
    }

    override fun onResume() {
        super.onResume()
        runnable = Runnable {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
        handler.postDelayed(runnable,1000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onStop() {
        super.onStop()
        this.finish()
    }
}