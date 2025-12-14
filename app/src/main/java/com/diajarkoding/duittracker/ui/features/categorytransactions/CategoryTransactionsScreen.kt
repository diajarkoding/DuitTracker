package com.diajarkoding.duittracker.ui.features.categorytransactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.ui.components.NeoCardFlat
import com.diajarkoding.duittracker.ui.components.NeoIconButton
import com.diajarkoding.duittracker.ui.components.NeoSkeletonBox
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoDimens
import com.diajarkoding.duittracker.ui.theme.NeoSpacing
import com.diajarkoding.duittracker.utils.CategoryUtils
import com.diajarkoding.duittracker.utils.CurrencyFormatter
import com.diajarkoding.duittracker.utils.DateFormatter

@Composable
fun CategoryTransactionsScreen(
    onNavigateBack: () -> Unit,
    onTransactionClick: (String) -> Unit,
    viewModel: CategoryTransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.categoryName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NeoColors.PureBlack
                    )
                    Text(
                        text = uiState.monthName,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeoColors.MediumGray
                    )
                }
            }
        },
        containerColor = NeoColors.Background
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingSkeleton(modifier = Modifier.padding(paddingValues))
        } else if (uiState.error != null) {
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = NeoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)
            ) {
                // Summary Card
                item {
                    SummaryCard(
                        transactionCount = uiState.transactions.size,
                        totalAmount = uiState.totalAmount,
                        isExpense = uiState.isExpense,
                        category = uiState.category
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(NeoSpacing.sm))
                }

                // Transaction List
                if (uiState.transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = NeoSpacing.xxl),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeoColors.MediumGray
                            )
                        }
                    }
                } else {
                    items(uiState.transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            isExpense = uiState.isExpense,
                            onClick = { onTransactionClick(transaction.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(NeoSpacing.xxl)) }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    transactionCount: Int,
    totalAmount: Double,
    isExpense: Boolean,
    category: com.diajarkoding.duittracker.data.model.TransactionCategory
) {
    val categoryColor = CategoryUtils.getColor(category)
    val categoryIcon = CategoryUtils.getIcon(category)

    NeoCardFlat(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = categoryColor,
        cornerRadius = NeoDimens.cornerRadius,
        borderWidth = NeoDimens.borderWidth
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NeoSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                    .background(NeoColors.PureWhite.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = NeoColors.PureWhite,
                    modifier = Modifier.size(NeoDimens.iconSizeLarge)
                )
            }

            Spacer(modifier = Modifier.width(NeoSpacing.lg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$transactionCount transactions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeoColors.PureWhite.copy(alpha = 0.8f)
                )
                Text(
                    text = CurrencyFormatter.format(totalAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeoColors.PureWhite
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    isExpense: Boolean,
    onClick: () -> Unit
) {
    NeoCardFlat(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = NeoColors.PureWhite,
        cornerRadius = NeoDimens.cornerRadius,
        borderWidth = NeoDimens.borderWidth
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NeoSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note.ifEmpty { "No note" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = NeoColors.PureBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!transaction.description.isNullOrEmpty()) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeoColors.MediumGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = DateFormatter.formatDateHeader(transaction.transactionDate.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = NeoColors.MediumGray
                )
            }

            Spacer(modifier = Modifier.width(NeoSpacing.md))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatCompact(transaction.amount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) NeoColors.ExpenseRed else NeoColors.IncomeGreen
                )
                Text(
                    text = transaction.accountSource.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeoColors.MediumGray
                )
            }
        }
    }
}

@Composable
private fun LoadingSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(NeoSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)
    ) {
        // Summary skeleton
        NeoCardFlat(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = NeoDimens.cornerRadius
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NeoSpacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeoSkeletonBox(width = 48.dp, height = 48.dp)
                Spacer(modifier = Modifier.width(NeoSpacing.lg))
                Column {
                    NeoSkeletonBox(width = 100.dp, height = 14.dp)
                    Spacer(modifier = Modifier.height(NeoSpacing.xs))
                    NeoSkeletonBox(width = 150.dp, height = 24.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(NeoSpacing.sm))

        // Transaction items skeleton
        repeat(5) {
            NeoCardFlat(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = NeoDimens.cornerRadius
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NeoSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        NeoSkeletonBox(width = 120.dp, height = 14.dp)
                        Spacer(modifier = Modifier.height(NeoSpacing.xs))
                        NeoSkeletonBox(width = 80.dp, height = 12.dp)
                    }
                    NeoSkeletonBox(width = 70.dp, height = 16.dp)
                }
            }
        }
    }
}
