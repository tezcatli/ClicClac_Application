package com.tezcatli.clicclac.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.tezcatli.clicclac.R


enum class ROUTES(val title: String) {
    HOME("Clic Clac App"),
    CAMERA("camera"),
    CONFIG("Settings"),
    CONFIGCASSETTE("Settings"),
    PHOTO("Photo developed")
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClicClacApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: ClicClacAppViewModel = ClicClacAppViewModel()
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
                currentDestination?.route == ROUTES.CAMERA.name -> true
                currentDestination?.route == ROUTES.PHOTO.name -> viewModel.fullScreen
                else -> false
            }


            Log.d("CLICCLAC", currentDestination?.route.toString() + " " + viewModel.fullScreen)

            if (!fullScreen) {
                TopAppBar(

                    title = {
                        Row(
                            modifier = modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (currentDestination?.route != null) {
                                Row(
                                    modifier = modifier.height(IntrinsicSize.Max),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        textAlign = TextAlign.Center,
                                        text = ROUTES.valueOf(currentDestination.route.toString()).title,
                                    )
                                }
                            }

                            Image(painter = painterResource(id = R.drawable.clicclac_logo_v2),
                                "",
                                colorFilter = if (isSystemInDarkTheme()) {
                                    val colorMatrixDark = floatArrayOf(
                                        -1f, 0f, 0f, 0f, 255f,
                                        0f, -1f, 0f, 0f, 255f,
                                        0f, 0f, -1f, 0f, 255f,
                                        0f, 0f, 0f, 1f, 0f
                                    )
                                    ColorFilter.colorMatrix(ColorMatrix(colorMatrixDark))
                                } else {
                                    val colorMatrixLight = floatArrayOf(
                                        1f, 0f, 0f, 0f, 0f,
                                        0f, 1f, 0f, 0f, 0f,
                                        0f, 0f, 1f, 0f, 0f,
                                        0f, 0f, 0f, 1f, 0f
                                    )
                                    ColorFilter.colorMatrix(ColorMatrix(colorMatrixLight))
                                },
                                modifier = modifier
                                    .size(48.dp)
                                    .padding(end = 10.dp)
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
                        selected = currentDestination?.hierarchy?.any { it.route == ROUTES.CONFIG.name } == true,
                        icon = { Icon(Icons.Filled.Menu, null) },
                        onClick = { navController.navigate(ROUTES.CONFIG.name) })
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == ROUTES.HOME.name } == true,
                        icon = { Icon(Icons.Filled.HourglassBottom, null) },
                        onClick = { navController.navigate(ROUTES.HOME.name) })
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == ROUTES.CAMERA.name } == true,
                        icon = { Icon(Icons.Filled.Camera, null) },
                        onClick = { navController.navigate(ROUTES.CAMERA.name) })
                }
            }
        }
    )
    { innerPadding ->
        NavHost(
            navController,
            startDestination = ROUTES.HOME.name,
            Modifier.padding(innerPadding)
        ) {
            composable(route = ROUTES.HOME.name,
                deepLinks = listOf(navDeepLink {
                    uriPattern = "clicclac://home"
                })
            ) {
                EscrowedList(
                    onClickExpired = { navController.navigate(ROUTES.PHOTO.name) }
                    //itemList = listAllState.itemList,
                    //    modifier = modifier.padding(innerPadding),
//                    onClick = viewModel::recoverPhoto
                )
            }
            composable(route = ROUTES.CAMERA.name) {
                CameraScreen(
                    onConfig = { navController.navigate(ROUTES.HOME.name) }
//                onCapture = {}
                )
            }
            composable(route = ROUTES.CONFIG.name) {
                ConfigScreen(
                    onCassetteClick = { navController.navigate(ROUTES.CONFIGCASSETTE.name) }
//                onCapture = {}
                )
            }
            composable(route = ROUTES.CONFIGCASSETTE.name) {
                ConfigCassetteScreen(
                    onSubmit = { navController.popBackStack() }
//                onCapture = {}
                )
            }
            composable(route = ROUTES.PHOTO.name) {
                PhotosScreen(
                    appViewModel = viewModel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

