package com.tezcatli.clicclac.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClicClacApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: ClicClaAppViewModel = ClicClaAppViewModel()
) {
    var fullScreen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination


            if (navBackStackEntry?.destination != null) {
                Log.e("ZOGZOG--->", currentDestination?.route!!)
            }



            fullScreen = when {
                viewModel.fullScreen -> true
                currentDestination?.route == "camera" -> true
                else -> false
            }


            Log.e("CLICCLAC", currentDestination?.route.toString() + " " + viewModel.fullScreen)

            if (!fullScreen) {
                TopAppBar(
                    title = {
                        Row(horizontalArrangement = Arrangement.Center) {
                            Text(
                                textAlign = TextAlign.Center,
                                text = "Clic Clac"
                            )
                        }
                    },
                )
            }
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            if (!fullScreen) {
                NavigationBar() {
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "config" } == true,
                        icon = { Icon(Icons.Filled.Menu, null) },
                        onClick = { navController.navigate("config") })
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                        icon = { Icon(Icons.Filled.HourglassBottom, null) },
                        onClick = { navController.navigate("home") })
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "camera" } == true,
                        icon = { Icon(Icons.Filled.Camera, null) },
                        onClick = { navController.navigate("camera") })
                }
            }
        }
    )
    { innerPadding ->
        NavHost(navController, startDestination = "home", Modifier.padding(innerPadding)) {
            composable(route = "home") {
                EscrowedList(
                    onClickExpired = { navController.navigate("photo") }
                    //itemList = listAllState.itemList,
                    //    modifier = modifier.padding(innerPadding),
//                    onClick = viewModel::recoverPhoto
                )
            }
            composable(route = "camera") {
                CameraScreen(
                    onConfig = { navController.navigate("home") }
//                onCapture = {}
                )
            }
            composable(route = "config") {
                ConfigScreen(
                    onCassetteClick = { navController.navigate("configCassette") }
//                onCapture = {}
                )
            }
            composable(route = "configCassette") {
                ConfigCassetteScreen(
                    onSubmit = { navController.popBackStack() }
//                onCapture = {}
                )
            }
            composable(route = "photo") {
                PhotosScreen(
                    appViewModel = viewModel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

