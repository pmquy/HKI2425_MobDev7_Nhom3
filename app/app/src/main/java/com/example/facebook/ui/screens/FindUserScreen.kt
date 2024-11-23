package com.example.facebook.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.facebook.model.User
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.FacebookTopBar
import com.example.facebook.ui.components.File
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindUserScreen(
    findUserViewModel: FindUserViewModel = viewModel(factory = FindUserViewModel.Factory),
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val uiState = findUserViewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
        }
    }

    Scaffold (
        topBar = {
            FacebookTopBar(
                FacebookScreen.FRIEND_SEARCHING,
                canNavigateBack = true
            ) {
                navController.navigateUp()
            }
        }
    ) { innerPadding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TextField(
                value = uiState.value.search,
                placeholder = {Text("Tìm kiếm")},
                onValueChange = findUserViewModel.setSearch,
                leadingIcon = {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier
                            .clickable {
                                findUserViewModel.clearInput()
                            }
                    )
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    containerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    errorCursorColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .focusRequester(focusRequester)
            )
            ResultList(uiState.value.users, navController)
        }
    }
}

@Composable
fun ResultList(
    users: List<User>,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    LazyColumn {
        items(users) { user->
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
                    .fillMaxWidth()
                    .clickable {
//                            navController.navigate("${FacebookScreen.PROFILE.name}/${user._id}")
                    }
            ) {
                File(
                    id = user.avatar,
                    modifier = Modifier
                        .size(75.dp)
                        .padding(10.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = user.firstName + user.lastName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(10.dp)
                )
            }
        }
    }
}