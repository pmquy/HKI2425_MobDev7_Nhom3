package com.example.facebook.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.facebook.model.File
import kotlinx.coroutines.launch

@Composable
fun GifPicker(
    modifier: Modifier = Modifier,
    fileViewModel: FileViewModel = viewModel(factory = FileViewModel.Factory),
    onClick: (String) -> Unit
) {
    val LIMIT = 10
    var gifs: List<File> by remember { mutableStateOf(emptyList()) }
    var offset by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            gifs = fileViewModel.getSystemFile("gif", 0, 50)
            offset += LIMIT
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.testTag("GifPicker"),
    ) {
        items(gifs) { gif ->
            File(
                id = gif._id,
                modifier = Modifier.clickable { onClick(gif._id) }
            )
        }
    }
}