package com.tezcatli.clicclac

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory

class CliClacApplication: Application(), Configuration.Provider {
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

        container.pendingPhotoNotificationManager.scheduleNextNotification()

        super.onCreate()


    }


    override fun getWorkManagerConfiguration(): Configuration {
        val myWorkerFactory = DelegatingWorkerFactory()
        myWorkerFactory.addFactory(NotificationWorkerFactory(container.pendingPhotoNotificationManager))
        // Add here other factories that you may need in your application

        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(myWorkerFactory)
            .build()
    }


}


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


