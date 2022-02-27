package com.mpcl.app

import android.app.Application
import com.mpcl.database.ObjectBox

class MyApp:Application() {
    private lateinit var app: MyApp

    override fun onCreate() {
        super.onCreate()
        app = this
        ObjectBox.init(this)
    }
}