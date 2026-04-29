package com.example.appforcooking

import android.app.Application
import android.content.Context
import android.util.Log


class CookingApplication : Application() {
    companion object {
        private lateinit var instance: CookingApplication

        fun getInstance(): CookingApplication {
            return instance
        }

        val appContext: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("CookingApplication", "Application инициализирован")
    }
}