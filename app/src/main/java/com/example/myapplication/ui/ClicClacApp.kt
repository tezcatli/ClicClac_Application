package com.example.myapplication.ui

import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

interface NavigationDestination {
    /**
     * Unique name to define the path for a composable
     */
    val route: String

    /**
     * String resource id to that contains title to be displayed for the screen.
     */
    val titleRes: Int
}

object EscrowedListDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

object CameraDestination : NavigationDestination {
    override val route = "camera"
    override val titleRes = R.string.app_name
}

@Composable
fun ClicClacApp(navController: NavHostController = rememberNavController()) {
    InventoryNavHost(navController = navController)
}

@Composable
fun InventoryNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    MaterialTheme {

        NavHost(
            navController = navController,
            startDestination = EscrowedListDestination.route,
            modifier = modifier
        ) {
            composable(route = EscrowedListDestination.route) {
                EscrowedListScreen(onCameraClick = { navController.navigate(CameraDestination.route) })
            }
            composable(route = CameraDestination.route) {
                CameraScreen(
                    onConfig = { navController.navigate(EscrowedListDestination.route) }
//                onCapture = {}
                )
            }
        }
    }
}


