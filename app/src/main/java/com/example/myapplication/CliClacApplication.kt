package com.example.myapplication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CliClacApplication: Application() {
    lateinit var escrowManager : EscrowManager
}