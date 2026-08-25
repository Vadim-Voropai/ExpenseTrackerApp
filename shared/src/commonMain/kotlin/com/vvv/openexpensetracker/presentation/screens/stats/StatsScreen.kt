package com.vvv.openexpensetracker.presentation.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.vvv.openexpensetracker.presentation.theme.AppTheme
import com.vvv.openexpensetracker.presentation.theme.getCategoryColor
import com.vvv.openexpensetracker.presentation.theme.getCategoryIcon
import com.vvv.openexpensetracker.presentation.theme.getCategoryNameResource
import openexpensetracker.shared.generated.resources.Res
import openexpensetracker.shared.generated.resources.stats_breakdown_title
import openexpensetracker.shared.generated.resources.stats_empty_message
import openexpensetracker.shared.generated.resources.stats_label_total_spent
import openexpensetracker.shared.generated.resources.stats_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val dimens = AppTheme.dimens
    val typography = MaterialTheme.typography

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(Res.string.stats_title), fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimens.spacingNormal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.totalSpent == 0.0) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.stats_empty_message),
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
                            text = stringResource(Res.string.stats_label_total_spent),
                            style = typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(dimens.spacingExtraSmall))
                        Text(
                            text = "${uiState.currency.symbol}${viewModel.formatAmount(uiState.totalSpent)}",
                            style = typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimens.spacingLarge))

                // Breakdown list
                Text(
                    text = stringResource(Res.string.stats_breakdown_title),
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
                            percentage = (entry.value / uiState.totalSpent) * 100,
                            currencySymbol = uiState.currency.symbol,
                            formatAmount = viewModel::formatAmount
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
    percentage: Double,
    currencySymbol: String,
    formatAmount: (Double) -> String
) {
    val dimens = AppTheme.dimens
    val typography = MaterialTheme.typography
    val color = getCategoryColor(category)
    val nameRes = getCategoryNameResource(category)
    
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
                    contentDescription = stringResource(nameRes),
                    tint = color,
                    modifier = Modifier.size(dimens.iconSizeSmall + dimens.spacingExtraSmall)
                )
                Spacer(modifier = Modifier.width(dimens.spacingSmall))
                // Title
                Text(
                    text = stringResource(nameRes),
                    style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            // Amount and percent
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${currencySymbol}${formatAmount(amount)}",
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
