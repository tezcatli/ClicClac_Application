package com.tezcatli.clicclac

import android.content.Context
import com.google.android.gms.location.LocationServices

class LocationManager(val appContext: Context) {

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)


    companion object Keys {

        @Volatile
        private var Instance: LocationManager? = null

        fun getInstance(appContext: Context): LocationManager {
            return Instance ?: synchronized(this) {
                return LocationManager(appContext).also { Instance = it }
            }
        }

    }
}