package com.example.facebook.ui.screens

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.facebook.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(
    videoCallViewModel: VideoCallViewModel = viewModel(factory = VideoCallViewModel.Factory),
    navController: NavHostController
) {
    val id = navController.currentBackStackEntry?.arguments?.getString("id") ?: ""
    val context = LocalContext.current

    val uiState = videoCallViewModel.uiState.collectAsState()


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {

        } else {

        }
    }

    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) -> {

            }

            else -> {
                launcher.launch(android.Manifest.permission.CAMERA)
            }
        }

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) -> {

            }

            else -> {
                launcher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
        videoCallViewModel.initVideoCall(id)
    }

    DisposableEffect(Unit) {
        onDispose {
            videoCallViewModel.endCall(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VIDEO CALL") },
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(onClick = {}) {
                    Icon(painterResource(R.drawable.baseline_mic_24), contentDescription = "Mic")
                }
                FloatingActionButton(onClick = {}) {
                    Icon(
                        painterResource(R.drawable.baseline_videocam_24),
                        contentDescription = "Camera"
                    )
                }
                FloatingActionButton(onClick = {}) {
                    Icon(
                        painterResource(R.drawable.baseline_flip_camera_ios_24),
                        contentDescription = "Flip Camera"
                    )
                }
                FloatingActionButton(
                    onClick = {},
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(
                        painterResource(R.drawable.baseline_phone_missed_24),
                        contentDescription = "Phone off"
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {
        Column(modifier = Modifier.padding(it)) {
            AndroidView(
                factory = { uiState.value.mainSurfaceViewRenderer },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {}
            uiState.value.users.forEach { user ->
                AndroidView(
                    factory = { user.surfaceViewRenderer },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {}
            }
        }
    }
}