package com.diajarkoding.duittracker.ui.features.input

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.diajarkoding.duittracker.R
import com.diajarkoding.duittracker.data.model.AccountSource
import com.diajarkoding.duittracker.data.model.TransactionCategory
import com.diajarkoding.duittracker.ui.components.NeoButtonText
import com.diajarkoding.duittracker.ui.components.NeoCardFlat
import com.diajarkoding.duittracker.ui.components.NeoCurrencyInput
import com.diajarkoding.duittracker.ui.components.NeoDatePickerDialog
import com.diajarkoding.duittracker.ui.components.NeoDatePickerField
import com.diajarkoding.duittracker.ui.components.NeoExpenseIncomeToggle
import com.diajarkoding.duittracker.ui.components.NeoIconButton
import com.diajarkoding.duittracker.ui.components.NeoInput
import com.diajarkoding.duittracker.ui.components.NeoSnackbarHost
import com.diajarkoding.duittracker.ui.components.SnackbarType
import com.diajarkoding.duittracker.ui.components.showNeoSnackbar
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoDimens
import com.diajarkoding.duittracker.ui.theme.NeoSpacing
import com.diajarkoding.duittracker.utils.CategoryUtils
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import androidx.compose.ui.tooling.preview.Preview
import com.diajarkoding.duittracker.ui.theme.DuitTrackerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    onTransactionAdded: () -> Unit = {},
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            viewModel.onImageSelected(tempImageUri)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    // Helper function to create temp image URI
    fun createTempImageUri(): Uri {
        val tempFile = File.createTempFile("temp_image_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempFile
        )
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempImageUri = createTempImageUri()
            cameraLauncher.launch(tempImageUri!!)
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            tempImageUri = createTempImageUri()
            cameraLauncher.launch(tempImageUri!!)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AddTransactionEvent.Success -> {
                    snackbarHostState.showNeoSnackbar(event.message, SnackbarType.SUCCESS)
                }
                is AddTransactionEvent.Error -> {
                    snackbarHostState.showNeoSnackbar(event.message, SnackbarType.ERROR)
                }
                is AddTransactionEvent.NavigateBack -> onTransactionAdded()
            }
        }
    }

    // Image Picker Dialog
    if (showImagePickerDialog) {
        ImagePickerDialog(
            onDismiss = { showImagePickerDialog = false },
            onCameraClick = {
                showImagePickerDialog = false
                launchCamera()
            },
            onGalleryClick = {
                showImagePickerDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    // Date Picker Dialog
    if (uiState.showDatePicker) {
        NeoDatePickerDialog(
            initialDate = uiState.selectedDate,
            onDateSelected = viewModel::onDateChange,
            onDismiss = viewModel::hideDatePicker
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .padding(horizontal = NeoSpacing.lg, vertical = NeoSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeoIconButton(
                    onClick = onNavigateBack,
                    backgroundColor = NeoColors.PureWhite
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        modifier = Modifier.size(NeoDimens.iconSizeMedium)
                    )
                }
                Text(
                    text = stringResource(R.string.add_transaction),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeoColors.PureBlack
                )
            }
        },
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
                .padding(NeoSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(NeoSpacing.lg)
        ) {
            // Type Toggle
            NeoExpenseIncomeToggle(
                isExpense = uiState.isExpense,
                onToggle = viewModel::onTypeChange,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Date Picker
            NeoDatePickerField(
                selectedDate = uiState.selectedDate,
                onDateClick = viewModel::showDatePicker,
                label = stringResource(R.string.date)
            )

            // Amount Input with currency format
            NeoCurrencyInput(
                value = uiState.amount,
                onValueChange = viewModel::onAmountChange,
                label = stringResource(R.string.amount)
            )

            // Category Picker
            Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                Text(
                    text = stringResource(R.string.category),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NeoColors.MediumGray
                )
                CategoryPicker(
                    selectedCategory = uiState.category,
                    onCategorySelect = viewModel::onCategoryChange,
                    isExpense = uiState.isExpense
                )
            }

            // Account Source
            Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                Text(
                    text = stringResource(R.string.account),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NeoColors.MediumGray
                )
                AccountSourcePicker(
                    selectedSource = uiState.accountSource,
                    onSourceSelect = viewModel::onAccountSourceChange
                )
            }

            // Note Input
            NeoInput(
                value = uiState.note,
                onValueChange = viewModel::onNoteChange,
                label = stringResource(R.string.note),
                placeholder = stringResource(R.string.enter_note),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            // Description & Image Row
            Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                Text(
                    text = stringResource(R.string.description_receipt_optional),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NeoColors.MediumGray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm),
                    verticalAlignment = Alignment.Top
                ) {
                    // Description Input
                    NeoInput(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChange,
                        placeholder = "Add details...",
                        singleLine = false,
                        maxLines = 2,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        )
                    )
                    // Camera Button
                    NeoIconButton(
                        onClick = { showImagePickerDialog = true },
                        backgroundColor = if (uiState.imageUri != null) NeoColors.IncomeGreen else NeoColors.PureWhite,
                        size = 56.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = stringResource(R.string.add_image),
                            modifier = Modifier.size(NeoDimens.iconSizeMedium),
                            tint = if (uiState.imageUri != null) NeoColors.PureWhite else NeoColors.MediumGray
                        )
                    }
                }

                // Image Preview (if selected)
                if (uiState.imageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(NeoDimens.cornerRadius))
                            .border(
                                NeoDimens.borderWidth,
                                NeoColors.PureBlack,
                                RoundedCornerShape(NeoDimens.cornerRadius)
                            )
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uiState.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.selected_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        NeoIconButton(
                            onClick = viewModel::clearImage,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(NeoSpacing.xs),
                            backgroundColor = NeoColors.ExpenseRed,
                            size = 28.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove_image),
                                modifier = Modifier.size(14.dp),
                                tint = NeoColors.PureWhite
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(NeoSpacing.md))

            // Save Button
            NeoButtonText(
                text = if (uiState.isLoading) stringResource(R.string.saving) else stringResource(R.string.save_transaction),
                onClick = viewModel::saveTransaction,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (uiState.isExpense) NeoColors.ExpenseRed else NeoColors.IncomeGreen,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(NeoSpacing.xxl))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryPicker(
    selectedCategory: TransactionCategory,
    onCategorySelect: (TransactionCategory) -> Unit,
    isExpense: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val categories = if (isExpense) {
        listOf(
            TransactionCategory.FOOD,
            TransactionCategory.TRANSPORT,
            TransactionCategory.SHOPPING,
            TransactionCategory.ENTERTAINMENT,
            TransactionCategory.BILLS,
            TransactionCategory.HEALTH,
            TransactionCategory.EDUCATION,
            TransactionCategory.SOCIAL,
            TransactionCategory.GIFT,
            TransactionCategory.DAILY_NEEDS,
            TransactionCategory.OTHER
        )
    } else {
        listOf(
            TransactionCategory.SALARY,
            TransactionCategory.INVESTMENT,
            TransactionCategory.OTHER
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
        // Selected category (always visible, clickable to expand)
        val categoryColor = CategoryUtils.getColor(selectedCategory)
        NeoCardFlat(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            backgroundColor = categoryColor,
            cornerRadius = NeoDimens.cornerRadiusSmall,
            borderWidth = NeoDimens.borderWidth
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NeoSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = CategoryUtils.getIcon(selectedCategory),
                    contentDescription = null,
                    modifier = Modifier.size(NeoDimens.iconSizeMedium),
                    tint = NeoColors.PureWhite
                )
                Spacer(modifier = Modifier.width(NeoSpacing.sm))
                Text(
                    text = CategoryUtils.getLocalizedDisplayName(selectedCategory),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NeoColors.PureWhite,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                    modifier = Modifier.size(NeoDimens.iconSizeMedium),
                    tint = NeoColors.PureWhite
                )
            }
        }

        // Expandable category list
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm),
                maxItemsInEachRow = 3
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    val catColor = CategoryUtils.getColor(category)

                    NeoCardFlat(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onCategorySelect(category)
                                isExpanded = false
                            },
                        backgroundColor = if (isSelected) catColor else NeoColors.PureWhite,
                        cornerRadius = NeoDimens.cornerRadiusSmall,
                        borderWidth = NeoDimens.borderWidth
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = NeoSpacing.sm, vertical = NeoSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = CategoryUtils.getIcon(category),
                                contentDescription = null,
                                modifier = Modifier.size(NeoDimens.iconSizeSmall),
                                tint = if (isSelected) NeoColors.PureWhite else catColor
                            )
                            Spacer(modifier = Modifier.width(NeoSpacing.xs))
                            Text(
                                text = CategoryUtils.getLocalizedDisplayName(category),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) NeoColors.PureWhite else NeoColors.PureBlack,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountSourcePicker(
    selectedSource: AccountSource,
    onSourceSelect: (AccountSource) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
        AccountSource.entries.forEach { source ->
            val isSelected = source == selectedSource

            NeoCardFlat(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSourceSelect(source) },
                backgroundColor = if (isSelected) NeoColors.PureBlack else NeoColors.PureWhite,
                cornerRadius = NeoDimens.cornerRadiusSmall,
                borderWidth = NeoDimens.borderWidth
            ) {
                Text(
                    text = source.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) NeoColors.PureWhite else NeoColors.PureBlack,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NeoSpacing.md)
                )
            }
        }
    }
}

