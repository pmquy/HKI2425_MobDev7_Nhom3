package com.example.facebook

import android.app.Application
import com.example.facebook.data.AppContainer
import com.example.facebook.data.DefaultAppContainer
import com.example.facebook.model.User

class FacebookApplication: Application() {
    lateinit var container: AppContainer
    lateinit var user: User
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
    }
}