package com.diajarkoding.duittracker.ui.features.statistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diajarkoding.duittracker.ui.components.NeoCardFlat
import com.diajarkoding.duittracker.ui.components.NeoIconButton
import com.diajarkoding.duittracker.ui.components.NeoSkeletonStatistics
import com.diajarkoding.duittracker.ui.components.NeoSnackbarHost
import com.diajarkoding.duittracker.ui.components.showNeoSnackbar
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoDimens
import com.diajarkoding.duittracker.ui.theme.NeoSpacing
import com.diajarkoding.duittracker.utils.CategoryUtils
import com.diajarkoding.duittracker.utils.CurrencyFormatter
import com.diajarkoding.duittracker.utils.DateFormatter
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    onCategoryClick: (category: String, year: Int, month: Int, isExpense: Boolean) -> Unit = { _, _, _, _ -> },
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is StatisticsEvent.ShowSnackbar -> {
                    snackbarHostState.showNeoSnackbar(event.message, event.type)
                }
                is StatisticsEvent.ShareExcel -> {
                    context.startActivity(
                        android.content.Intent.createChooser(event.intent, "Share Report")
                    )
                }
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
                    text = "Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeoColors.PureBlack,
                    modifier = Modifier.weight(1f)
                )
                // Export Button
                NeoIconButton(
                    onClick = { if (!uiState.isExporting) viewModel.exportToExcel() },
                    backgroundColor = NeoColors.IncomeGreen
                ) {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(NeoDimens.iconSizeMedium),
                            color = NeoColors.PureWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export to Excel",
                            modifier = Modifier.size(NeoDimens.iconSizeMedium),
                            tint = NeoColors.PureWhite
                        )
                    }
                }
            }
        },
        snackbarHost = { NeoSnackbarHost(snackbarHostState) },
        containerColor = NeoColors.Background
    ) { paddingValues ->
        if (uiState.isLoading && uiState.expenseByCategory.isEmpty() && uiState.incomeByCategory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NeoSkeletonStatistics()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = NeoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(NeoSpacing.md)
            ) {
                // Month Selector
                item {
                    MonthSelector(
                        currentMonth = uiState.selectedMonthName,
                        canGoNext = uiState.selectedMonthIndex > 0,
                        canGoPrevious = uiState.selectedMonthIndex < uiState.availableMonths.size - 1,
                        onPrevious = viewModel::previousMonth,
                        onNext = viewModel::nextMonth
                    )
                }

                // Summary Cards - Neobrutalism style
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md)
                    ) {
                        // Expense Card
                        NeoCardFlat(
                            modifier = Modifier.weight(1f),
                            backgroundColor = NeoColors.ExpenseRed,
                            cornerRadius = NeoDimens.cornerRadiusSmall,
                            borderWidth = NeoDimens.borderWidth
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(NeoSpacing.lg),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Expense",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = NeoColors.PureWhite.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(NeoSpacing.xs))
                                Text(
                                    text = CurrencyFormatter.formatCompact(uiState.totalExpense),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeoColors.PureWhite
                                )
                            }
                        }
                        // Income Card
                        NeoCardFlat(
                            modifier = Modifier.weight(1f),
                            backgroundColor = NeoColors.IncomeGreen,
                            cornerRadius = NeoDimens.cornerRadiusSmall,
                            borderWidth = NeoDimens.borderWidth
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(NeoSpacing.lg),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Income",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = NeoColors.PureWhite.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(NeoSpacing.xs))
                                Text(
                                    text = CurrencyFormatter.formatCompact(uiState.totalIncome),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeoColors.PureWhite
                                )
                            }
                        }
                    }
                }

                // Expense Section
                if (uiState.expenseByCategory.isNotEmpty()) {
                    item {
                        Text(
                            text = "Expense by Category",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = NeoColors.MediumGray,
                            modifier = Modifier.padding(top = NeoSpacing.sm)
                        )
                    }

                    item {
                        NeoCardFlat(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = NeoColors.PureWhite,
                            cornerRadius = NeoDimens.cornerRadius
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(NeoSpacing.xl),
                                contentAlignment = Alignment.Center
                            ) {
                                PieChart(
                                    data = uiState.expenseByCategory,
                                    totalAmount = uiState.totalExpense,
                                    modifier = Modifier.size(180.dp)
                                )
                            }
                        }
                    }

                    items(uiState.expenseByCategory) { categoryData ->
                        val selectedMonth = uiState.availableMonths.getOrNull(uiState.selectedMonthIndex)
                        CategoryRow(
                            categoryData = categoryData,
                            isExpense = true,
                            onClick = {
                                selectedMonth?.let {
                                    onCategoryClick(
                                        categoryData.category.name,
                                        it.year,
                                        it.month.ordinal + 1,
                                        true
                                    )
                                }
                            }
                        )
                    }
                } else {
                    item { EmptyChartState(message = "No expenses this month") }
                }

                // Income Section
                if (uiState.incomeByCategory.isNotEmpty()) {
                    item {
                        Text(
                            text = "Income by Category",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = NeoColors.MediumGray,
                            modifier = Modifier.padding(top = NeoSpacing.sm)
                        )
                    }

                    item {
                        NeoCardFlat(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = NeoColors.PureWhite,
                            cornerRadius = NeoDimens.cornerRadius
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(NeoSpacing.xl),
                                contentAlignment = Alignment.Center
                            ) {
                                PieChart(
                                    data = uiState.incomeByCategory,
                                    totalAmount = uiState.totalIncome,
                                    modifier = Modifier.size(180.dp)
                                )
                            }
                        }
                    }

                    items(uiState.incomeByCategory) { categoryData ->
                        val selectedMonth = uiState.availableMonths.getOrNull(uiState.selectedMonthIndex)
                        CategoryRow(
                            categoryData = categoryData,
                            isExpense = false,
                            onClick = {
                                selectedMonth?.let {
                                    onCategoryClick(
                                        categoryData.category.name,
                                        it.year,
                                        it.month.ordinal + 1,
                                        false
                                    )
                                }
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(NeoSpacing.xxl)) }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    currentMonth: String,
    canGoNext: Boolean,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NeoDimens.cornerRadius))
            .background(NeoColors.PureWhite)
            .padding(horizontal = NeoSpacing.sm, vertical = NeoSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious, enabled = canGoPrevious) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous Month",
                tint = if (canGoPrevious) NeoColors.PureBlack else NeoColors.LightGray,
                modifier = Modifier.size(NeoDimens.iconSizeMedium)
            )
        }
        Text(
            text = currentMonth,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NeoColors.PureBlack
        )
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next Month",
                tint = if (canGoNext) NeoColors.PureBlack else NeoColors.LightGray,
                modifier = Modifier.size(NeoDimens.iconSizeMedium)
            )
        }
    }
}

