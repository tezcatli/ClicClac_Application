package com.tezcatli.clicclac

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.tezcatli.clicclac.ui.ClicClacApp
import com.tezcatli.clicclac.ui.theme.ClicClacTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject


data class PermissionResult (
    val permissionsToRequest : List<String>,
    val showRationale : Boolean
        )




@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var escrowManager: EscrowManager

    private val listPermissions =  buildList {
        add(android.Manifest.permission.CAMERA)
        add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        add(android.Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var showRationale by mutableStateOf(false)


    lateinit var permissions : PermissionResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       // requestPermissions()

        permissions = requestPermissions()
        showRationale = permissions.showRationale


        lifecycleScope.launch {
            escrowManager.init(
                URL("http://10.0.2.2:5000"),
                ::onReady
            )
        }

    }

    private fun onReady() {


        setContent {
            //(application as CliClacApplication).container.escrowManager = escrowManager
            ClicClacTheme {
                if (!showRationale) {
                    ClicClacApp()
                } else {
                    Column(modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center) {

                        ElevatedCard(modifier = Modifier.padding(all = 20.dp)) {
                            Column(modifier = Modifier.padding(all = 20.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        stringResource(R.string.permissions_rationale_title),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                // Row(horizontalArrangement = Arrangement.Center) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        stringResource(R.string.permissions_rationale)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Button(
                                        onClick = {
                                            showRationale = false
                                            requestPermissionLauncher.launch(permissions.permissionsToRequest.toTypedArray())
                                        }) {
                                        Text(text = "Understood")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        result.forEach { entry ->
            if (entry.value) {
                Log.i("CLICLAC", "Permission " + entry.key + " granted")
            } else {
                Log.i("CLICLAC", "Permission " + entry.key + " denied")
         //       Toast.makeText(applicationContext, "Permission " + entry.key + " denied", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun requestPermissions(): PermissionResult {

        val listPermissionsToRequest = buildList {
            listPermissions.forEach {
                if (ContextCompat.checkSelfPermission(applicationContext, it)
                    == PackageManager.PERMISSION_DENIED
                ) add(it)
            }
        }

        var showRationale =  listPermissionsToRequest.isNotEmpty() &&
            (listPermissionsToRequest.filter { !ActivityCompat.shouldShowRequestPermissionRationale(this, it) }.size == listPermissionsToRequest.size)

        return PermissionResult(listPermissionsToRequest, showRationale)

        //ActivityCompat.shouldShowRequestPermissionRationale(this, )

       // requestPermissionLauncher.launch(listPermissionsToRequest.toTypedArray())
    }


}



