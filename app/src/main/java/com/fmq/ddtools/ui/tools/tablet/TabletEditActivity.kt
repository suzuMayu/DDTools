package com.fmq.ddtools.ui.tools.tablet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.fmq.ddtools.databinding.ActivityTabletBinding
import com.fmq.ddtools.databinding.ActivityTabletEditBinding
import com.fmq.ddtools.utils.PublicUtils

class TabletEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTabletEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabletEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.te.setOnClickListener {
            startActivity(Intent(this, TabletActivity::class.java))
        }
        PublicUtils.addTitleIconBackBtn(this)
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}