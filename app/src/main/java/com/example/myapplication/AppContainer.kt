package com.example.myapplication


import android.content.ContentResolver
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.myapplication.settings.SettingsRepository
import java.util.concurrent.Executor

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val escrowManager: EscrowManager
    val contentResolver : ContentResolver
    val dataStore : DataStore<Preferences>
    val settingsRepository : SettingsRepository
    val mainExecutor : Executor
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

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository.getInstance(context.dataStore)
    }
}

