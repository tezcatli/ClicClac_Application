package com.tezcatli.clicclac

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CliClacApplication : Application(), Configuration.Provider {

    @Inject lateinit var pendingPhotoNotificationManager: PendingPhotoNotificationManager


    override fun onCreate() {


        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )


        super.onCreate()

        pendingPhotoNotificationManager.scheduleNextNotification()

    }


    override fun getWorkManagerConfiguration(): Configuration {
        val myWorkerFactory = DelegatingWorkerFactory()
        myWorkerFactory.addFactory(NotificationWorkerFactory(pendingPhotoNotificationManager))
        // Add here other factories that you may need in your application

        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(myWorkerFactory)
            .build()
    }

    companion object {
        public val DO_CRYPTO = false
    }


}


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


