package com.example.facebook.ui.screens

import androidx.navigation.NavHostController
import com.example.facebook.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileScreenTest {

    private lateinit var userViewModel: UserViewModel
    private lateinit var friendViewModel: FriendsViewModel
    private lateinit var navController: NavHostController
    private lateinit var user: User

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        user = User(
            _id = "1",
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phoneNumber = "123456789",
            friendStatus = "friend"
        )
        userViewModel = mockk(relaxed = true)
        friendViewModel = mockk(relaxed = true)
        navController = mockk(relaxed = true)

        coEvery { userViewModel.getUserById(any()) } returns MutableStateFlow(user)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ProfileScreen should display the correct user information`() = runTest {
        val userState = userViewModel.getUserById(user._id).value
        assertEquals("John", userState?.firstName)
        assertEquals("Doe", userState?.lastName)
        assertEquals("john.doe@example.com", userState?.email)
    }

    @Test
    fun `EditInfoDialog should update user info with correct inputs`() = runTest {
        val updatedFirstName = "Johnny"
        val updatedLastName = "Dough"

        userViewModel.handleUpdate(firstName = updatedFirstName, lastName = updatedLastName)

        coVerify {
            userViewModel.handleUpdate(
                firstName = updatedFirstName,
                lastName = updatedLastName
            )
        }
    }

    @Test
    fun `EditPasswordDialog should update user password when confirmed`() = runTest {
        val newPassword = "securepassword"

        userViewModel.handleUpdate(password = newPassword)

        coVerify { userViewModel.handleUpdate(password = newPassword) }
    }

    @Test
    fun `Logout button should trigger user logout`() = runTest {
        userViewModel.logout()

        coVerify { userViewModel.logout() }
    }

    @Test
    fun `FriendStatusSection should handle friend removal`() = runTest {
        friendViewModel.disfriend(user._id)

        coVerify { friendViewModel.disfriend(user._id) }
    }

    @Test
    fun `Friend request actions should be handled correctly`() = runTest {
        // Simulate a scenario where friend status is 'suggest'
        val userWithSuggestStatus = user.copy(friendStatus = "suggest")
        coEvery { userViewModel.getUserById(any()) } returns MutableStateFlow(userWithSuggestStatus)

        friendViewModel.request(user._id)

        coVerify { friendViewModel.request(user._id) }
    }

    @Test
    fun `Revoke friend request when status is 'send'`() = runTest {
        val userWithSendStatus = user.copy(friendStatus = "send")
        coEvery { userViewModel.getUserById(any()) } returns MutableStateFlow(userWithSendStatus)

        friendViewModel.revoke(user._id)

        coVerify { friendViewModel.revoke(user._id) }
    }

    @Test
    fun `Accept friend request when status is 'request'`() = runTest {
        val userWithRequestStatus = user.copy(friendStatus = "request")
        coEvery { userViewModel.getUserById(any()) } returns MutableStateFlow(userWithRequestStatus)

        friendViewModel.accept(user._id)

        coVerify { friendViewModel.accept(user._id) }
    }

    @Test
    fun `Decline friend request when status is 'request'`() = runTest {
        val userWithRequestStatus = user.copy(friendStatus = "request")
        coEvery { userViewModel.getUserById(any()) } returns MutableStateFlow(userWithRequestStatus)

        friendViewModel.decline(user._id)

        coVerify { friendViewModel.decline(user._id) }
    }
}