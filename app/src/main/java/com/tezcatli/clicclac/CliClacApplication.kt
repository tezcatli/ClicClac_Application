package com.tezcatli.clicclac

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

class CliClacApplication: Application() {
    //lateinit var escrowManager : EscrowManager

    lateinit var container: AppContainer

    //val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreate() {


        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )


        container = AppDataContainer(this)

        super.onCreate()


    }
}


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
