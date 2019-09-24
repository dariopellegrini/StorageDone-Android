package com.dariopellegrini.storagedoneapp

import android.app.Application

class SDApplication: Application() {

    override fun onCreate() {
        super.onCreate()

//        CouchbaseLite.init(this)
    }
}