package com.vvv.openexpensetracker.presentation.screens.expenses

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.vvv.openexpensetracker.domain.model.Category
import com.vvv.openexpensetracker.domain.model.Expense
import com.vvv.openexpensetracker.presentation.theme.AppTheme
import com.vvv.openexpensetracker.presentation.theme.getCategoryColor
import com.vvv.openexpensetracker.presentation.theme.getCategoryIcon
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import openexpensetracker.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseListViewModel,
    onNavigateToAddEdit: (String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dimens = AppTheme.dimens
    val typography = MaterialTheme.typography
    val snackbarHostState = remember { SnackbarHostState() }

    // Lottie Composition
    val composition by rememberLottieComposition {
        try {
            val bytes = Res.readBytes("files/sync_animation.json")
            LottieCompositionSpec.JsonString(bytes.decodeToString())
        } catch (e: Exception) {
            LottieCompositionSpec.JsonString("{}")
        }
    }

    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Int.MAX_VALUE,
        isPlaying = uiState.isRefreshing
    )

    LaunchedEffect(uiState.syncMessage) {
        uiState.syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = dimens.spacingNormal, vertical = dimens.spacingSmall)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "My Expenses",
                        style = typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = { viewModel.syncExpenses() }) {
                        if (uiState.isRefreshing) {
                            Image(
                                painter = rememberLottiePainter(
                                    composition = composition,
                                    progress = { lottieProgress }
                                ),
                                contentDescription = "Syncing",
                                modifier = Modifier.size(dimens.iconSizeNormal * 1.5f)
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync Now")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimens.spacingSmall))

                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search description...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimens.cornerRadiusNormal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(dimens.spacingNormal))

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingSmall),
                    contentPadding = PaddingValues(vertical = dimens.spacingExtraSmall)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory == null,
                            onClick = { viewModel.setCategoryFilter(null) },
                            label = { Text("All") },
                            shape = RoundedCornerShape(dimens.cornerRadiusExtraLarge)
                        )
                    }
                    items(Category.list) { category ->
                        val isSelected = uiState.selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setCategoryFilter(if (isSelected) null else category) },
                            label = { Text(category) },
                            shape = RoundedCornerShape(dimens.cornerRadiusExtraLarge),
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(category),
                                    contentDescription = category,
                                    modifier = Modifier.size(dimens.iconSizeSmall),
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else getCategoryColor(
                                        category
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.expenses.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(dimens.spacingNormal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No Expenses",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(dimens.iconSizeExtraLarge + dimens.spacingNormal)
                    )
                    Spacer(modifier = Modifier.height(dimens.spacingNormal))
                    Text(
                        text = if (uiState.searchQuery.isNotEmpty() || uiState.selectedCategory != null) "No matching expenses" else "No expenses yet",
                        style = typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(dimens.spacingSmall))
                    Text(
                        text = "Tap the '+' button to log a new purchase.",
                        style = typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = dimens.spacingNormal, vertical = dimens.spacingSmall),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall)
                ) {
                    items(uiState.expenses, key = { it.id }) { expense ->
                        ExpenseItemRow(
                            expense = expense,
                            currency = uiState.currency,
                            onEdit = { onNavigateToAddEdit(expense.id) },
                            onDelete = { viewModel.deleteExpense(expense.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItemRow(
    expense: Expense,
    currency: com.vvv.openexpensetracker.domain.model.AppCurrency,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dimens = AppTheme.dimens
    val typography = MaterialTheme.typography

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(dimens.cornerRadiusLarge),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.spacingNormal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon with soft background
            val catColor = getCategoryColor(expense.category)
            Box(
                modifier = Modifier
                    .size(dimens.iconSizeExtraLarge)
                    .clip(RoundedCornerShape(dimens.cornerRadiusNormal))
                    .background(catColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(expense.category),
                    contentDescription = expense.category,
                    tint = catColor,
                    modifier = Modifier.size(dimens.iconSizeNormal)
                )
            }

            Spacer(modifier = Modifier.width(dimens.spacingNormal))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.description,
                    style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(dimens.spacingExtraSmall))
                Text(
                    text = formatDate(expense.date),
                    style = typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Amount & Actions
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${currency.symbol}${formatAmount(expense.amount)}",
                    style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(dimens.spacingExtraSmall))
                Row(horizontalArrangement = Arrangement.End) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(dimens.iconSizeNormal)
                            .clickable { onDelete() }
                    )
                }
            }
        }
    }
}

// Helpers
fun formatDate(timestamp: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val tz = TimeZone.currentSystemDefault()
        val dateTime = instant.toLocalDateTime(tz)

        val month = dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        val day = dateTime.dayOfMonth.toString().padStart(2, '0')
        "$month $day, ${dateTime.year}"
    } catch (e: Exception) {
        "Unknown Date"
    }
}

fun formatAmount(amount: Double): String {
    val cents = ((amount - amount.toInt()) * 100).toInt()
    return if (cents == 0) {
        amount.toInt().toString()
    } else {
        "${amount.toInt()}.${cents.toString().padStart(2, '0')}"
    }
}
