package com.diajarkoding.duittracker.data.repository

import com.diajarkoding.duittracker.data.model.User
import com.diajarkoding.duittracker.domain.repository.AuthResult
import com.diajarkoding.duittracker.domain.repository.IAuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor() : IAuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser

    override val isLoggedIn: Flow<Boolean> = _currentUser.map { it != null }

    private val registeredUsers = mutableMapOf<String, Pair<User, String>>()

    init {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val demoUser = User(
            id = "user-1",
            email = "demo@duittracker.com",
            name = "Demo User",
            createdAt = now,
            updatedAt = now
        )
        registeredUsers["demo@duittracker.com"] = Pair(demoUser, "password123")
    }

    override suspend fun login(email: String, password: String): AuthResult {
        delay(500)

        val userEntry = registeredUsers[email.lowercase()]
        
        return if (userEntry != null && userEntry.second == password) {
            _currentUser.value = userEntry.first
            AuthResult.Success(userEntry.first)
        } else {
            AuthResult.Error("Invalid email or password")
        }
    }

    override suspend fun register(name: String, email: String, password: String): AuthResult {
        delay(500)

        if (registeredUsers.containsKey(email.lowercase())) {
            return AuthResult.Error("Email already registered")
        }

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val newUser = User(
            id = UUID.randomUUID().toString(),
            email = email.lowercase(),
            name = name,
            createdAt = now,
            updatedAt = now
        )

        registeredUsers[email.lowercase()] = Pair(newUser, password)
        _currentUser.value = newUser

        return AuthResult.Success(newUser)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }

    override suspend fun getCurrentUser(): User? {
        return _currentUser.value
    }
}
