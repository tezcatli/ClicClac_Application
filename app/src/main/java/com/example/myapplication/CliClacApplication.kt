package com.example.myapplication

import android.app.Application
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CliClacApplication: Application() {
    lateinit var escrowManager : EscrowManager

    override fun onCreate() {


        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
        super.onCreate()

    }
}