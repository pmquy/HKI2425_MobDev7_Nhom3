package com.example.facebook.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService(private val context: Context, private val content: String) {

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun requestSingleLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("LocationService", "Location permission not granted")
            return
        }
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                val speed = location.speed // Tốc độ di chuyển (m/s)
                Log.d("LocationService", "Current speed: $speed m/s")

                if (speed > 0) {
                    Log.d("LocationService", "Speed is greater than 1 m/s, starting ListenService")
                    val intent = Intent(context, ListenService::class.java).apply {
                        putExtra("content", content)
                    }
                    context.startService(intent)
                } else {
                    Log.d("LocationService", "Speed is less than 1 m/s, no action taken.")
                }
            } else {
                Log.d("LocationService", "Failed to get current location.")
            }
        }.addOnFailureListener { exception ->
            Log.d("LocationService", "Failed to get current location: ${exception.message}")
        }
    }



}
