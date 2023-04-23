package com.example.myapplication

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items

@Composable
fun EscrowedUIi(
    pending : List<EscrowDbEntry>
) {
    MaterialTheme {
        // Material Components like Button, Card, Switch, etc.
        LazyColumn {
            // Add a single item
           items(pending) {
               element ->
                Text(text = element.UUID)
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



