package com.example.myapplication.ui

import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.AppViewModelProvider
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClicClacApp(navController: NavHostController = rememberNavController(),
    viewModel: EscrowedListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier,
) {
    //val pendingListState by viewModel.pendingListState.collectAsState()
    //val expiredListState by viewModel.expiredListState.collectAsState()
    //val listAllState by viewModel.listAllState.collectAsState()

    Scaffold(
        topBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            if (navBackStackEntry?.destination != null) {
                Log.e("ZOGZOG--->", currentDestination?.route!!)
            }
            if (currentDestination?.route != "camera") {
                TopAppBar(
                    title = { Text("Clic Clac") },
                    /*
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Menu, null)
                        }
                    },
                    actions = {
                        BadgedBox(badge = { Badge { Text(expiredListState.itemList.size.toString()) } }) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Filled.Mail, null)
                            }
                        }
                        BadgedBox(badge = { Badge { Text(pendingListState.itemList.size.toString()) } }) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Filled.HourglassBottom, null)
                            }
                        }
                        IconButton(onClick = { navController.navigate("camera") }) {
                            Icon(Icons.Filled.ArrowBack, null)
                        }
                    }
                    */
                )
            }
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            if (currentDestination?.route != "camera") {
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
                    onCassetteClick =  { navController.navigate("configCassette") }
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
                )
            }
        }
    }
}

