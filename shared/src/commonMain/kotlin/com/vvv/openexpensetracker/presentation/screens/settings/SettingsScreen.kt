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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.vvv.openexpensetracker.presentation.screens.expenses.formatDate
import com.vvv.openexpensetracker.presentation.theme.AppTheme
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import openexpensetracker.shared.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
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
        isPlaying = uiState.isSyncing,
    )

    LaunchedEffect(uiState.syncMessage) {
        uiState.syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title), fontWeight = FontWeight.Bold) },
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
                modifier = Modifier.fillMaxWidth()
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
                        // User Avatar Placeholder / Icon
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
                                    viewModel.setCurrency(cur)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimens.spacingLarge))

                // Action controls
                Text(
                    text = stringResource(Res.string.settings_sync_section_title),
                    style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = dimens.spacingExtraSmall, bottom = dimens.spacingSmall)
                )

                if (uiState.isSignedIn) {
                    // Sync Now Button
                    Button(
                        onClick = { viewModel.syncNow() },
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
                            Text(stringResource(Res.string.settings_btn_sync), style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
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
                            text = formatLastSyncTime(uiState.lastSyncTime),
                            style = typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(dimens.spacingLarge))

                    // Sign Out Button
                    OutlinedButton(
                        onClick = { viewModel.signOut() },
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
                        Text(stringResource(Res.string.settings_btn_sign_out), style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                } else {
                    // Sign In Button
                    Button(
                        onClick = { viewModel.signIn() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimens.buttonHeight),
                        shape = RoundedCornerShape(dimens.cornerRadiusLarge),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(Res.string.settings_btn_sign_in), style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

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

@Composable
fun formatLastSyncTime(timestamp: Long): String {
    if (timestamp == 0L) return stringResource(Res.string.never)
    
    val datePart = formatDate(timestamp)
    val timePart = try {
        val instant = kotlin.time.Instant.fromEpochMilliseconds(timestamp)
        val tz = TimeZone.currentSystemDefault()
        val localDateTime = instant.toLocalDateTime(tz)
        "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
    } catch (_: Exception) {
        ""
    }
    
    return if (timePart.isNotEmpty()) {
        "$datePart ${stringResource(Res.string.at)} $timePart"
    } else {
        datePart
    }
}
