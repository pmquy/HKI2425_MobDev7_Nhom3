package com.example.facebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.facebook.ui.FacebookApp
import com.example.facebook.ui.theme.FacebookTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            FacebookTheme {
                FacebookApp(startDestination = intent.getStringExtra("startDestination"))
            }
        }
    }
}
