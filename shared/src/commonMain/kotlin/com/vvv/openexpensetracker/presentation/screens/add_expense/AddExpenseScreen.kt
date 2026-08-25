package com.vvv.openexpensetracker.presentation.screens.add_expense

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.vvv.openexpensetracker.domain.model.Category
import com.vvv.openexpensetracker.presentation.theme.AppTheme
import com.vvv.openexpensetracker.presentation.theme.getCategoryColor
import com.vvv.openexpensetracker.presentation.theme.getCategoryIcon
import com.vvv.openexpensetracker.presentation.theme.getCategoryNameResource
import openexpensetracker.shared.generated.resources.Res
import openexpensetracker.shared.generated.resources.action_save
import openexpensetracker.shared.generated.resources.action_scan_receipt
import openexpensetracker.shared.generated.resources.add_btn_set_today
import openexpensetracker.shared.generated.resources.add_error_amount
import openexpensetracker.shared.generated.resources.add_label_amount
import openexpensetracker.shared.generated.resources.add_label_category
import openexpensetracker.shared.generated.resources.add_label_date
import openexpensetracker.shared.generated.resources.add_label_description
import openexpensetracker.shared.generated.resources.add_placeholder_amount
import openexpensetracker.shared.generated.resources.add_placeholder_description
import openexpensetracker.shared.generated.resources.add_title
import openexpensetracker.shared.generated.resources.back
import openexpensetracker.shared.generated.resources.edit_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    viewModel: AddExpenseViewModel,
    expenseId: String?,
    navigateBack: () -> Unit,
    onNavigateToScan: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dimens = AppTheme.dimens
    val typography = MaterialTheme.typography
    val scrollState = rememberScrollState()

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(expenseId) {
        viewModel.onIntent(AddExpenseIntent.LoadExpense(expenseId))
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.onIntent(AddExpenseIntent.ResetSaveState)
            navigateBack()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = { 
                Text(
                    if (expenseId == null) 
                        stringResource(Res.string.add_title) 
                    else 
                        stringResource(Res.string.edit_title)
                ) 
            },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                }
            },
            actions = {
                IconButton(
                    onClick = onNavigateToScan,
                    enabled = uiState.isScanEnabled
                ) {
                    Icon(
                        imageVector = Icons.Default.Add, // Placeholder for camera
                        contentDescription = stringResource(Res.string.action_scan_receipt),
                        tint = if (uiState.isScanEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
                TextButton(onClick = { viewModel.onIntent(AddExpenseIntent.SaveExpense) }) {
                    Text(
                        text = stringResource(Res.string.action_save),
                        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(dimens.spacingNormal),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingLarge)
        ) {
            // Amount Input Section
            Column {
                Text(
                    text = stringResource(Res.string.add_label_amount),
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(dimens.spacingExtraSmall))
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = { viewModel.onIntent(AddExpenseIntent.AmountChanged(it)) },
                    placeholder = { Text(stringResource(Res.string.add_placeholder_amount), style = typography.headlineMedium) },
                    prefix = { Text(uiState.currency.symbol, style = typography.headlineMedium) },
                    textStyle = typography.headlineMedium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(dimens.cornerRadiusLarge),
                    isError = uiState.amountError != null,
                    supportingText = {
                        if (uiState.amountError != null) {
                            Text(stringResource(Res.string.add_error_amount), color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true
                )
            }

            // Description Input Section
            Column {
                Text(
                    text = stringResource(Res.string.add_label_description),
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(dimens.spacingExtraSmall))
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onIntent(AddExpenseIntent.DescriptionChanged(it)) },
                    placeholder = { Text(stringResource(Res.string.add_placeholder_description)) },
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
            }

            // Date Picker Section
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
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(dimens.cornerRadiusNormal))
                    Column {
                        Text(
                            stringResource(Res.string.add_label_date),
                            style = typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = viewModel.formatDate(uiState.date),
                            style = typography.titleMedium
                        )
                    }
                }
                TextButton(onClick = {
                    viewModel.onIntent(AddExpenseIntent.DateChanged(
                        kotlin.time.Clock.System.now().toEpochMilliseconds()
                    ))
                }) {
                    Text(stringResource(Res.string.add_btn_set_today))
                }
            }

            // Category Selector Section
            Column {
                Text(
                    text = stringResource(Res.string.add_label_category),
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(dimens.spacingSmall))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingSmall),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall),
                    maxItemsInEachRow = 3
                ) {
                    Category.list.forEach { cat ->
                        val isSelected = uiState.category == cat
                        val color = getCategoryColor(cat)
                        val nameRes = getCategoryNameResource(cat)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
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
                                .clickable { viewModel.onIntent(AddExpenseIntent.CategoryChanged(cat)) },
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
                                        contentDescription = stringResource(nameRes),
                                        tint = if (isSelected) Color.White else color,
                                        modifier = Modifier.size(dimens.iconSizeNormal)
                                    )
                                }
                                Spacer(modifier = Modifier.height(dimens.spacingExtraSmall))
                                Text(
                                    text = stringResource(nameRes),
                                    style = typography.labelMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    
                    // Add spacers to fill the last row if not full (optional but helps alignment)
                    val remaining = 3 - (Category.list.size % 3)
                    if (remaining < 3) {
                        repeat(remaining) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            
            // Extra bottom spacing to ensure we can scroll past everything
            Spacer(modifier = Modifier.height(dimens.spacingExtraLarge))
        }
    }
}
