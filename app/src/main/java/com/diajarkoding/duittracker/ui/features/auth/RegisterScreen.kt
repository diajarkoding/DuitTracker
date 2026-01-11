package com.diajarkoding.duittracker.ui.features.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
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
import com.diajarkoding.duittracker.R
import com.diajarkoding.duittracker.ui.components.NeoButtonText
import com.diajarkoding.duittracker.ui.components.NeoCard
import com.diajarkoding.duittracker.ui.components.NeoIconButton
import com.diajarkoding.duittracker.ui.components.NeoInput
import com.diajarkoding.duittracker.ui.components.NeoPasswordInput
import com.diajarkoding.duittracker.ui.components.NeoSnackbarHost
import com.diajarkoding.duittracker.ui.components.SnackbarType
import com.diajarkoding.duittracker.ui.components.showNeoSnackbar
import com.diajarkoding.duittracker.ui.theme.NeoColors
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.tooling.preview.Preview
import com.diajarkoding.duittracker.ui.theme.DuitTrackerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is RegisterEvent.Success -> {
                    snackbarHostState.showNeoSnackbar(
                        message = event.message,
                        type = SnackbarType.SUCCESS
                    )
                    onNavigateBack()
                }
                is RegisterEvent.Error -> snackbarHostState.showNeoSnackbar(
                    message = event.message,
                    type = SnackbarType.ERROR
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { NeoSnackbarHost(snackbarHostState) },
        containerColor = NeoColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .imeNestedScroll()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Back button row
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                NeoIconButton(
                    onClick = onNavigateBack,
                    backgroundColor = NeoColors.PureWhite
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.create_account),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = NeoColors.PureBlack
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.start_tracking),
                style = MaterialTheme.typography.bodyLarge,
                color = NeoColors.DarkGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NeoColors.PureWhite,
                shadowOffset = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.register),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    NeoInput(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = stringResource(R.string.full_name),
                        placeholder = stringResource(R.string.enter_your_name),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                    if (uiState.nameError != null) {
                        Text(
                            text = uiState.nameError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeoColors.ExpenseRed
                        )
                    }

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
                            imeAction = ImeAction.Next
                        )
                    )
                    if (uiState.passwordError != null) {
                        Text(
                            text = uiState.passwordError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeoColors.ExpenseRed
                        )
                    }

                    NeoPasswordInput(
                        value = uiState.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = stringResource(R.string.confirm_password),
                        placeholder = stringResource(R.string.confirm_your_password),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )
                    if (uiState.confirmPasswordError != null) {
                        Text(
                            text = uiState.confirmPasswordError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeoColors.ExpenseRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    NeoButtonText(
                        text = if (uiState.isLoading) stringResource(R.string.creating_account) else stringResource(R.string.register).uppercase(),
                        onClick = viewModel::register,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        backgroundColor = NeoColors.IncomeGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onNavigateBack) {
                Text(
                    text = "${stringResource(R.string.already_have_account)} ${stringResource(R.string.login)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeoColors.ElectricBlue
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RegisterScreenContent(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    nameError: String?,
    emailError: String?,
    passwordError: String?,
    confirmPasswordError: String?,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .imeNestedScroll()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            NeoIconButton(
                onClick = onNavigateBack,
                backgroundColor = NeoColors.PureWhite
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Buat Akun",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = NeoColors.PureBlack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Mulai kelola keuanganmu",
            style = MaterialTheme.typography.bodyLarge,
            color = NeoColors.DarkGray
        )

        Spacer(modifier = Modifier.height(32.dp))

        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoColors.PureWhite,
            shadowOffset = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Daftar",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                NeoInput(
                    value = name,
                    onValueChange = onNameChange,
                    label = "Nama Lengkap",
                    placeholder = "Masukkan nama",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                if (nameError != null) {
                    Text(
                        text = nameError,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeoColors.ExpenseRed
                    )
                }

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
                    placeholder = "Buat password",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    )
                )
                if (passwordError != null) {
                    Text(
                        text = passwordError,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeoColors.ExpenseRed
                    )
                }

                NeoPasswordInput(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = "Konfirmasi Password",
                    placeholder = "Konfirmasi password",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )
                if (confirmPasswordError != null) {
                    Text(
                        text = confirmPasswordError,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeoColors.ExpenseRed
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                NeoButtonText(
                    text = if (isLoading) "MEMBUAT AKUN..." else "DAFTAR",
                    onClick = onRegisterClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    backgroundColor = NeoColors.IncomeGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onNavigateBack) {
            Text(
                text = "Sudah punya akun? Masuk",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NeoColors.ElectricBlue
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Preview(showBackground = true, name = "Register - Empty State")
@Composable
private fun RegisterScreenEmptyPreview() {
    DuitTrackerTheme {
        RegisterScreenContent(
            name = "",
            email = "",
            password = "",
            confirmPassword = "",
            nameError = null,
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            isLoading = false,
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onRegisterClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Register - Full Screen"
)
@Composable
private fun RegisterScreenFullPreview() {
    DuitTrackerTheme {
        Scaffold(
            containerColor = NeoColors.Background
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                RegisterScreenContent(
                    name = "",
                    email = "",
                    password = "",
                    confirmPassword = "",
                    nameError = null,
                    emailError = null,
                    passwordError = null,
                    confirmPasswordError = null,
                    isLoading = false,
                    onNameChange = {},
                    onEmailChange = {},
                    onPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onRegisterClick = {},
                    onNavigateBack = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Register - Filled State")
@Composable
private fun RegisterScreenFilledPreview() {
    DuitTrackerTheme {
        RegisterScreenContent(
            name = "John Doe",
            email = "john@example.com",
            password = "password123",
            confirmPassword = "password123",
            nameError = null,
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            isLoading = false,
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onRegisterClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Register - Error State")
@Composable
private fun RegisterScreenErrorPreview() {
    DuitTrackerTheme {
        RegisterScreenContent(
            name = "J",
            email = "invalid",
            password = "123",
            confirmPassword = "456",
            nameError = "Nama minimal 2 karakter",
            emailError = "Format email tidak valid",
            passwordError = "Password minimal 6 karakter",
            confirmPasswordError = "Password tidak cocok",
            isLoading = false,
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onRegisterClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Register - Loading State")
@Composable
private fun RegisterScreenLoadingPreview() {
    DuitTrackerTheme {
        RegisterScreenContent(
            name = "John Doe",
            email = "john@example.com",
            password = "password123",
            confirmPassword = "password123",
            nameError = null,
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            isLoading = true,
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onRegisterClick = {},
            onNavigateBack = {}
        )
    }
}
