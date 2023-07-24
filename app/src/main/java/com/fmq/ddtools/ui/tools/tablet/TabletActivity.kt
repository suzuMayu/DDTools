package com.fmq.ddtools.ui.tools.tablet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.fmq.ddtools.databinding.ActivityTabletBinding

class TabletActivity : AppCompatActivity() {


    private lateinit var binding: ActivityTabletBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.texts.start()
    }
}