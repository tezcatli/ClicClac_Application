package com.example.myapplication

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.ui.CameraViewModel
import com.example.myapplication.ui.ConfigCassetteViewModel
import com.example.myapplication.ui.ConfigViewModel
import com.example.myapplication.ui.EscrowedListViewModel
import com.example.myapplication.ui.PhotosViewModel


/**
 * Provides Factory to create instance of ViewModel for the entire Inventory app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ItemEditViewModel
        initializer {
            EscrowedListViewModel(
                clicClacApplication().container.escrowManager,
                clicClacApplication().container.contentResolver
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
                clicClacApplication().container.settingsRepository
            )
        }

        initializer {
            ConfigCassetteViewModel(
                clicClacApplication().container.settingsRepository
            )
        }

        initializer {
            PhotosViewModel(
                clicClacApplication().container.escrowManager,
                clicClacApplication().container.contentResolver
            )
        }

        /*
        // Initializer for ItemEntryViewModel
        initializer {
            ItemEntryViewModel(inventoryApplication().container.itemsRepository)
        }

        // Initializer for ItemDetailsViewModel
        initializer {
            ItemDetailsViewModel(
                this.createSavedStateHandle(),
                inventoryApplication().container.itemsRepository
            )
        }

        // Initializer for HomeViewModel
        initializer {
            HomeViewModel(inventoryApplication().container.itemsRepository)
        }
        */
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [InventoryApplication].
 */
fun CreationExtras.clicClacApplication(): CliClacApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CliClacApplication)
