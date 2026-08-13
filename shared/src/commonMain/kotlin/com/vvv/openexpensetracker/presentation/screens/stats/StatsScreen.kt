package com.vvv.openexpensetracker.presentation.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.vvv.openexpensetracker.presentation.screens.expenses.formatAmount
import com.vvv.openexpensetracker.presentation.theme.AppTheme
import com.vvv.openexpensetracker.presentation.theme.getCategoryColor
import com.vvv.openexpensetracker.presentation.theme.getCategoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val dimens = AppTheme.dimens
    val typography = MaterialTheme.typography

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimens.spacingNormal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.totalSpent == 0.0) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add some expenses to see analytics.",
                        style = typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(dimens.spacingNormal))

                // Canvas Donut Chart
                Box(
                    modifier = Modifier
                        .size(dimens.chartSize)
                        .padding(dimens.spacingNormal),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(categoryTotals = uiState.categoryTotals, totalSpent = uiState.totalSpent)
                    
                    // Inside Circle Texts
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Total Spent",
                            style = typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(dimens.spacingExtraSmall))
                        Text(
                            text = "$${formatAmount(uiState.totalSpent)}",
                            style = typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimens.spacingLarge))

                // Breakdown list
                Text(
                    text = "Category Breakdown",
                    style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(dimens.spacingSmall))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall),
                    contentPadding = PaddingValues(bottom = dimens.spacingNormal)
                ) {
                    items(uiState.categoryTotals.entries.sortedByDescending { it.value }) { entry ->
                        CategoryBreakdownRow(
                            category = entry.key,
                            amount = entry.value,
                            percentage = (entry.value / uiState.totalSpent) * 100
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChart(
    categoryTotals: Map<String, Double>,
    totalSpent: Double,
    modifier: Modifier = Modifier
) {
    val dimens = AppTheme.dimens
    val items = categoryTotals.entries.toList()
    Canvas(modifier = modifier.fillMaxSize()) {
        var startAngle = -90f
        val strokeWidth = dimens.spacingExtraLarge.toPx()

        if (totalSpent == 0.0) {
            drawCircle(
                color = Color.LightGray,
                style = Stroke(width = strokeWidth)
            )
        } else {
            items.forEach { entry ->
                val sweepAngle = ((entry.value / totalSpent) * 360f).toFloat()
                drawArc(
                    color = getCategoryColor(entry.key),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }
    }
}

@Composable
fun CategoryBreakdownRow(
    category: String,
    amount: Double,
    percentage: Double
) {
    val dimens = AppTheme.dimens
    val typography = MaterialTheme.typography
    val color = getCategoryColor(category)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimens.cornerRadiusNormal),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.cornerRadiusNormal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Colored Circle
                Box(
                    modifier = Modifier
                        .size(dimens.spacingNormal)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(dimens.cornerRadiusNormal))
                // Icon
                Icon(
                    imageVector = getCategoryIcon(category),
                    contentDescription = category,
                    tint = color,
                    modifier = Modifier.size(dimens.iconSizeSmall + dimens.spacingExtraSmall)
                )
                Spacer(modifier = Modifier.width(dimens.spacingSmall))
                // Title
                Text(
                    text = category,
                    style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            // Amount and percent
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${formatAmount(amount)}",
                    style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${percentage.toInt()}%",
                    style = typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
