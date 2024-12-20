package com.example.facebook.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.example.facebook.FacebookApplication
import com.example.facebook.R
import com.example.facebook.model.File
import com.example.facebook.ui.components.FileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewScreen(
    imageViewModel: ImageViewModel = viewModel(factory = ImageViewModel.Factory),
    fileViewModel: FileViewModel = viewModel(factory = FileViewModel.Factory),
    navController: NavHostController,
    allowOrigin: Boolean = true,
    modifier: Modifier = Modifier
) {
    val id = navController.currentBackStackEntry?.arguments?.getString("id") ?: ""
    val file = fileViewModel.getFileById(id).collectAsState().value
    val uiState by imageViewModel.uiState.collectAsState()
    var view by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        imageViewModel.reset()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isDownloading) {
                            CircularProgressIndicator()
                        } else {
                            IconButton(
                                onClick = {
                                    if (file != null) {
                                        imageViewModel.downloadAndSaveImage(
                                            url = file.url
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.download),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .wrapContentSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = modifier
                    .padding(20.dp)
            ) {
                if (file != null) {
                    if (file.status == "safe" || view) {
                        ZoomableImage(
                            imageUrl = file.url,
                            uiState = uiState,
                            imageViewModel = imageViewModel
                        )
                    } else if (file.status == "unsafe") {
                        SubcomposeAsyncImage(
                            model = file.blurUrl,
                            contentDescription = file.name,
                            contentScale = ContentScale.Crop,
                            loading = { CircularProgressIndicator() },
                            modifier = Modifier.fillMaxSize()
                        )
                        if (allowOrigin) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("This image has been flagged as inappropriate.")
                                Button(onClick = { view = true }) {
                                    Text("View anyway")
                                }
                            }
                        }
                    } else if (file.status == "processing") {
                        SubcomposeAsyncImage(
                            model = file.blurUrl,
                            contentDescription = file.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(
    imageUrl: String,
    uiState: ImageUIState,
    imageViewModel: ImageViewModel
) {
    Box(
        modifier = Modifier
            .clip(RectangleShape)
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures(onGesture = imageViewModel.onGesture)
            }
    ) {
        AsyncImage(
            model = imageUrl,
            modifier = Modifier
                .align(Alignment.Center) // keep the image centralized into the Box
                .graphicsLayer(
                    // adding some zoom limits (min 50%, max 200%)
                    scaleX = maxOf(.5f, minOf(3f, uiState.scale)),
                    scaleY = maxOf(.5f, minOf(3f, uiState.scale)),
                    translationX = uiState.offsetX,
                    translationY = uiState.offsetY,
//                    rotationZ = uiState.rotationState
                ),
            contentDescription = null
        )
    }
}