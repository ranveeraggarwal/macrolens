package com.ranveeraggarwal.macrolens

import android.app.Application

class MacroLensApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
