package com.diajarkoding.duittracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoDimens
import com.diajarkoding.duittracker.ui.theme.NeoSpacing

@Composable
fun NeoSkeletonBox(
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 20.dp,
    cornerRadius: Dp = NeoDimens.cornerRadiusSmall
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    Box(
        modifier = modifier
            .size(width, height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(NeoColors.LightGray.copy(alpha = alpha))
    )
}

@Composable
fun NeoSkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(NeoColors.LightGray.copy(alpha = alpha))
    )
}

@Composable
fun NeoSkeletonRoundedBox(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    cornerRadius: Dp = NeoDimens.cornerRadiusSmall
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(NeoColors.LightGray.copy(alpha = alpha))
    )
}

@Composable
fun NeoSkeletonTransactionItem(
    modifier: Modifier = Modifier
) {
    NeoCardFlat(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = NeoDimens.cornerRadius,
        borderWidth = NeoDimens.borderWidth
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NeoSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeoSkeletonRoundedBox(size = 40.dp)

            Spacer(modifier = Modifier.width(NeoSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                NeoSkeletonBox(width = 100.dp, height = 14.dp)
                Spacer(modifier = Modifier.height(NeoSpacing.xs))
                NeoSkeletonBox(width = 80.dp, height = 12.dp)
            }

            NeoSkeletonBox(width = 60.dp, height = 14.dp)
        }
    }
}

@Composable
fun NeoSkeletonSummaryCard(
    modifier: Modifier = Modifier
) {
    NeoCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = NeoColors.PureWhite,
        shadowOffset = NeoDimens.shadowOffset,
        cornerRadius = NeoDimens.cornerRadius
    ) {
        Column(modifier = Modifier.padding(NeoSpacing.lg)) {
            // Month and Balance row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    NeoSkeletonBox(width = 80.dp, height = 12.dp)
                    Spacer(modifier = Modifier.height(NeoSpacing.xs))
                    NeoSkeletonBox(width = 140.dp, height = 28.dp)
                }
                NeoSkeletonBox(width = 60.dp, height = 20.dp, cornerRadius = NeoDimens.cornerRadiusSmall)
            }

            Spacer(modifier = Modifier.height(NeoSpacing.lg))

            // Income and Expense cards - Neobrutalism style
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md)
            ) {
                NeoCardFlat(
                    modifier = Modifier.weight(1f),
                    backgroundColor = NeoColors.IncomeGreen.copy(alpha = 0.6f),
                    cornerRadius = NeoDimens.cornerRadiusSmall,
                    borderWidth = NeoDimens.borderWidth
                ) {
                    Column(modifier = Modifier.padding(NeoSpacing.md)) {
                        NeoSkeletonBox(width = 50.dp, height = 10.dp)
                        Spacer(modifier = Modifier.height(NeoSpacing.xs))
                        NeoSkeletonBox(width = 80.dp, height = 16.dp)
                    }
                }
                NeoCardFlat(
                    modifier = Modifier.weight(1f),
                    backgroundColor = NeoColors.ExpenseRed.copy(alpha = 0.6f),
                    cornerRadius = NeoDimens.cornerRadiusSmall,
                    borderWidth = NeoDimens.borderWidth
                ) {
                    Column(modifier = Modifier.padding(NeoSpacing.md)) {
                        NeoSkeletonBox(width = 50.dp, height = 10.dp)
                        Spacer(modifier = Modifier.height(NeoSpacing.xs))
                        NeoSkeletonBox(width = 80.dp, height = 16.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun NeoSkeletonCategoryRow(
    modifier: Modifier = Modifier
) {
    NeoCardFlat(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = NeoDimens.cornerRadius,
        borderWidth = NeoDimens.borderWidth
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NeoSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeoSkeletonRoundedBox(size = 36.dp)

            Spacer(modifier = Modifier.width(NeoSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                NeoSkeletonBox(width = 90.dp, height = 14.dp)
                Spacer(modifier = Modifier.height(NeoSpacing.xs))
                NeoSkeletonBox(width = 70.dp, height = 12.dp)
            }

            Column(horizontalAlignment = Alignment.End) {
                NeoSkeletonBox(width = 60.dp, height = 14.dp)
                Spacer(modifier = Modifier.height(NeoSpacing.xs))
                NeoSkeletonBox(width = 30.dp, height = 10.dp)
            }
        }
    }
}

@Composable
fun NeoSkeletonDashboard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = NeoSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(NeoSpacing.md)
    ) {
        // Summary card skeleton
        NeoSkeletonSummaryCard()

        // View mode toggle skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                .background(NeoColors.LightGray.copy(alpha = 0.3f))
                .padding(NeoSpacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NeoSpacing.xs)
            ) {
                NeoSkeletonBox(
                    modifier = Modifier.weight(1f),
                    width = 100.dp,
                    height = 36.dp,
                    cornerRadius = NeoDimens.cornerRadiusSmall
                )
                NeoSkeletonBox(
                    modifier = Modifier.weight(1f),
                    width = 100.dp,
                    height = 36.dp,
                    cornerRadius = NeoDimens.cornerRadiusSmall
                )
            }
        }

        // Date header skeleton
        Spacer(modifier = Modifier.height(NeoSpacing.xs))
        NeoSkeletonBox(width = 100.dp, height = 12.dp)

        // Transaction items skeleton
        repeat(4) {
            NeoSkeletonTransactionItem()
        }
    }
}

