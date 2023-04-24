package com.example.myapplication

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.EscrowedListState
import com.example.myapplication.ui.EscrowedListViewModel

@Composable
fun EscrowedUI(
    viewModel: EscrowedListViewModel = viewModel(factory = EscrowedListViewModel.Factory)
) {
    val escrowedListState by viewModel.escrowedListState.collectAsState()

    MaterialTheme {
        // Material Components like Button, Card, Switch, etc.
        LazyColumn {
            // Add a single iteme

           items(escrowedListState.itemList) {
               element ->
                Text(text = element.UUID)
                Text(text = element.deadline.toString())
           }
        }

    }
}

/*
@Preview
@Composable
fun DefaultPreview2() {
    EscrowedUIi(List<EscrowDbEntry()>)
}
*/



