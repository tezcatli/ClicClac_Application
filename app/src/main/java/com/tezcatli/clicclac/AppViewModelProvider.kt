package com.tezcatli.clicclac

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tezcatli.clicclac.ui.CameraViewModel
import com.tezcatli.clicclac.ui.ConfigCassetteViewModel
import com.tezcatli.clicclac.ui.ConfigViewModel
import com.tezcatli.clicclac.ui.EscrowedListViewModel
import com.tezcatli.clicclac.ui.PhotosViewModel


/**
 * Provides Factory to create instance of ViewModel for the entire Inventory app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ItemEditViewModel
        initializer {
            EscrowedListViewModel(
                clicClacApplication().container.escrowManager,
                clicClacApplication().container.secureTime
            )
        }

        initializer {
            ConfigViewModel(
                clicClacApplication().container.settingsRepository
            )
        }

        initializer {
            CameraViewModel(
                clicClacApplication().container.mainExecutor,
                clicClacApplication().container.escrowManager,
                clicClacApplication().container.settingsRepository,
                clicClacApplication().container.cameraManager,
                clicClacApplication().container.pendingPhotoNotificationManager,
                clicClacApplication().container.locationManager,
                clicClacApplication().container.appContext
            )
        }

        initializer {
            ConfigCassetteViewModel(
                clicClacApplication().container.settingsRepository,
                clicClacApplication().container.appContext
            )
        }

        initializer {
            PhotosViewModel(
                clicClacApplication().container.escrowManager,
                clicClacApplication().container.contentResolver
            )
        }

    }
}

fun CreationExtras.clicClacApplication(): CliClacApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CliClacApplication)

