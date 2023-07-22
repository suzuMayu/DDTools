package com.fmq.ddtools.utils

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.fmq.ddtools.R

object PublicUtils {
    fun addTitleIconBackBtn(activity: Activity):Boolean {
        (activity as AppCompatActivity).supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_return_back)
        }
        return true
    }
}