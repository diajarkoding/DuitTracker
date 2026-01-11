package com.diajarkoding.duittracker.ui.features.edit

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
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
import com.diajarkoding.duittracker.ui.components.NeoSkeletonDashboard
import com.diajarkoding.duittracker.ui.components.NeoSnackbarHost
import com.diajarkoding.duittracker.ui.components.SnackbarType
import com.diajarkoding.duittracker.ui.components.showNeoSnackbar
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoDimens
import com.diajarkoding.duittracker.ui.theme.NeoSpacing
import com.diajarkoding.duittracker.utils.CategoryUtils
import kotlinx.coroutines.flow.collectLatest
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTransactionScreen(
    onNavigateBack: () -> Unit,
    onTransactionUpdated: () -> Unit = {},
    viewModel: EditTransactionViewModel = hiltViewModel()
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
                is EditTransactionEvent.Success -> {
                    snackbarHostState.showNeoSnackbar(event.message, SnackbarType.SUCCESS)
                }
                is EditTransactionEvent.Error -> {
                    snackbarHostState.showNeoSnackbar(event.message, SnackbarType.ERROR)
                }
                is EditTransactionEvent.NavigateBack -> onTransactionUpdated()
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
    val selectedDate = uiState.selectedDate
    if (uiState.showDatePicker && selectedDate != null) {
        NeoDatePickerDialog(
            initialDate = selectedDate,
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
                    text = stringResource(R.string.edit_transaction),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeoColors.PureBlack
                )
            }
        },
        snackbarHost = { NeoSnackbarHost(snackbarHostState) },
        containerColor = NeoColors.Background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                NeoSkeletonDashboard()
            }
        } else {
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

                // Date Picker
                uiState.selectedDate?.let { date ->
                    NeoDatePickerField(
                        selectedDate = date,
                        onDateClick = viewModel::showDatePicker,
                        label = stringResource(R.string.date)
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
                    val hasImage = uiState.imageUri != null || uiState.existingImageUrl != null
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
                            backgroundColor = if (hasImage) NeoColors.IncomeGreen else NeoColors.PureWhite,
                            size = 56.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = stringResource(R.string.add_image),
                                modifier = Modifier.size(NeoDimens.iconSizeMedium),
                                tint = if (hasImage) NeoColors.PureWhite else NeoColors.MediumGray
                            )
                        }
                    }

                    // Image Preview (if selected)
                    if (hasImage) {
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
                            val imageData = uiState.imageUri ?: uiState.existingImageUrl
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageData)
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
                    text = if (uiState.isSaving) stringResource(R.string.updating) else stringResource(R.string.update_transaction),
                    onClick = viewModel::saveTransaction,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (uiState.isExpense) NeoColors.ExpenseRed else NeoColors.IncomeGreen,
                    enabled = !uiState.isSaving
                )

                // Offline indicator
                if (uiState.isOffline) {
                    Text(
                        text = stringResource(R.string.offline_changes_sync),
                        style = MaterialTheme.typography.bodySmall,
                        color = NeoColors.MediumGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(NeoSpacing.xxl))
            }
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
