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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diajarkoding.duittracker.R
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.model.TransactionType
import com.diajarkoding.duittracker.ui.components.NeoButton
import com.diajarkoding.duittracker.ui.components.NeoCard
import com.diajarkoding.duittracker.ui.components.NeoCardFlat
import com.diajarkoding.duittracker.ui.components.NeoAvatar
import com.diajarkoding.duittracker.ui.components.NeoSkeletonDashboard
import com.diajarkoding.duittracker.ui.components.NeoSnackbarHost
import com.diajarkoding.duittracker.ui.components.OfflineIndicator
import com.diajarkoding.duittracker.ui.components.showNeoSnackbar
import com.diajarkoding.duittracker.ui.theme.MoneyLargeTextStyle
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoTheme
import com.diajarkoding.duittracker.ui.theme.NeoDimens
import com.diajarkoding.duittracker.ui.theme.NeoSpacing
import com.diajarkoding.duittracker.utils.CategoryUtils
import com.diajarkoding.duittracker.utils.CurrencyFormatter
import com.diajarkoding.duittracker.utils.DateFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.LocalDate

import kotlinx.datetime.LocalDateTime
import androidx.compose.ui.tooling.preview.Preview
import com.diajarkoding.duittracker.data.model.AccountSource
import com.diajarkoding.duittracker.data.model.TransactionCategory
import com.diajarkoding.duittracker.ui.theme.DuitTrackerTheme
@Composable
fun DashboardScreen(
    onAddClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onProfileClick: () -> Unit = {},
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
                        text = "${stringResource(uiState.greetingResId)},",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeoTheme.colors.textSecondary
                    )
                    Text(
                        text = uiState.userName.ifEmpty { stringResource(R.string.user_default) },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NeoTheme.colors.textPrimary
                    )
                }
                NeoAvatar(
                    userName = uiState.userName,
                    onClick = onProfileClick
                )
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
                    contentDescription = stringResource(R.string.add_transaction),
                    modifier = Modifier.size(NeoDimens.iconSizeLarge)
                )
            }
        },
        containerColor = NeoTheme.colors.background
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
                            item { EmptyState(message = stringResource(R.string.no_transactions_month)) }
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
                            item { EmptyState(message = stringResource(R.string.no_transactions)) }
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
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = NeoSpacing.sm
            ),
        backgroundColor = NeoTheme.colors.cardBackground,
        shadowOffset = NeoDimens.shadowOffset,
        cornerRadius = NeoDimens.cornerRadius
    ) {
        Column(modifier = Modifier.padding(NeoSpacing.lg)) {
            // Month and Balance
            Row(
                modifier = Modifier.fillMaxWidth()
                ,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = currentMonthName,
                    style = MaterialTheme.typography.labelMedium,
                    color = NeoTheme.colors.textSecondary,
                    maxLines = 1
                )
                // Balance indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                        .background(NeoColors.SunYellow)
                        .padding(horizontal = NeoSpacing.md, vertical = NeoSpacing.xs)
                ) {
                    Text(
                        text = stringResource(R.string.balance),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeoColors.PureBlack,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(NeoSpacing.md))

            Text(
                text = CurrencyFormatter.format(balance),
                style = MoneyLargeTextStyle,
                color = NeoTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

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
                            text = stringResource(R.string.income),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = NeoTheme.colors.cardBackground.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(NeoSpacing.xs))
                        Text(
                            text = CurrencyFormatter.format(totalIncome),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeoTheme.colors.cardBackground
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
                            text = stringResource(R.string.expense),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = NeoTheme.colors.cardBackground.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(NeoSpacing.xs))
                        Text(
                            text = CurrencyFormatter.format(totalExpense),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeoTheme.colors.cardBackground
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
            .background(NeoTheme.colors.lightGray.copy(alpha = 0.5f))
            .padding(NeoSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(NeoSpacing.xs)
    ) {
        ViewMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                    .background(if (isSelected) NeoTheme.colors.textPrimary else NeoTheme.colors.background.copy(alpha = 0f))
                    .clickable { onModeChange(mode) }
                    .padding(vertical = NeoSpacing.md),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (mode) {
                        ViewMode.DAILY -> stringResource(R.string.daily)
                        ViewMode.MONTHLY -> stringResource(R.string.monthly)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) NeoTheme.colors.cardBackground else NeoTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun DateHeader(date: LocalDate) {
    Text(
        text = DateFormatter.formatDateHeaderLocalized(date),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = NeoTheme.colors.textSecondary,
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
        backgroundColor = if (monthData.isExpanded) NeoTheme.colors.textPrimary else NeoTheme.colors.cardBackground,
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
                    tint = if (monthData.isExpanded) NeoTheme.colors.cardBackground else NeoTheme.colors.textPrimary,
                    modifier = Modifier.size(NeoDimens.iconSizeMedium)
                )
                Column {
                    Text(
                        text = monthData.monthKey,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (monthData.isExpanded) NeoTheme.colors.cardBackground else NeoTheme.colors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.transaction_count_format, monthData.transactions.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (monthData.isExpanded) NeoTheme.colors.lightGray else NeoTheme.colors.textSecondary
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
            color = NeoTheme.colors.textSecondary
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
                    tint = NeoTheme.colors.cardBackground,
                    modifier = Modifier.size(NeoDimens.iconSizeSmall)
                )
            }

            Spacer(modifier = Modifier.width(NeoSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = NeoTheme.colors.textPrimary
                )
                Text(
                    text = "${CategoryUtils.getLocalizedDisplayName(transaction.category)} · ${DateFormatter.formatTime(transaction.transactionDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeoTheme.colors.textSecondary
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

// Preview Data
private object PreviewData {
    val sampleTransactions = listOf(
        Transaction(
            id = "1",
            userId = "user1",
            amount = 50000.0,
            category = TransactionCategory.FOOD,
            type = TransactionType.EXPENSE,
            accountSource = AccountSource.CASH,
            note = "Makan siang",
            transactionDate = LocalDateTime(2024, 1, 15, 12, 30, 0)
        ),
        Transaction(
            id = "2",
            userId = "user1",
            amount = 5000000.0,
            category = TransactionCategory.SALARY,
            type = TransactionType.INCOME,
            accountSource = AccountSource.BANK,
            note = "Gaji bulanan",
            transactionDate = LocalDateTime(2024, 1, 15, 9, 0, 0)
        ),
        Transaction(
            id = "3",
            userId = "user1",
            amount = 150000.0,
            category = TransactionCategory.TRANSPORT,
            type = TransactionType.EXPENSE,
            accountSource = AccountSource.EWALLET,
            note = "Bensin motor",
            transactionDate = LocalDateTime(2024, 1, 14, 18, 0, 0)
        )
    )
}

@Preview(showBackground = true, name = "Summary Card - Normal Balance")
@Composable
private fun SummaryCardPreview() {
    DuitTrackerTheme {
        SummaryCard(
            balance = 4800000.0,
            totalExpense = 200000.0,
            totalIncome = 5000000.0,
            currentMonthName = "January 2024"
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Dashboard - Full Screen with Transactions"
)
@Composable
private fun DashboardFullScreenPreview() {
    DuitTrackerTheme {
        DashboardContentPreview(
            userName = "John",
            balance = 4800000.0,
            totalExpense = 200000.0,
            totalIncome = 5000000.0,
            transactions = PreviewData.sampleTransactions
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Dashboard - Full Screen Empty"
)
@Composable
private fun DashboardFullScreenEmptyPreview() {
    DuitTrackerTheme {
        DashboardContentPreview(
            userName = "John",
            balance = 0.0,
            totalExpense = 0.0,
            totalIncome = 0.0,
            transactions = emptyList()
        )
    }
}

@Composable
private fun DashboardContentPreview(
    userName: String,
    balance: Double,
    totalExpense: Double,
    totalIncome: Double,
    transactions: List<Transaction>
) {
    Scaffold(
        topBar = {
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
                        text = "Good Morning,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeoTheme.colors.textSecondary
                    )
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NeoTheme.colors.textPrimary
                    )
                }
            }
        },
        floatingActionButton = {
            NeoButton(
                onClick = {},
                backgroundColor = NeoColors.PureBlack,
                contentColor = NeoColors.SunYellow,
                shadowOffset = NeoDimens.shadowOffset,
                cornerRadius = NeoDimens.cornerRadius
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(NeoDimens.iconSizeLarge)
                )
            }
        },
        containerColor = NeoTheme.colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = NeoSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(NeoSpacing.md)
        ) {
            item {
                SummaryCard(
                    balance = balance,
                    totalExpense = totalExpense,
                    totalIncome = totalIncome,
                    currentMonthName = "January 2024"
                )
            }

            item {
                ViewModeToggle(
                    selectedMode = ViewMode.DAILY,
                    onModeChange = {}
                )
            }

            if (transactions.isEmpty()) {
                item { EmptyState(message = stringResource(R.string.no_transactions_month)) }
            } else {
                transactions.groupBy { it.transactionDate.date }.forEach { (date, txList) ->
                    item { DateHeader(date = date) }
                    items(txList) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = {}
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Preview(showBackground = true, name = "Summary Card - Large Balance")
@Composable
private fun SummaryCardLargeBalancePreview() {
    DuitTrackerTheme {
        SummaryCard(
            balance = 999999999.0,
            totalExpense = 50000000.0,
            totalIncome = 1049999999.0,
            currentMonthName = "January 2024"
        )
    }
}

@Preview(showBackground = true, name = "Summary Card - Negative Balance")
@Composable
private fun SummaryCardNegativeBalancePreview() {
    DuitTrackerTheme {
        SummaryCard(
            balance = -500000.0,
            totalExpense = 1500000.0,
            totalIncome = 1000000.0,
            currentMonthName = "January 2024"
        )
    }
}

@Preview(showBackground = true, name = "Summary Card - Zero Balance")
@Composable
private fun SummaryCardZeroBalancePreview() {
    DuitTrackerTheme {
        SummaryCard(
            balance = 0.0,
            totalExpense = 0.0,
            totalIncome = 0.0,
            currentMonthName = "January 2024"
        )
    }
}

@Preview(showBackground = true, name = "View Mode Toggle - Daily")
@Composable
private fun ViewModeToggleDailyPreview() {
    DuitTrackerTheme {
        ViewModeToggle(
            selectedMode = ViewMode.DAILY,
            onModeChange = {}
        )
    }
}

@Preview(showBackground = true, name = "View Mode Toggle - Monthly")
@Composable
private fun ViewModeToggleMonthlyPreview() {
    DuitTrackerTheme {
        ViewModeToggle(
            selectedMode = ViewMode.MONTHLY,
            onModeChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Transaction Item - Expense")
@Composable
private fun TransactionItemExpensePreview() {
    DuitTrackerTheme {
        TransactionItem(
            transaction = PreviewData.sampleTransactions[0],
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Transaction Item - Income")
@Composable
private fun TransactionItemIncomePreview() {
    DuitTrackerTheme {
        TransactionItem(
            transaction = PreviewData.sampleTransactions[1],
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Date Header")
@Composable
private fun DateHeaderPreview() {
    DuitTrackerTheme {
        DateHeader(date = LocalDate(2024, 1, 15))
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
private fun EmptyStatePreview() {
    DuitTrackerTheme {
        EmptyState(message = "Tidak ada transaksi bulan ini")
    }
}

@Preview(showBackground = true, name = "Month Header - Collapsed")
@Composable
private fun ExpandableMonthHeaderCollapsedPreview() {
    DuitTrackerTheme {
        ExpandableMonthHeader(
            monthData = MonthData(
                monthKey = "JAN 2024",
                year = 2024,
                month = kotlinx.datetime.Month.JANUARY,
                transactions = PreviewData.sampleTransactions,
                totalExpense = 200000.0,
                totalIncome = 5000000.0,
                isExpanded = false
            ),
            onToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Month Header - Expanded")
@Composable
private fun ExpandableMonthHeaderExpandedPreview() {
    DuitTrackerTheme {
        ExpandableMonthHeader(
            monthData = MonthData(
                monthKey = "JAN 2024",
                year = 2024,
                month = kotlinx.datetime.Month.JANUARY,
                transactions = PreviewData.sampleTransactions,
                totalExpense = 200000.0,
                totalIncome = 5000000.0,
                isExpanded = true
            ),
            onToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Summary Card - Small Screen", widthDp = 320)
@Composable
private fun SummaryCardSmallScreenPreview() {
    DuitTrackerTheme {
        SummaryCard(
            balance = 999999999.0,
            totalExpense = 50000000.0,
            totalIncome = 1049999999.0,
            currentMonthName = "January 2024"
        )
    }
}
