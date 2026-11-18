package com.sadsha.dnsvpn.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sadsha.dnsvpn.R
import com.sadsha.dnsvpn.ui.components.sdv.cards.SdvCardHigh
import com.sadsha.dnsvpn.ui.components.sdv.cards.SdvCardLow
import com.sadsha.dnsvpn.ui.theme.ConnectedGreen
import com.sadsha.dnsvpn.ui.theme.ConnectingAmber
import com.sadsha.dnsvpn.ui.theme.DisconnectedRed
import com.sadsha.dnsvpn.ui.theme.SdvColor
import com.sadsha.dnsvpn.ui.theme.SdvSpace
import com.sadsha.dnsvpn.util.TrafficFormatters
import com.sadsha.dnsvpn.util.VpnManager

@Composable
fun SdvConnectionTelemetryCard(
    vpnState: VpnManager.VpnState,
    proxyHost: String,
    proxyPort: Int,
    socksAuthEnabled: Boolean,
    socksUser: String,
    socksPass: String,
    isConnecting: Boolean,
    connectionWarning: String? = null
) {
    val scanStatus by VpnManager.scanStatus.collectAsState()
    val upBps by VpnManager.uploadSpeedBps.collectAsState()
    val downBps by VpnManager.downloadSpeedBps.collectAsState()
    val uploadTotalBytes by VpnManager.uploadTotalBytes.collectAsState()
    val downloadTotalBytes by VpnManager.downloadTotalBytes.collectAsState()
    val connectedDurationSeconds by VpnManager.connectedDurationSeconds.collectAsState()
    val totalResolvers = scanStatus.scanTotalFromCore
    val scannedCount = scanStatus.validCount + scanStatus.rejectedCount
    val scanProgress = if (totalResolvers > 0) {
        (scannedCount.toFloat() / totalResolvers.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    SdvCardLow(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SdvSpace.S4)
        ) {
            // Header Row: Status and Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_connection_status_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = SdvColor.PrimaryDim,
                        fontWeight = FontWeight.Bold
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(2.dp))
                    val statusText = when (vpnState) {
                        VpnManager.VpnState.CONNECTED -> stringResource(R.string.home_connection_running)
                        VpnManager.VpnState.CONNECTING -> stringResource(R.string.home_connection_preparing)
                        VpnManager.VpnState.DISCONNECTING -> stringResource(R.string.home_state_disconnecting)
                        VpnManager.VpnState.ERROR -> stringResource(R.string.home_connection_error_check_logs)
                        else -> stringResource(R.string.home_state_disconnected)
                    }
                    val statusColor = when (vpnState) {
                        VpnManager.VpnState.CONNECTED -> ConnectedGreen
                        VpnManager.VpnState.ERROR -> DisconnectedRed
                        else -> SdvColor.OnSurface
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (vpnState == VpnManager.VpnState.CONNECTING) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.home_connection_tap_to_cancel),
                            style = MaterialTheme.typography.bodySmall,
                            color = SdvColor.OnSurfaceVariant
                        )
                    }
                }

                if (connectedDurationSeconds > 0 ||
                    vpnState == VpnManager.VpnState.CONNECTED ||
                    vpnState == VpnManager.VpnState.CONNECTING
                ) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = stringResource(R.string.home_status_session),
                            style = MaterialTheme.typography.labelSmall,
                            color = SdvColor.OnSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatDuration(connectedDurationSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            color = SdvColor.OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Scanning Progress Section
            if (scanStatus.scanning || isConnecting) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SdvSpace.S3))
                androidx.compose.material3.Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    color = SdvColor.SurfaceHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(SdvSpace.S3)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.home_status_dns_scan),
                                style = MaterialTheme.typography.labelMedium,
                                color = SdvColor.OnSurfaceVariant
                            )
                            Text(
                                text = "$scannedCount / $totalResolvers",
                                style = MaterialTheme.typography.labelMedium,
                                color = SdvColor.PrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { scanProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = SdvColor.PrimaryContainer,
                            trackColor = SdvColor.SurfaceBright,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        if (scanStatus.validCount > 0 || scanStatus.rejectedCount > 0) {
                            val validLabel = stringResource(R.string.home_status_valid)
                            val rejectedLabel = stringResource(R.string.home_status_rejected)
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        append(validLabel)
                                        append(" ")
                                        pushStyle(SpanStyle(color = ConnectedGreen, fontWeight = FontWeight.Bold))
                                        append(scanStatus.validCount.toString())
                                        pop()
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = buildAnnotatedString {
                                        append(rejectedLabel)
                                        append(" ")
                                        pushStyle(SpanStyle(color = DisconnectedRed, fontWeight = FontWeight.Bold))
                                        append(scanStatus.rejectedCount.toString())
                                        pop()
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (scanStatus.lastResolver.isNotBlank()) {
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${scanStatus.lastResolver} • ${scanStatus.lastDecision}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SdvColor.OnSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Connection Warning Section
            if (connectionWarning != null && vpnState == VpnManager.VpnState.CONNECTING) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SdvSpace.S3))
                androidx.compose.material3.Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    color = ConnectingAmber.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ConnectingAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = SdvSpace.S3, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠",
                            style = MaterialTheme.typography.titleMedium,
                            color = ConnectingAmber,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = connectionWarning,
                            style = MaterialTheme.typography.bodySmall,
                            color = ConnectingAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Network Traffic Grid
            if (vpnState == VpnManager.VpnState.CONNECTED || downloadTotalBytes > 0 || uploadTotalBytes > 0 || downBps > 0 || upBps > 0) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SdvSpace.S3))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SdvSpace.S3)
                ) {
                    // Download Column
                    androidx.compose.material3.Surface(
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        color = SdvColor.SurfaceHigh
                    ) {
                        Column(
                            modifier = Modifier.padding(SdvSpace.S3),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.home_status_download),
                                style = MaterialTheme.typography.labelSmall,
                                color = ConnectedGreen,
                                fontWeight = FontWeight.Bold
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatSpeed(downBps),
                                style = MaterialTheme.typography.titleMedium,
                                color = SdvColor.OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatBytes(downloadTotalBytes),
                                style = MaterialTheme.typography.bodySmall,
                                color = SdvColor.OnSurfaceVariant
                            )
                        }
                    }

                    // Upload Column
                    androidx.compose.material3.Surface(
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        color = SdvColor.SurfaceHigh
                    ) {
                        Column(
                            modifier = Modifier.padding(SdvSpace.S3),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.home_status_upload),
                                style = MaterialTheme.typography.labelSmall,
                                color = SdvColor.PrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatSpeed(upBps),
                                style = MaterialTheme.typography.titleMedium,
                                color = SdvColor.OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatBytes(uploadTotalBytes),
                                style = MaterialTheme.typography.bodySmall,
                                color = SdvColor.OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Extra Info (MTU, Active Resolvers, SOCKS)
            if (vpnState == VpnManager.VpnState.CONNECTED) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SdvSpace.S3))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (scanStatus.activeResolvers > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.home_status_active_resolvers), style = MaterialTheme.typography.bodySmall, color = SdvColor.OnSurfaceVariant)
                            Text(scanStatus.activeResolvers.toString(), style = MaterialTheme.typography.bodySmall, color = SdvColor.OnSurface, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (scanStatus.syncedUploadMtu > 0 || scanStatus.syncedDownloadMtu > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.home_status_synced_mtu), style = MaterialTheme.typography.bodySmall, color = SdvColor.OnSurfaceVariant)
                            Text("${scanStatus.syncedUploadMtu} / ${scanStatus.syncedDownloadMtu}", style = MaterialTheme.typography.bodySmall, color = SdvColor.OnSurface, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.home_status_socks5_proxy), style = MaterialTheme.typography.bodySmall, color = SdvColor.OnSurfaceVariant)
                        Text("$proxyHost:$proxyPort", style = MaterialTheme.typography.bodySmall, color = SdvColor.OnSurface, fontWeight = FontWeight.Bold)
                    }
                    if (socksAuthEnabled && socksUser.isNotBlank()) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.home_status_socks5_auth), style = MaterialTheme.typography.bodySmall, color = SdvColor.OnSurfaceVariant)
                            Text(stringResource(R.string.home_status_enabled), style = MaterialTheme.typography.bodySmall, color = SdvColor.OnSurface, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.home_status_credentials), style = MaterialTheme.typography.bodySmall, color = SdvColor.OnSurfaceVariant)
                            Text("$socksUser:$socksPass", style = MaterialTheme.typography.bodySmall, color = SdvColor.OnSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SdvProfileSelectorCard(
    profileName: String,
    onNavigateToProfiles: () -> Unit
) {
    SdvCardHigh(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToProfiles)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SdvSpace.S4),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_profile_title),
                    style = MaterialTheme.typography.labelSmall,
                    color = SdvColor.OnSurfaceVariant
                )
                Text(
                    text = profileName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = SdvColor.OnSurface
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = SdvColor.PrimaryContainer
            )
        }
    }
}

@Composable
fun SdvErrorCard(msg: String) {
    SdvCardLow(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = msg,
            style = MaterialTheme.typography.bodyMedium,
            color = DisconnectedRed,
            modifier = Modifier.padding(SdvSpace.S3),
            textAlign = TextAlign.Center
        )
    }
}

private fun formatSpeed(bps: Long): String = TrafficFormatters.formatSpeed(bps)
private fun formatBytes(bytes: Long): String = TrafficFormatters.formatBytes(bytes)
private fun formatDuration(seconds: Long): String = TrafficFormatters.formatDuration(seconds)
