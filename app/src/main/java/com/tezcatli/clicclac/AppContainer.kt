package com.tezcatli.clicclac


import android.content.ContentResolver
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tezcatli.clicclac.Camera.CameraManager
import com.tezcatli.clicclac.settings.SettingsRepository
import java.util.concurrent.Executor

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val escrowManager: EscrowManager
    val contentResolver : ContentResolver
    val dataStore : DataStore<Preferences>
    val settingsRepository : SettingsRepository
    val cameraManager : CameraManager
    val mainExecutor : Executor
    val appContext : Context
    val pendingPhotoNotificationManager : PendingPhotoNotificationManager
}

/**
 */
class AppDataContainer(private val context: Context) : AppContainer {

    override val escrowManager: EscrowManager by lazy {
        EscrowManager.getInstance(context)
    }

    override val contentResolver: ContentResolver
        get() = context.contentResolver

    override val dataStore: DataStore<Preferences>
        get() = context.dataStore

    override val mainExecutor: Executor
        get() = context.mainExecutor

    override val appContext: Context
        get() = context

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository.getInstance(context.dataStore)
    }

    override val cameraManager: CameraManager by lazy {
        CameraManager.getInstance(context)
    }

    override val pendingPhotoNotificationManager: PendingPhotoNotificationManager by lazy {
        PendingPhotoNotificationManager.getInstance(context, escrowManager)
    }
}


