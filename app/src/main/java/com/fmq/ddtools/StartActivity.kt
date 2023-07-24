package com.fmq.ddtools

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fmq.ddtools.utils.PublicConstants.Companion.SPLASH_TIME
import com.tencent.mmkv.MMKV
import kotlin.properties.Delegates


class StartActivity : AppCompatActivity() {
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var time by Delegates.notNull<Long>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        handler = Handler(Looper.getMainLooper())
        MMKV.initialize(this)
        time = MMKV.defaultMMKV().decodeLong("START_TIME")
        time = if (time == 0L) SPLASH_TIME else if (time == 1L) 0L else time
    }

    override fun onResume() {
        super.onResume()
        runnable = Runnable {
            startActivity(
                Intent(applicationContext, MainActivity::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()

            )
        }
        handler.postDelayed(runnable, time)
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