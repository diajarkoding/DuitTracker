package com.diajarkoding.duittracker.ui.features.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.model.TransactionType
import com.diajarkoding.duittracker.ui.components.NeoButton
import com.diajarkoding.duittracker.ui.components.NeoCard
import com.diajarkoding.duittracker.ui.components.NeoCardFlat
import com.diajarkoding.duittracker.ui.components.NeoIconButton
import com.diajarkoding.duittracker.ui.components.NeoSkeletonDashboard
import com.diajarkoding.duittracker.ui.components.NeoSnackbarHost
import com.diajarkoding.duittracker.ui.components.OfflineIndicator
import com.diajarkoding.duittracker.ui.components.showNeoSnackbar
import com.diajarkoding.duittracker.ui.theme.MoneyLargeTextStyle
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoDimens
import com.diajarkoding.duittracker.ui.theme.NeoSpacing
import com.diajarkoding.duittracker.utils.CategoryUtils
import com.diajarkoding.duittracker.utils.CurrencyFormatter
import com.diajarkoding.duittracker.utils.DateFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.LocalDate

@Composable
fun DashboardScreen(
    onAddClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onStatsClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    shouldRefresh: Boolean = false,
    onRefreshHandled: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Refresh data only when signaled (after successful transaction add/delete)
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.refresh()
            onRefreshHandled()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DashboardEvent.LoggedOut -> onLogout()
                is DashboardEvent.ShowSnackbar -> {
                    snackbarHostState.showNeoSnackbar(event.message, event.type)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { NeoSnackbarHost(snackbarHostState) },
        topBar = {
            // Simple header with actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .padding(horizontal = NeoSpacing.lg, vertical = NeoSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${uiState.greeting},",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeoColors.MediumGray
                    )
                    Text(
                        text = uiState.userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NeoColors.PureBlack
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                    NeoIconButton(
                        onClick = onStatsClick,
                        backgroundColor = NeoColors.PureWhite
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Statistics",
                            modifier = Modifier.size(NeoDimens.iconSizeMedium)
                        )
                    }
                    NeoIconButton(
                        onClick = viewModel::logout,
                        backgroundColor = NeoColors.PureBlack,
                        contentColor = NeoColors.PureWhite
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(NeoDimens.iconSizeMedium)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            NeoButton(
                onClick = onAddClick,
                backgroundColor = NeoColors.PureBlack,
                contentColor = NeoColors.SunYellow,
                shadowOffset = NeoDimens.shadowOffset,
                cornerRadius = NeoDimens.cornerRadius
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(NeoDimens.iconSizeLarge)
                )
            }
        },
        containerColor = NeoColors.Background
    ) { paddingValues ->
        if (uiState.isLoading && uiState.currentMonthTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NeoSkeletonDashboard()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = NeoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(NeoSpacing.md)
            ) {
                // Offline Indicator
                item {
                    OfflineIndicator(
                        isOffline = uiState.isOffline,
                        pendingCount = uiState.pendingCount
                    )
                }

                // Summary Card
                item {
                    SummaryCard(
                        balance = uiState.balance,
                        totalExpense = uiState.totalExpense,
                        totalIncome = uiState.totalIncome,
                        currentMonthName = uiState.currentMonthName
                    )
                }

                // View Mode Toggle
                item {
                    ViewModeToggle(
                        selectedMode = uiState.viewMode,
                        onModeChange = viewModel::setViewMode
                    )
                }

                // Content based on view mode
                when (uiState.viewMode) {
                    ViewMode.DAILY -> {
                        if (uiState.groupedTransactions.isEmpty()) {
                            item { EmptyState(message = "No transactions this month") }
                        } else {
                            uiState.groupedTransactions.forEach { (date, transactions) ->
                                item(key = "date_$date") {
                                    DateHeader(date = date)
                                }
                                items(items = transactions, key = { it.id }) { transaction ->
                                    TransactionItem(
                                        transaction = transaction,
                                        onClick = { onTransactionClick(transaction.id) }
                                    )
                                }
                            }
                        }
                    }
                    ViewMode.MONTHLY -> {
                        if (uiState.monthlyData.isEmpty()) {
                            item { EmptyState(message = "No transactions yet") }
                        } else {
                            uiState.monthlyData.forEach { monthData ->
                                item(key = "month_${monthData.monthKey}") {
                                    ExpandableMonthHeader(
                                        monthData = monthData,
                                        onToggle = { viewModel.toggleMonthExpansion(monthData.monthKey) }
                                    )
                                }
                                item(key = "month_content_${monthData.monthKey}") {
                                    AnimatedVisibility(
                                        visible = monthData.isExpanded,
                                        enter = expandVertically(),
                                        exit = shrinkVertically()
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm),
                                            modifier = Modifier.padding(top = NeoSpacing.sm)
                                        ) {
                                            monthData.transactions.forEach { transaction ->
                                                TransactionItem(
                                                    transaction = transaction,
                                                    onClick = { onTransactionClick(transaction.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    balance: Double,
    totalExpense: Double,
    totalIncome: Double,
    currentMonthName: String
) {
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = NeoColors.PureWhite,
        shadowOffset = NeoDimens.shadowOffset,
        cornerRadius = NeoDimens.cornerRadius
    ) {
        Column(modifier = Modifier.padding(NeoSpacing.lg)) {
            // Month and Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = currentMonthName,
                        style = MaterialTheme.typography.labelMedium,
                        color = NeoColors.MediumGray
                    )
                    Spacer(modifier = Modifier.height(NeoSpacing.xs))
                    Text(
                        text = CurrencyFormatter.format(balance),
                        style = MoneyLargeTextStyle,
                        color = NeoColors.PureBlack
                    )
                }
                // Balance indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                        .background(NeoColors.SunYellow)
                        .padding(horizontal = NeoSpacing.md, vertical = NeoSpacing.xs)
                ) {
                    Text(
                        text = "Balance",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeoColors.PureBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(NeoSpacing.lg))

            // Income and Expense row - Neobrutalism style
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md)
            ) {
                // Income Card
                NeoCardFlat(
                    modifier = Modifier.weight(1f),
                    backgroundColor = NeoColors.IncomeGreen,
                    cornerRadius = NeoDimens.cornerRadiusSmall,
                    borderWidth = NeoDimens.borderWidth
                ) {
                    Column(modifier = Modifier.padding(NeoSpacing.md)) {
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = NeoColors.PureWhite.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(NeoSpacing.xs))
                        Text(
                            text = CurrencyFormatter.format(totalIncome),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeoColors.PureWhite
                        )
                    }
                }
                // Expense Card
                NeoCardFlat(
                    modifier = Modifier.weight(1f),
                    backgroundColor = NeoColors.ExpenseRed,
                    cornerRadius = NeoDimens.cornerRadiusSmall,
                    borderWidth = NeoDimens.borderWidth
                ) {
                    Column(modifier = Modifier.padding(NeoSpacing.md)) {
                        Text(
                            text = "Expense",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = NeoColors.PureWhite.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(NeoSpacing.xs))
                        Text(
                            text = CurrencyFormatter.format(totalExpense),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeoColors.PureWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewModeToggle(
    selectedMode: ViewMode,
    onModeChange: (ViewMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
            .background(NeoColors.LightGray.copy(alpha = 0.5f))
            .padding(NeoSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(NeoSpacing.xs)
    ) {
        ViewMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                    .background(if (isSelected) NeoColors.PureBlack else NeoColors.Background.copy(alpha = 0f))
                    .clickable { onModeChange(mode) }
                    .padding(vertical = NeoSpacing.md),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) NeoColors.PureWhite else NeoColors.MediumGray
                )
            }
        }
    }
}

@Composable
private fun DateHeader(date: LocalDate) {
    Text(
        text = DateFormatter.formatDateHeader(date),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = NeoColors.MediumGray,
        modifier = Modifier.padding(top = NeoSpacing.sm)
    )
}

@Composable
private fun ExpandableMonthHeader(
    monthData: MonthData,
    onToggle: () -> Unit
) {
    NeoCardFlat(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        backgroundColor = if (monthData.isExpanded) NeoColors.PureBlack else NeoColors.PureWhite,
        borderWidth = NeoDimens.borderWidth,
        cornerRadius = NeoDimens.cornerRadius
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NeoSpacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md)
            ) {
                Icon(
                    imageVector = if (monthData.isExpanded)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (monthData.isExpanded) NeoColors.PureWhite else NeoColors.PureBlack,
                    modifier = Modifier.size(NeoDimens.iconSizeMedium)
                )
                Column {
                    Text(
                        text = monthData.monthKey,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (monthData.isExpanded) NeoColors.PureWhite else NeoColors.PureBlack
                    )
                    Text(
                        text = "${monthData.transactions.size} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (monthData.isExpanded) NeoColors.LightGray else NeoColors.MediumGray
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${CurrencyFormatter.formatCompact(monthData.totalIncome)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeoColors.IncomeGreen
                )
                Text(
                    text = "-${CurrencyFormatter.formatCompact(monthData.totalExpense)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeoColors.ExpenseRed
                )
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = NeoSpacing.xxl),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = NeoColors.MediumGray
        )
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val categoryColor = CategoryUtils.getColor(transaction.category)
    val categoryIcon = CategoryUtils.getIcon(transaction.category)
    val isExpense = transaction.type == TransactionType.EXPENSE

    NeoCardFlat(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = NeoDimens.cornerRadius,
        borderWidth = NeoDimens.borderWidth
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NeoSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                    .background(categoryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = NeoColors.PureWhite,
                    modifier = Modifier.size(NeoDimens.iconSizeSmall)
                )
            }

            Spacer(modifier = Modifier.width(NeoSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = NeoColors.PureBlack
                )
                Text(
                    text = "${CategoryUtils.getDisplayName(transaction.category)} · ${DateFormatter.formatTime(transaction.transactionDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeoColors.MediumGray
                )
            }

            Text(
                text = CurrencyFormatter.formatWithSign(transaction.amount, isExpense),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isExpense) NeoColors.ExpenseRed else NeoColors.IncomeGreen
            )
        }
    }
}
