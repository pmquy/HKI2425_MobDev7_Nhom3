package com.example.facebook

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.facebook.ui.FacebookApp
import com.example.facebook.ui.theme.FacebookTheme

class MainActivity : ComponentActivity() {

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission is granted
        } else {
            // Permission is denied
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) -> {

            }
            else -> {
                permissionRequest.launch(android.Manifest.permission.CAMERA)
            }
        }

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) -> {

            }
            else -> {
                permissionRequest.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {

            }
            else -> {
                permissionRequest.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        when(PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) -> {

            }
            else -> {
                permissionRequest.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge()
        setContent {
            FacebookTheme {
                FacebookApp()
            }
        }
    }
}
