package com.example.facebook.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FriendSearchScreen(
    modifier: Modifier = Modifier
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
    ) {
        TextField(
            "Tim kiem",
            {},
            leadingIcon = {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = null
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null
                )
            }
        )
        ResultList()
    }
}

fun ResultList(
    modifier: Modifier = Modifier
) {

}

@Preview
@Composable
fun FriendSearchScreenPreview() {
    FriendSearchScreen()
}