@Composable
fun NeoSkeletonTransactionDetail(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(NeoSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(NeoSpacing.md)
    ) {
        // Amount card skeleton
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
                NeoSkeletonBox(width = 60.dp, height = 20.dp, cornerRadius = NeoDimens.cornerRadiusSmall)
                Spacer(modifier = Modifier.height(NeoSpacing.sm))
                NeoSkeletonBox(width = 140.dp, height = 32.dp)
            }
        }

        // Category skeleton
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
                NeoSkeletonRoundedBox(size = 48.dp)
                Column {
                    NeoSkeletonBox(width = 60.dp, height = 10.dp)
                    Spacer(modifier = Modifier.height(NeoSpacing.xs))
                    NeoSkeletonBox(width = 100.dp, height = 14.dp)
                }
            }
        }

        // Details skeleton
        NeoCardFlat(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = NeoDimens.cornerRadius
        ) {
            Column(
                modifier = Modifier.padding(NeoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(NeoSpacing.lg)
            ) {
                repeat(5) {
                    Column {
                        NeoSkeletonBox(width = 50.dp, height = 10.dp)
                        Spacer(modifier = Modifier.height(NeoSpacing.xs))
                        NeoSkeletonBox(width = 120.dp, height = 14.dp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(NeoSpacing.md))

        // Delete button skeleton
        NeoSkeletonBox(
            modifier = Modifier.fillMaxWidth(),
            width = 200.dp,
            height = 48.dp,
            cornerRadius = NeoDimens.cornerRadius
        )
    }
}

@Composable
fun NeoSkeletonStatistics(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = NeoSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(NeoSpacing.md)
    ) {
        // Month selector skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(NeoDimens.cornerRadius))
                .background(NeoColors.PureWhite)
                .padding(horizontal = NeoSpacing.sm, vertical = NeoSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeoSkeletonCircle(size = 24.dp)
                NeoSkeletonBox(width = 100.dp, height = 18.dp)
                NeoSkeletonCircle(size = 24.dp)
            }
        }

        // Summary cards skeleton - Neobrutalism style
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md)
        ) {
            NeoCardFlat(
                modifier = Modifier.weight(1f),
                backgroundColor = NeoColors.ExpenseRed.copy(alpha = 0.6f),
                cornerRadius = NeoDimens.cornerRadiusSmall,
                borderWidth = NeoDimens.borderWidth
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NeoSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NeoSkeletonBox(width = 50.dp, height = 10.dp)
                    Spacer(modifier = Modifier.height(NeoSpacing.xs))
                    NeoSkeletonBox(width = 70.dp, height = 16.dp)
                }
            }
            NeoCardFlat(
                modifier = Modifier.weight(1f),
                backgroundColor = NeoColors.IncomeGreen.copy(alpha = 0.6f),
                cornerRadius = NeoDimens.cornerRadiusSmall,
                borderWidth = NeoDimens.borderWidth
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NeoSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NeoSkeletonBox(width = 50.dp, height = 10.dp)
                    Spacer(modifier = Modifier.height(NeoSpacing.xs))
                    NeoSkeletonBox(width = 70.dp, height = 16.dp)
                }
            }
        }

        // Section title skeleton
        Spacer(modifier = Modifier.height(NeoSpacing.xs))
        NeoSkeletonBox(width = 120.dp, height = 12.dp)

        // Pie chart placeholder skeleton
        NeoCardFlat(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = NeoDimens.cornerRadius
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NeoSpacing.xl),
                contentAlignment = Alignment.Center
            ) {
                NeoSkeletonCircle(size = 180.dp)
            }
        }

        // Category rows skeleton
        repeat(3) {
            NeoSkeletonCategoryRow()
        }
    }
}
