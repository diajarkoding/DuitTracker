package com.diajarkoding.duittracker.ui.features.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diajarkoding.duittracker.BuildConfig
import com.diajarkoding.duittracker.R
import com.diajarkoding.duittracker.ui.components.NeoButtonText
import com.diajarkoding.duittracker.ui.components.NeoCard
import com.diajarkoding.duittracker.ui.components.NeoInput
import com.diajarkoding.duittracker.ui.components.NeoPasswordInput
import com.diajarkoding.duittracker.ui.components.NeoSnackbarHost
import com.diajarkoding.duittracker.ui.components.SnackbarType
import com.diajarkoding.duittracker.ui.components.showNeoSnackbar
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoTheme
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.tooling.preview.Preview
import com.diajarkoding.duittracker.ui.theme.DuitTrackerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is LoginEvent.Success -> onLoginSuccess()
                is LoginEvent.Error -> snackbarHostState.showNeoSnackbar(
                    message = event.message,
                    type = SnackbarType.ERROR
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { NeoSnackbarHost(snackbarHostState) },
        containerColor = NeoTheme.colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .imeNestedScroll()
        ) {
            // Check if keyboard is visible
            val isKeyboardVisible = WindowInsets.isImeVisible

            // Centered content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = NeoTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.start_tracking),
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeoColors.DarkGray
                )

                Spacer(modifier = Modifier.height(48.dp))

                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = NeoTheme.colors.cardBackground,
                    shadowOffset = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.login),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        NeoInput(
                            value = uiState.email,
                            onValueChange = viewModel::onEmailChange,
                            label = stringResource(R.string.email),
                            placeholder = stringResource(R.string.enter_your_email),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )
                        if (uiState.emailError != null) {
                            Text(
                                text = uiState.emailError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = NeoColors.ExpenseRed
                            )
                        }

                        NeoPasswordInput(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChange,
                            label = stringResource(R.string.password),
                            placeholder = stringResource(R.string.create_password),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            )
                        )
                        if (uiState.passwordError != null) {
                            Text(
                                text = uiState.passwordError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = NeoColors.ExpenseRed
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        NeoButtonText(
                            text = if (uiState.isLoading) stringResource(R.string.logging_in) else stringResource(R.string.login).uppercase(),
                            onClick = viewModel::login,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading,
                            backgroundColor = NeoColors.ElectricBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "${stringResource(R.string.dont_have_account)} ${stringResource(R.string.register)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeoColors.ElectricBlue
                    )
                }
            }

            // Version at bottom - hide when keyboard is visible
            AnimatedVisibility(
                visible = !isKeyboardVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Text(
                    text = stringResource(R.string.version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodySmall,
                    color = NeoTheme.colors.textSecondary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LoginScreenContent(
    email: String,
    password: String,
    emailError: String?,
    passwordError: String?,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .imeNestedScroll()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Duit Tracker",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = NeoTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mulai kelola keuanganmu",
                style = MaterialTheme.typography.bodyLarge,
                color = NeoColors.DarkGray
            )

            Spacer(modifier = Modifier.height(48.dp))

            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NeoTheme.colors.cardBackground,
                shadowOffset = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Masuk",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    NeoInput(
                        value = email,
                        onValueChange = onEmailChange,
                        label = "Email",
                        placeholder = "Masukkan email",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )
                    if (emailError != null) {
                        Text(
                            text = emailError,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeoColors.ExpenseRed
                        )
                    }

                    NeoPasswordInput(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = "Password",
                        placeholder = "Masukkan password",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )
                    if (passwordError != null) {
                        Text(
                            text = passwordError,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeoColors.ExpenseRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    NeoButtonText(
                        text = if (isLoading) "LOGGING IN..." else "MASUK",
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        backgroundColor = NeoColors.ElectricBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "Belum punya akun? Daftar",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeoColors.ElectricBlue
                )
            }
        }

        // Version at bottom - hide when keyboard is visible
        val isKeyboardVisible = WindowInsets.isImeVisible
        AnimatedVisibility(
            visible = !isKeyboardVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = NeoTheme.colors.textSecondary,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Login - Empty State")
@Composable
private fun LoginScreenEmptyPreview() {
    DuitTrackerTheme {
        LoginScreenContent(
            email = "",
            password = "",
            emailError = null,
            passwordError = null,
            isLoading = false,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Login - Full Screen"
)
@Composable
private fun LoginScreenFullPreview() {
    DuitTrackerTheme {
        Scaffold(
            containerColor = NeoTheme.colors.background
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                LoginScreenContent(
                    email = "",
                    password = "",
                    emailError = null,
                    passwordError = null,
                    isLoading = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onLoginClick = {},
                    onNavigateToRegister = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Login - Filled State")
@Composable
private fun LoginScreenFilledPreview() {
    DuitTrackerTheme {
        LoginScreenContent(
            email = "user@example.com",
            password = "password123",
            emailError = null,
            passwordError = null,
            isLoading = false,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(showBackground = true, name = "Login - Error State")
@Composable
private fun LoginScreenErrorPreview() {
    DuitTrackerTheme {
        LoginScreenContent(
            email = "invalid-email",
            password = "123",
            emailError = "Format email tidak valid",
            passwordError = "Password minimal 6 karakter",
            isLoading = false,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(showBackground = true, name = "Login - Loading State")
@Composable
private fun LoginScreenLoadingPreview() {
    DuitTrackerTheme {
        LoginScreenContent(
            email = "user@example.com",
            password = "password123",
            emailError = null,
            passwordError = null,
            isLoading = true,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(showBackground = true, name = "Login - Small Screen", widthDp = 320)
@Composable
private fun LoginScreenSmallPreview() {
    DuitTrackerTheme {
        LoginScreenContent(
            email = "user@example.com",
            password = "password123",
            emailError = null,
            passwordError = null,
            isLoading = false,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToRegister = {}
        )
    }
}
