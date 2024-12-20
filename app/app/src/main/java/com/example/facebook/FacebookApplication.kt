package com.example.facebook

import android.app.Application
import com.example.facebook.data.AppContainer
import com.example.facebook.data.DefaultAppContainer
import com.example.facebook.model.User
import com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable
import com.google.firebase.messaging.FirebaseMessaging

class FacebookApplication: Application() {
    lateinit var container: AppContainer
    var user: User = User()
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
        if(isGooglePlayServicesAvailable(applicationContext) == 1) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                container.userPreferenceRepository.setToken(it.result)
            }
        }
    }
}