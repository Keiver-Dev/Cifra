package com.Vlynk.cifra

import android.app.Application
import com.Vlynk.cifra.di.AppContainer

class CifraApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