@Composable
private fun PieChart(
    data: List<CategoryData>,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    val colors = remember(data) {
        data.map { CategoryUtils.getColor(it.category) }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 35.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            var startAngle = -90f

            data.forEachIndexed { index, categoryData ->
                val sweepAngle = categoryData.percentage * 3.6f

                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                startAngle += sweepAngle
            }

            // Draw black borders
            drawCircle(
                color = Color.Black,
                radius = radius + strokeWidth / 2,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color.Black,
                radius = radius - strokeWidth / 2,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Center text showing total
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelSmall,
                color = NeoColors.OtherGray
            )
            Text(
                text = CurrencyFormatter.formatCompact(totalAmount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeoColors.PureBlack
            )
        }
    }
}

@Composable
private fun CategoryRow(
    categoryData: CategoryData,
    isExpense: Boolean,
    onClick: () -> Unit
) {
    val categoryColor = CategoryUtils.getColor(categoryData.category)
    val categoryIcon = CategoryUtils.getIcon(categoryData.category)

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
                    .size(36.dp)
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
                    text = CategoryUtils.getDisplayName(categoryData.category),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = NeoColors.PureBlack
                )
                Text(
                    text = "${categoryData.transactionCount} transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeoColors.MediumGray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatCompact(categoryData.amount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) NeoColors.ExpenseRed else NeoColors.IncomeGreen
                )
                Text(
                    text = "${categoryData.percentage.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeoColors.MediumGray
                )
            }

            Spacer(modifier = Modifier.width(NeoSpacing.xs))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View transactions",
                modifier = Modifier.size(NeoDimens.iconSizeMedium),
                tint = NeoColors.MediumGray
            )
        }
    }
}

@Composable
private fun EmptyChartState(message: String) {
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
