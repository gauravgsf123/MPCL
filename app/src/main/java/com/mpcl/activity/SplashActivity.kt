package com.mpcl.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.mpcl.R
import com.mpcl.app.BaseActivity

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //throw RuntimeException("This is a crash");

        Handler().postDelayed(Runnable {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 1000)
    }
}