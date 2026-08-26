package com.vvv.openexpensetracker.presentation.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.vvv.openexpensetracker.domain.model.AppCurrency
import com.vvv.openexpensetracker.presentation.theme.AppTheme
import com.vvv.openexpensetracker.presentation.theme.ColorSuccess
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.flow.collectLatest
import openexpensetracker.shared.generated.resources.Res
import openexpensetracker.shared.generated.resources.at
import openexpensetracker.shared.generated.resources.llm_model_name
import openexpensetracker.shared.generated.resources.never
import openexpensetracker.shared.generated.resources.settings_ai_btn_delete
import openexpensetracker.shared.generated.resources.settings_ai_btn_enable
import openexpensetracker.shared.generated.resources.settings_ai_download_progress
import openexpensetracker.shared.generated.resources.settings_ai_performance_title
import openexpensetracker.shared.generated.resources.settings_ai_ready
import openexpensetracker.shared.generated.resources.settings_ai_scanner_desc
import openexpensetracker.shared.generated.resources.settings_ai_scanner_title
import openexpensetracker.shared.generated.resources.settings_ai_section_title
import openexpensetracker.shared.generated.resources.settings_ai_time_label
import openexpensetracker.shared.generated.resources.settings_ai_tps_label
import openexpensetracker.shared.generated.resources.settings_btn_sign_in
import openexpensetracker.shared.generated.resources.settings_btn_sign_out
import openexpensetracker.shared.generated.resources.settings_btn_sync
import openexpensetracker.shared.generated.resources.settings_guest_mode_subtitle
import openexpensetracker.shared.generated.resources.settings_guest_mode_title
import openexpensetracker.shared.generated.resources.settings_label_currency
import openexpensetracker.shared.generated.resources.settings_label_last_synced
import openexpensetracker.shared.generated.resources.settings_signed_in_user
import openexpensetracker.shared.generated.resources.settings_syncing
import openexpensetracker.shared.generated.resources.settings_title
import openexpensetracker.shared.generated.resources.settings_user_account
import openexpensetracker.shared.generated.resources.settings_version_info
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    snackbarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsState()
    val dimens = AppTheme.dimens
    val typography = MaterialTheme.typography
    val scrollState = rememberScrollState()

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
        isPlaying = uiState.isSyncing,
    )

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SettingsUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(Res.string.settings_title), fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(dimens.spacingNormal)
        ) {
            // Profile Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimens.cornerRadiusLarge),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.cornerRadiusExtraLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimens.categoryIconSize - dimens.spacingSmall)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = stringResource(Res.string.settings_user_account),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(dimens.iconSizeExtraLarge - dimens.spacingSmall)
                        )
                    }

                    Spacer(modifier = Modifier.height(dimens.spacingNormal))

                    if (uiState.isSignedIn) {
                        Text(
                            text = uiState.userName ?: stringResource(Res.string.settings_signed_in_user),
                            style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(dimens.spacingExtraSmall))
                        Text(
                            text = uiState.userEmail ?: "",
                            style = typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.settings_guest_mode_title),
                            style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(dimens.spacingExtraSmall))
                        Text(
                            text = stringResource(Res.string.settings_guest_mode_subtitle),
                            style = typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.spacingLarge))

            // AI Features Section
            Text(
                text = stringResource(Res.string.settings_ai_section_title),
                style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = dimens.spacingSmall)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimens.cornerRadiusLarge),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(dimens.spacingNormal)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(dimens.iconSizeNormal)
                        )
                        Spacer(modifier = Modifier.width(dimens.spacingSmallIntermediate))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(Res.string.settings_ai_scanner_title),
                                style = typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = stringResource(Res.string.settings_ai_scanner_desc),
                                style = typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimens.spacingSmall))

                    if (uiState.isLlmDownloaded) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(Res.string.llm_model_name),
                                    style = typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = dimens.spacingSmall)
                                )
                                TextButton(
                                    onClick = { viewModel.onIntent(SettingsIntent.DeleteLlmModel) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(dimens.iconSizeStatus)
                                    )
                                    Spacer(modifier = Modifier.width(dimens.spacingExtraSmall))
                                    Text(stringResource(Res.string.settings_ai_btn_delete))
                                }
                            }

                            // Performance Metrics
                            Text(
                                text = stringResource(Res.string.settings_ai_performance_title),
                                style = typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Column(modifier = Modifier.padding(vertical = dimens.spacingExtraSmall)) {
                                Text(
                                    text = stringResource(
                                        Res.string.settings_ai_tps_label,
                                        uiState.formattedTps
                                    ),
                                    style = typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(
                                        Res.string.settings_ai_time_label,
                                        uiState.formattedScanTime
                                    ),
                                    style = typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else if (uiState.isLlmDownloading) {
                        Column {
                            LinearProgressIndicator(
                                progress = { uiState.llmDownloadProgress },
                                modifier = Modifier.fillMaxWidth().height(dimens.progressBarHeight)
                                    .clip(RoundedCornerShape(dimens.spacingExtraSmall)),
                            )
                            Spacer(modifier = Modifier.height(dimens.spacingSmall))
                            Text(
                                text = stringResource(
                                    Res.string.settings_ai_download_progress,
                                    (uiState.llmDownloadProgress * 100).toInt()
                                ),
                                style = typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.onIntent(SettingsIntent.DownloadLlmModel) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(dimens.cornerRadiusNormal)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(dimens.iconSizeStatus)
                            )
                            Spacer(modifier = Modifier.width(dimens.spacingSmall))
                            Text(stringResource(Res.string.settings_ai_btn_enable))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.spacingLarge))

            // Currency Selector Section
            Text(
                text = stringResource(Res.string.settings_label_currency),
                style = typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = dimens.spacingSmall)
            )
            var expanded by remember { mutableStateOf(value = false) }
            val currencies = AppCurrency.entries
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${uiState.currency.name} (${uiState.currency.symbol})")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    currencies.forEach { cur ->
                        DropdownMenuItem(
                            text = { Text("${cur.name} (${cur.symbol})") },
                            onClick = {
                                viewModel.onIntent(SettingsIntent.SetCurrency(cur))
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.spacingLarge))

            // Action controls
            Text(
                text = stringResource(Res.string.settings_syncing),
                style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = dimens.spacingExtraSmall, bottom = dimens.spacingSmall)
            )

            if (uiState.isSignedIn) {
                // Sync Now Button
                Button(
                    onClick = { viewModel.onIntent(SettingsIntent.SyncNow) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.buttonHeight),
                    shape = RoundedCornerShape(dimens.cornerRadiusLarge),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !uiState.isSyncing
                ) {
                    if (uiState.isSyncing) {
                        Image(
                            painter = rememberLottiePainter(
                                composition = composition,
                                progress = { lottieProgress }
                            ),
                            contentDescription = stringResource(Res.string.settings_syncing),
                            modifier = Modifier.size(dimens.iconSizeNormal * 1.5f)
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(dimens.spacingSmall))
                        Text(
                            stringResource(Res.string.settings_btn_sync),
                            style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimens.spacingNormal - dimens.spacingExtraSmall))

                // Sync log details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.spacingExtraSmall),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.settings_label_last_synced),
                        style = typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = viewModel.formatLastSyncTime(
                            uiState.lastSyncTime,
                            stringResource(Res.string.never),
                            stringResource(Res.string.at),
                            viewModel::formatDate
                        ),
                        style = typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(dimens.spacingLarge))

                // Sign Out Button
                OutlinedButton(
                    onClick = { viewModel.onIntent(SettingsIntent.SignOut) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.buttonHeight),
                    shape = RoundedCornerShape(dimens.cornerRadiusLarge),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    )
                ) {
                    Text(
                        stringResource(Res.string.settings_btn_sign_out),
                        style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                // Sign In Button
                Button(
                    onClick = { viewModel.onIntent(SettingsIntent.SignIn) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.buttonHeight),
                    shape = RoundedCornerShape(dimens.cornerRadiusLarge),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        stringResource(Res.string.settings_btn_sign_in),
                        style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.spacingLarge))

            // About footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimens.spacingSmall),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(dimens.iconSizeSmall),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(dimens.spacingExtraSmall))
                    Text(
                        text = stringResource(Res.string.settings_version_info),
                        style = typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
