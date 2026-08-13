package com.vvv.openexpensetracker.presentation.screens.add_expense

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.vvv.openexpensetracker.domain.model.Category
import com.vvv.openexpensetracker.presentation.screens.expenses.formatDate
import com.vvv.openexpensetracker.presentation.theme.AppTheme
import com.vvv.openexpensetracker.presentation.theme.getCategoryColor
import com.vvv.openexpensetracker.presentation.theme.getCategoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: AddExpenseViewModel,
    expenseId: String?,
    navigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dimens = AppTheme.dimens
    val typography = MaterialTheme.typography

    LaunchedEffect(expenseId) {
        viewModel.loadExpense(expenseId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (expenseId == null) "Add Expense" else "Edit Expense") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
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
                .padding(dimens.spacingNormal),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Large Amount Input
                Text(
                    text = "Amount",
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(dimens.spacingExtraSmall))
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = { viewModel.onAmountChanged(it) },
                    placeholder = { Text("0.00", style = typography.headlineMedium) },
                    prefix = { Text(uiState.currency.symbol, style = typography.headlineMedium) },
                    textStyle = typography.headlineMedium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimens.cornerRadiusLarge),
                    isError = uiState.amountError != null,
                    supportingText = {
                        if (uiState.amountError != null) {
                            Text(uiState.amountError ?: "", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(dimens.spacingNormal))

                // Description Input
                Text(
                    text = "Description",
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(dimens.spacingExtraSmall))
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onDescriptionChanged(it) },
                    placeholder = { Text("What did you buy?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimens.cornerRadiusLarge),
                    isError = uiState.descriptionError != null,
                    supportingText = {
                        if (uiState.descriptionError != null) {
                            Text(uiState.descriptionError ?: "", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(dimens.spacingNormal))

                // Date Picker Trigger (Inline simple selector for KMP simplicity)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimens.cornerRadiusLarge))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(dimens.spacingNormal),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(dimens.cornerRadiusNormal))
                        Column {
                            Text(
                                "Date",
                                style = typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatDate(uiState.date),
                                style = typography.titleMedium
                            )
                        }
                    }
                    TextButton(onClick = {
                        viewModel.onDateChanged(
                            kotlin.time.Clock.System.now().toEpochMilliseconds()
                        )
                    }) {
                        Text("Set Today")
                    }
                }

                Spacer(modifier = Modifier.height(dimens.spacingLarge))

                // Category Selector
                Text(
                    text = "Select Category",
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(dimens.spacingSmall))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingSmall),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall),
                    modifier = Modifier.weight(1f)
                ) {
                    items(Category.list) { cat ->
                        val isSelected = uiState.category == cat
                        val color = getCategoryColor(cat)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimens.categoryIconSize)
                                .clip(RoundedCornerShape(dimens.cornerRadiusLarge))
                                .background(
                                    if (isSelected) color.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                )
                                .border(
                                    width = if (isSelected) dimens.borderWidthSelected else dimens.borderWidthNormal,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(dimens.cornerRadiusLarge)
                                )
                                .clickable { viewModel.onCategoryChanged(cat) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(dimens.iconSizeLarge)
                                        .clip(CircleShape)
                                        .background(if (isSelected) color else color.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(cat),
                                        contentDescription = cat,
                                        tint = if (isSelected) Color.White else color,
                                        modifier = Modifier.size(dimens.iconSizeNormal)
                                    )
                                }
                                Spacer(modifier = Modifier.height(dimens.spacingExtraSmall))
                                Text(
                                    text = cat,
                                    style = typography.labelMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Save Button
            Button(
                onClick = { viewModel.saveExpense() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.buttonHeight),
                shape = RoundedCornerShape(dimens.cornerRadiusLarge),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Done, contentDescription = "Save")
                Spacer(modifier = Modifier.width(dimens.spacingSmall))
                Text("Save Expense", style = typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}
