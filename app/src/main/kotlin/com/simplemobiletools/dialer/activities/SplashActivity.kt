package com.simplemobiletools.dialer.activities

import android.content.Intent
import com.simplemobiletools.commons.activities.BaseSplashActivity
import com.simplemobiletools.dialer.SignupActivity

class SplashActivity : BaseSplashActivity() {
    override fun initActivity() {
        startActivity(Intent(this, SignupActivity::class.java))
        finish()
    }
}
