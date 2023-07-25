package com.fmq.ddtools.ui.tools.tablet

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fmq.ddtools.R
import com.fmq.ddtools.databinding.ActivityTabletBinding

class TabletActivity : AppCompatActivity() {


    private lateinit var binding: ActivityTabletBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setBackgroundDrawable(ColorDrawable(getColor(R.color.white)))
        binding.text.start()
    }
}