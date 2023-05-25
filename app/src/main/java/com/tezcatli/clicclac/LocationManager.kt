package com.tezcatli.clicclac

import android.content.Context
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationManager @Inject constructor(@ApplicationContext val appContext: Context) {

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

}