@Composable
private fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        NeoCardFlat(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoColors.PureWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NeoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(NeoSpacing.md)
            ) {
                Text(
                    text = stringResource(R.string.add_image),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeoColors.PureBlack
                )

                // Camera option
                NeoCardFlat(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCameraClick() },
                    backgroundColor = NeoColors.Background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(NeoSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(NeoDimens.iconSizeMedium),
                            tint = NeoColors.ElectricBlue
                        )
                        Text(
                            text = stringResource(R.string.take_photo),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = NeoColors.PureBlack
                        )
                    }
                }

                // Gallery option
                NeoCardFlat(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGalleryClick() },
                    backgroundColor = NeoColors.Background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(NeoSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(NeoDimens.iconSizeMedium),
                            tint = NeoColors.IncomeGreen
                        )
                        Text(
                            text = stringResource(R.string.choose_from_gallery),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = NeoColors.PureBlack
                        )
                    }
                }

                // Cancel button
                NeoButtonText(
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = NeoColors.LightGray
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, name = "Category Picker - Expense")
@Composable
private fun CategoryPickerExpensePreview() {
    DuitTrackerTheme {
        CategoryPicker(
            selectedCategory = TransactionCategory.FOOD,
            onCategorySelect = {},
            isExpense = true
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, name = "Category Picker - Income")
@Composable
private fun CategoryPickerIncomePreview() {
    DuitTrackerTheme {
        CategoryPicker(
            selectedCategory = TransactionCategory.SALARY,
            onCategorySelect = {},
            isExpense = false
        )
    }
}

@Preview(showBackground = true, name = "Account Source Picker - Cash")
@Composable
private fun AccountSourcePickerCashPreview() {
    DuitTrackerTheme {
        AccountSourcePicker(
            selectedSource = AccountSource.CASH,
            onSourceSelect = {}
        )
    }
}

@Preview(showBackground = true, name = "Account Source Picker - Bank")
@Composable
private fun AccountSourcePickerBankPreview() {
    DuitTrackerTheme {
        AccountSourcePicker(
            selectedSource = AccountSource.BANK,
            onSourceSelect = {}
        )
    }
}

@Preview(showBackground = true, name = "Account Source Picker - EWallet")
@Composable
private fun AccountSourcePickerEWalletPreview() {
    DuitTrackerTheme {
        AccountSourcePicker(
            selectedSource = AccountSource.EWALLET,
            onSourceSelect = {}
        )
    }
}

@Preview(showBackground = true, name = "Image Picker Dialog")
@Composable
private fun ImagePickerDialogPreview() {
    DuitTrackerTheme {
        ImagePickerDialog(
            onDismiss = {},
            onCameraClick = {},
            onGalleryClick = {}
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Add Transaction - Full Screen"
)
@Composable
private fun AddTransactionScreenFullPreview() {
    DuitTrackerTheme {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .systemBarsPadding()
                        .padding(horizontal = NeoSpacing.lg, vertical = NeoSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeoIconButton(
                        onClick = {},
                        backgroundColor = NeoColors.PureWhite
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(NeoDimens.iconSizeMedium)
                        )
                    }
                    Text(
                        text = "Tambah Transaksi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NeoColors.PureBlack
                    )
                }
            },
            containerColor = NeoColors.Background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(NeoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(NeoSpacing.lg)
            ) {
                NeoExpenseIncomeToggle(
                    isExpense = true,
                    onToggle = {},
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                NeoDatePickerField(
                    selectedDate = kotlinx.datetime.LocalDate(2024, 1, 15),
                    onDateClick = {},
                    label = "Tanggal"
                )

                NeoCurrencyInput(
                    value = "50000",
                    onValueChange = {},
                    label = "Jumlah"
                )

                Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                    Text(
                        text = "Kategori",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NeoColors.MediumGray
                    )
                    CategoryPicker(
                        selectedCategory = TransactionCategory.FOOD,
                        onCategorySelect = {},
                        isExpense = true
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                    Text(
                        text = "Akun",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NeoColors.MediumGray
                    )
                    AccountSourcePicker(
                        selectedSource = AccountSource.CASH,
                        onSourceSelect = {}
                    )
                }

                NeoInput(
                    value = "Makan siang",
                    onValueChange = {},
                    label = "Catatan",
                    placeholder = "Masukkan catatan"
                )

                Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                    Text(
                        text = "Deskripsi & Struk (Opsional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NeoColors.MediumGray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm),
                        verticalAlignment = Alignment.Top
                    ) {
                        NeoInput(
                            value = "",
                            onValueChange = {},
                            placeholder = "Tambah detail...",
                            singleLine = false,
                            maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                        NeoIconButton(
                            onClick = {},
                            backgroundColor = NeoColors.PureWhite,
                            size = 56.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Tambah gambar",
                                modifier = Modifier.size(NeoDimens.iconSizeMedium),
                                tint = NeoColors.MediumGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(NeoSpacing.md))

                NeoButtonText(
                    text = "Simpan Transaksi",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = NeoColors.ExpenseRed
                )

                Spacer(modifier = Modifier.height(NeoSpacing.xxl))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Add Transaction - Full Screen Income"
)
@Composable
private fun AddTransactionScreenIncomePreview() {
    DuitTrackerTheme {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .systemBarsPadding()
                        .padding(horizontal = NeoSpacing.lg, vertical = NeoSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeoIconButton(
                        onClick = {},
                        backgroundColor = NeoColors.PureWhite
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(NeoDimens.iconSizeMedium)
                        )
                    }
                    Text(
                        text = "Tambah Transaksi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NeoColors.PureBlack
                    )
                }
            },
            containerColor = NeoColors.Background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(NeoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(NeoSpacing.lg)
            ) {
                NeoExpenseIncomeToggle(
                    isExpense = false,
                    onToggle = {},
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                NeoCurrencyInput(
                    value = "5000000",
                    onValueChange = {},
                    label = "Jumlah"
                )

                Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                    Text(
                        text = "Kategori",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NeoColors.MediumGray
                    )
                    CategoryPicker(
                        selectedCategory = TransactionCategory.SALARY,
                        onCategorySelect = {},
                        isExpense = false
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                    Text(
                        text = "Akun",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NeoColors.MediumGray
                    )
                    AccountSourcePicker(
                        selectedSource = AccountSource.BANK,
                        onSourceSelect = {}
                    )
                }

                NeoDatePickerField(
                    selectedDate = kotlinx.datetime.LocalDate(2024, 1, 25),
                    onDateClick = {},
                    label = "Tanggal"
                )

                NeoInput(
                    value = "Gaji bulanan",
                    onValueChange = {},
                    label = "Catatan",
                    placeholder = "Masukkan catatan"
                )

                Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                    Text(
                        text = "Deskripsi & Struk (Opsional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NeoColors.MediumGray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm),
                        verticalAlignment = Alignment.Top
                    ) {
                        NeoInput(
                            value = "Transfer dari perusahaan",
                            onValueChange = {},
                            placeholder = "Tambah detail...",
                            singleLine = false,
                            maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                        NeoIconButton(
                            onClick = {},
                            backgroundColor = NeoColors.PureWhite,
                            size = 56.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Tambah gambar",
                                modifier = Modifier.size(NeoDimens.iconSizeMedium),
                                tint = NeoColors.MediumGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(NeoSpacing.md))

                NeoButtonText(
                    text = "Simpan Transaksi",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = NeoColors.IncomeGreen
                )

                Spacer(modifier = Modifier.height(NeoSpacing.xxl))
            }
        }
    }
}

@Preview(showBackground = true, name = "Expense Income Toggle - Expense")
@Composable
private fun ExpenseIncomeToggleExpensePreview() {
    DuitTrackerTheme {
        NeoExpenseIncomeToggle(
            isExpense = true,
            onToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Expense Income Toggle - Income")
@Composable
private fun ExpenseIncomeToggleIncomePreview() {
    DuitTrackerTheme {
        NeoExpenseIncomeToggle(
            isExpense = false,
            onToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Currency Input - Empty")
@Composable
private fun CurrencyInputEmptyPreview() {
    DuitTrackerTheme {
        NeoCurrencyInput(
            value = "",
            onValueChange = {},
            label = "Jumlah"
        )
    }
}

@Preview(showBackground = true, name = "Currency Input - Filled")
@Composable
private fun CurrencyInputFilledPreview() {
    DuitTrackerTheme {
        NeoCurrencyInput(
            value = "150000",
            onValueChange = {},
            label = "Jumlah"
        )
    }
}

@Preview(showBackground = true, name = "Currency Input - Large Amount")
@Composable
private fun CurrencyInputLargePreview() {
    DuitTrackerTheme {
        NeoCurrencyInput(
            value = "999999999",
            onValueChange = {},
            label = "Jumlah"
        )
    }
}

@Preview(showBackground = true, name = "Note Input - Empty")
@Composable
private fun NoteInputEmptyPreview() {
    DuitTrackerTheme {
        NeoInput(
            value = "",
            onValueChange = {},
            label = "Catatan",
            placeholder = "Masukkan catatan"
        )
    }
}

@Preview(showBackground = true, name = "Note Input - Filled")
@Composable
private fun NoteInputFilledPreview() {
    DuitTrackerTheme {
        NeoInput(
            value = "Makan siang di restoran favorit",
            onValueChange = {},
            label = "Catatan",
            placeholder = "Masukkan catatan"
        )
    }
}

@Preview(showBackground = true, name = "Save Button - Expense")
@Composable
private fun SaveButtonExpensePreview() {
    DuitTrackerTheme {
        NeoButtonText(
            text = "Simpan Transaksi",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoColors.ExpenseRed
        )
    }
}

@Preview(showBackground = true, name = "Save Button - Income")
@Composable
private fun SaveButtonIncomePreview() {
    DuitTrackerTheme {
        NeoButtonText(
            text = "Simpan Transaksi",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoColors.IncomeGreen
        )
    }
}

@Preview(showBackground = true, name = "Save Button - Loading")
@Composable
private fun SaveButtonLoadingPreview() {
    DuitTrackerTheme {
        NeoButtonText(
            text = "Menyimpan...",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoColors.ExpenseRed,
            enabled = false
        )
    }
}

@Preview(showBackground = true, name = "Date Picker Field")
@Composable
private fun DatePickerFieldPreview() {
    DuitTrackerTheme {
        NeoDatePickerField(
            selectedDate = kotlinx.datetime.LocalDate(2024, 1, 15),
            onDateClick = {},
            label = "Tanggal"
        )
    }
}

@Preview(showBackground = true, name = "Date Picker Field - Today")
@Composable
private fun DatePickerFieldTodayPreview() {
    DuitTrackerTheme {
        NeoDatePickerField(
            selectedDate = kotlinx.datetime.LocalDate(2024, 1, 20),
            onDateClick = {},
            label = "Tanggal"
        )
    }
}

@Preview(showBackground = true, name = "Date Picker Dialog")
@Composable
private fun DatePickerDialogPreview() {
    DuitTrackerTheme {
        NeoDatePickerDialog(
            initialDate = kotlinx.datetime.LocalDate(2024, 1, 15),
            onDateSelected = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Description & Image Row - Empty")
@Composable
private fun DescriptionImageRowEmptyPreview() {
    DuitTrackerTheme {
        Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
            Text(
                text = "Deskripsi & Struk (Opsional)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = NeoColors.MediumGray
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm),
                verticalAlignment = Alignment.Top
            ) {
                NeoInput(
                    value = "",
                    onValueChange = {},
                    placeholder = "Tambah detail...",
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                NeoIconButton(
                    onClick = {},
                    backgroundColor = NeoColors.PureWhite,
                    size = 56.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Tambah gambar",
                        modifier = Modifier.size(NeoDimens.iconSizeMedium),
                        tint = NeoColors.MediumGray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Description & Image Row - Filled")
@Composable
private fun DescriptionImageRowFilledPreview() {
    DuitTrackerTheme {
        Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
            Text(
                text = "Deskripsi & Struk (Opsional)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = NeoColors.MediumGray
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm),
                verticalAlignment = Alignment.Top
            ) {
                NeoInput(
                    value = "Makan bersama keluarga di restoran",
                    onValueChange = {},
                    placeholder = "Tambah detail...",
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                NeoIconButton(
                    onClick = {},
                    backgroundColor = NeoColors.IncomeGreen,
                    size = 56.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Tambah gambar",
                        modifier = Modifier.size(NeoDimens.iconSizeMedium),
                        tint = NeoColors.PureWhite
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Camera Button - No Image")
@Composable
private fun CameraButtonNoImagePreview() {
    DuitTrackerTheme {
        NeoIconButton(
            onClick = {},
            backgroundColor = NeoColors.PureWhite,
            size = 56.dp
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Tambah gambar",
                modifier = Modifier.size(NeoDimens.iconSizeMedium),
                tint = NeoColors.MediumGray
            )
        }
    }
}

@Preview(showBackground = true, name = "Camera Button - Has Image")
@Composable
private fun CameraButtonHasImagePreview() {
    DuitTrackerTheme {
        NeoIconButton(
            onClick = {},
            backgroundColor = NeoColors.IncomeGreen,
            size = 56.dp
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Tambah gambar",
                modifier = Modifier.size(NeoDimens.iconSizeMedium),
                tint = NeoColors.PureWhite
            )
        }
    }
}
