package com.example.myapplication.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscrowedListScreen(
    viewModel: EscrowedListViewModel = viewModel(factory = EscrowedListViewModel.Factory),
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pendingListState by viewModel.pendingListState.collectAsState()
    val expiredListState by viewModel.expiredListState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clic Clac") },
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
                    IconButton(onClick = onCameraClick) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                }
            )
        },
    )
    { innerPadding ->
        EscrowedList(
            itemList = expiredListState.itemList,
            modifier = modifier.padding(innerPadding),
            onClick = viewModel::recoverPhoto
        )
    }
}


@Composable
fun EscrowedList(
    itemList: List<EscrowedState>,
    modifier: Modifier = Modifier,
    onClick : (String) -> Unit = {}
) {
    LazyColumn(modifier=modifier) {
        // Add a single item

        items(itemList) { element ->
            Log.e("ZOGZOG", element.uuid)
            Row(modifier = Modifier.clickable { onClick(element.uuid) }) {
                Text(
                    text = element.deadline.toString()
                )
            }
        }
    }
}

@Preview
@Composable
fun DefaultPreview2() {
    EscrowedList(
        listOf(
            EscrowedState(
                uuid = " 0262d08e-5321-4129-870b-83b9fa3dad80",
                deadline = ZonedDateTime.parse("2023-04-26T09:29:12.20481+02:00")
            )
        )
    )
}




