package com.diajarkoding.duittracker.ui.features.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.model.TransactionType
import com.diajarkoding.duittracker.ui.components.NeoButtonText
import com.diajarkoding.duittracker.ui.components.NeoCardFlat
import com.diajarkoding.duittracker.ui.components.NeoIconButton
import com.diajarkoding.duittracker.ui.components.NeoSkeletonTransactionDetail
import com.diajarkoding.duittracker.ui.theme.MoneyLargeTextStyle
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoDimens
import com.diajarkoding.duittracker.ui.theme.NeoSpacing
import com.diajarkoding.duittracker.utils.CategoryUtils
import com.diajarkoding.duittracker.utils.CurrencyFormatter
import com.diajarkoding.duittracker.utils.DateFormatter
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TransactionDetailScreen(
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit = {},
    onTransactionDeleted: () -> Unit = {},
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TransactionDetailEvent.Deleted -> onTransactionDeleted()
                is TransactionDetailEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .padding(horizontal = NeoSpacing.lg, vertical = NeoSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeoIconButton(
                    onClick = onNavigateBack,
                    backgroundColor = NeoColors.PureWhite
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(NeoDimens.iconSizeMedium)
                    )
                }
                Spacer(modifier = Modifier.width(NeoSpacing.md))
                Text(
                    text = "Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeoColors.PureBlack,
                    modifier = Modifier.weight(1f)
                )
                // Edit button
                if (uiState.transaction != null) {
                    NeoIconButton(
                        onClick = { onEditClick(uiState.transaction!!.id) },
                        backgroundColor = NeoColors.SunYellow
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(NeoDimens.iconSizeMedium),
                            tint = NeoColors.PureBlack
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = NeoColors.Background
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    NeoSkeletonTransactionDetail()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = NeoColors.ExpenseRed
                    )
                }
            }
            uiState.transaction != null -> {
                TransactionDetailContent(
                    transaction = uiState.transaction!!,
                    imageUrl = uiState.imageUrl,
                    onDelete = viewModel::deleteTransaction,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun TransactionDetailContent(
    transaction: Transaction,
    imageUrl: String?,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val categoryColor = CategoryUtils.getColor(transaction.category)
    val categoryIcon = CategoryUtils.getIcon(transaction.category)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(NeoSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(NeoSpacing.md)
    ) {
        // Amount Card
        NeoCardFlat(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = NeoDimens.cornerRadius
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NeoSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                        .background(if (isExpense) NeoColors.ExpenseRed.copy(alpha = 0.1f) else NeoColors.IncomeGreen.copy(alpha = 0.1f))
                        .padding(horizontal = NeoSpacing.md, vertical = NeoSpacing.xs)
                ) {
                    Text(
                        text = if (isExpense) "Expense" else "Income",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isExpense) NeoColors.ExpenseRed else NeoColors.IncomeGreen
                    )
                }
                Spacer(modifier = Modifier.height(NeoSpacing.sm))
                Text(
                    text = CurrencyFormatter.formatWithSign(transaction.amount, isExpense),
                    style = MoneyLargeTextStyle.copy(fontSize = 32.sp),
                    color = if (isExpense) NeoColors.ExpenseRed else NeoColors.IncomeGreen
                )
            }
        }

        // Category
        NeoCardFlat(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = NeoDimens.cornerRadius
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NeoSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                        .background(categoryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = NeoColors.PureWhite,
                        modifier = Modifier.size(NeoDimens.iconSizeMedium)
                    )
                }
                Column {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeoColors.MediumGray
                    )
                    Text(
                        text = CategoryUtils.getDisplayName(transaction.category),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeoColors.PureBlack
                    )
                }
            }
        }

        // Details
        NeoCardFlat(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = NeoDimens.cornerRadius
        ) {
            Column(
                modifier = Modifier.padding(NeoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(NeoSpacing.lg)
            ) {
                DetailRow(label = "Note", value = transaction.note)
                if (!transaction.description.isNullOrBlank()) {
                    DetailRow(label = "Description", value = transaction.description)
                }
                DetailRow(
                    label = "Date",
                    value = DateFormatter.formatFullDate(transaction.transactionDate.date)
                )
                DetailRow(
                    label = "Time",
                    value = DateFormatter.formatTime(transaction.transactionDate)
                )
                DetailRow(label = "Account", value = transaction.accountSource.name)
                DetailRow(
                    label = "Status",
                    value = if (transaction.isSynced) "Synced" else "Pending sync"
                )
            }
        }

        // Receipt Image (if available)
        if (!imageUrl.isNullOrBlank()) {
            NeoCardFlat(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = NeoDimens.cornerRadius
            ) {
                Column(
                    modifier = Modifier.padding(NeoSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)
                ) {
                    Text(
                        text = "Receipt",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeoColors.MediumGray
                    )
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Receipt image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                            .border(
                                NeoDimens.borderWidth,
                                NeoColors.PureBlack,
                                RoundedCornerShape(NeoDimens.cornerRadiusSmall)
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(NeoSpacing.md))

        // Delete Button
        NeoButtonText(
            text = "Delete Transaction",
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoColors.ExpenseRed
        )

        Spacer(modifier = Modifier.height(NeoSpacing.xxl))
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = NeoColors.MediumGray
        )
        Spacer(modifier = Modifier.height(NeoSpacing.xs))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = NeoColors.PureBlack
        )
    }
}
