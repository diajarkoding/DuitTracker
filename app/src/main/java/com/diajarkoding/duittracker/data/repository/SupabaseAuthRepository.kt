package com.diajarkoding.duittracker.data.repository

import com.diajarkoding.duittracker.data.local.preferences.SyncPreferences
import com.diajarkoding.duittracker.data.model.User
import com.diajarkoding.duittracker.domain.repository.AuthResult
import com.diajarkoding.duittracker.domain.repository.IAuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val syncPreferences: SyncPreferences
) : IAuthRepository {

    override val currentUser: Flow<User?> = supabaseClient.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                val session = status.session
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                User(
                    id = session.user?.id ?: "",
                    email = session.user?.email ?: "",
                    name = session.user?.userMetadata?.get("name")?.toString()?.removeSurrounding("\"") 
                        ?: session.user?.email?.substringBefore("@") ?: "User",
                    avatarUrl = null,
                    createdAt = now,
                    updatedAt = now
                )
            }
            else -> null
        }
    }

    override val isLoggedIn: Flow<Boolean> = supabaseClient.auth.sessionStatus.map { status ->
        status is SessionStatus.Authenticated
    }

    override suspend fun login(email: String, password: String): AuthResult {
        return try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val user = getCurrentUser()
            if (user != null) {
                // Cache user ID for offline access
                syncPreferences.setCurrentUserId(user.id)
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Failed to get user after login")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun register(name: String, email: String, password: String): AuthResult {
        return try {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("name", name)
                }
            }
            
            // Don't auto sign in - user will be redirected to login page
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val user = User(
                id = "",
                email = email,
                name = name,
                avatarUrl = null,
                createdAt = now,
                updatedAt = now
            )
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun logout() {
        try {
            supabaseClient.auth.signOut()
            syncPreferences.clearAll()
        } catch (e: Exception) {
            // Ignore logout errors, but still clear local preferences
            syncPreferences.clearAll()
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val session = supabaseClient.auth.currentSessionOrNull()
            if (session != null) {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val user = User(
                    id = session.user?.id ?: "",
                    email = session.user?.email ?: "",
                    name = session.user?.userMetadata?.get("name")?.toString()?.removeSurrounding("\"")
                        ?: session.user?.email?.substringBefore("@") ?: "User",
                    avatarUrl = null,
                    createdAt = now,
                    updatedAt = now
                )
                // Cache user ID for offline access
                if (user.id.isNotBlank()) {
                    syncPreferences.setCurrentUserId(user.id)
                }
                user
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun refreshSession(): Boolean {
        return try {
            supabaseClient.auth.refreshCurrentSession()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getSessionStatus(): Flow<SessionStatus> = supabaseClient.auth.sessionStatus
}
