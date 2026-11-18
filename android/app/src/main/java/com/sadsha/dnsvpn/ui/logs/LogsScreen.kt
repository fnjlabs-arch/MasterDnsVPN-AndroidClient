package com.sadsha.dnsvpn.ui.logs

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sadsha.dnsvpn.R
import com.sadsha.dnsvpn.ui.components.sdv.cards.SdvCardLow
import com.sadsha.dnsvpn.ui.components.sdv.controls.SdvBackTopAppBar
import com.sadsha.dnsvpn.ui.components.sdv.controls.SdvFilterChip
import com.sadsha.dnsvpn.ui.theme.SdvColor
import com.sadsha.dnsvpn.ui.theme.SdvRadius
import com.sadsha.dnsvpn.ui.theme.SdvSpace
import com.sadsha.dnsvpn.util.VpnManager
import java.util.Locale

private enum class LogFilter(val labelRes: Int) {
    ALL(R.string.logs_filter_all),
    CORE(R.string.logs_filter_core),
    ANDROID(R.string.logs_filter_android)
}

internal enum class LogSeverity {
    ERROR,
    WARN,
    INFO,
    DEBUG,
    VERBOSE,
    PLAIN
}

private data class ParsedLogFields(
    val timestamp: String?,
    val message: String,
    val severity: LogSeverity,
    val explicitSeverity: Boolean
)

internal data class UiLogItem(
    val key: String,
    val source: VpnManager.LogSource,
    val timestamp: String?,
    val severity: LogSeverity,
    val message: String,
    val details: List<String>
)

private data class LogStats(
    val total: Long,
    val errors: Long,
    val warnings: Long
)

private val stampPattern = Regex("^(\\d{4}[-/]\\d{2}[-/]\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{3})?)\\s*(.*)$")
private val explicitSeverityPattern = Regex("^\\[([A-Za-z]+)]\\s*(.*)$")

@Composable
fun LogsScreen(onBack: () -> Unit) {
    val logEntries by VpnManager.logEntries.collectAsState()
    val counters by VpnManager.logCounters.collectAsState()
    var activeFilter by remember { mutableStateOf(LogFilter.ALL) }
    val filteredLogs = remember(logEntries, activeFilter) {
        when (activeFilter) {
            LogFilter.ALL -> logEntries
            LogFilter.CORE -> logEntries.filter { it.source == VpnManager.LogSource.CORE }
            LogFilter.ANDROID -> logEntries.filter { it.source == VpnManager.LogSource.ANDROID }
        }
    }
    val uiLogItems by produceState(initialValue = emptyList<UiLogItem>(), key1 = filteredLogs) {
        value = withContext(Dispatchers.Default) { buildUiLogItems(filteredLogs) }
    }
    val stats by produceState(
        initialValue = LogStats(total = 0L, errors = 0L, warnings = 0L),
        key1 = uiLogItems,
        key2 = activeFilter,
        key3 = counters
    ) {
        value = withContext(Dispatchers.Default) {
            if (activeFilter == LogFilter.ALL) {
                LogStats(
                    total = counters.total,
                    errors = counters.errors,
                    warnings = counters.warnings
                )
            } else {
                buildLogStats(uiLogItems)
            }
        }
    }

    val listState = rememberLazyListState()
    var autoScrollEnabled by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val shareLogs: () -> Unit = {
        if (filteredLogs.isNotEmpty()) {
            scope.launch {
                val content = withContext(Dispatchers.IO) {
                    filteredLogs.joinToString("\n") { it.line }
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.logs_share_subject))
                    putExtra(Intent.EXTRA_TEXT, content)
                }
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.logs_share_chooser)))
            }
        }
    }

    LaunchedEffect(uiLogItems, autoScrollEnabled) {
        if (!autoScrollEnabled || uiLogItems.isEmpty()) return@LaunchedEffect
        listState.scrollToLatestItem(uiLogItems.lastIndex)
    }

    Scaffold(
        topBar = {
            SdvBackTopAppBar(
                title = stringResource(R.string.title_logs),
                onBack = onBack,
                actions = {
                    IconButton(onClick = shareLogs) {
                        Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.logs_share))
                    }
                    FilledTonalIconButton(onClick = {
                        autoScrollEnabled = !autoScrollEnabled
                    }) {
                        Icon(Icons.Filled.AutoMode, contentDescription = stringResource(R.string.logs_auto))
                    }
                    Text(
                        text = if (autoScrollEnabled) {
                            stringResource(R.string.logs_auto_on)
                        } else {
                            stringResource(R.string.logs_auto_off)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = SdvColor.OnSurfaceVariant,
                        modifier = Modifier.padding(end = SdvSpace.S1)
                    )
                    IconButton(onClick = { VpnManager.clearLogs() }) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = stringResource(R.string.logs_clear))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SdvColor.Background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SdvSpace.S2, vertical = SdvSpace.S2),
                horizontalArrangement = Arrangement.spacedBy(SdvSpace.S2)
            ) {
                LogFilter.entries.forEach { filter ->
                    SdvFilterChip(
                        selected = activeFilter == filter,
                        onClick = { activeFilter = filter },
                        label = stringResource(filter.labelRes)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SdvSpace.S2),
                horizontalArrangement = Arrangement.spacedBy(SdvSpace.S2)
            ) {
                LogStatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.logs_stat_total),
                    value = stats.total.toString(),
                    accent = SdvColor.Primary
                )
                LogStatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.logs_stat_errors),
                    value = stats.errors.toString(),
                    accent = SdvColor.Error
                )
                LogStatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.logs_stat_warnings),
                    value = stats.warnings.toString(),
                    accent = SdvColor.LogWarn
                )
            }

            if (uiLogItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(SdvSpace.S4),
                    contentAlignment = Alignment.Center
                ) {
                    SdvCardLow(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(SdvSpace.S4)) {
                            Text(
                                text = stringResource(R.string.logs_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = SdvColor.OnSurface
                            )
                            Text(
                                text = stringResource(R.string.logs_empty_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SdvColor.OnSurfaceVariant,
                                modifier = Modifier.padding(top = SdvSpace.S1)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(SdvSpace.S2),
                    verticalArrangement = Arrangement.spacedBy(SdvSpace.S2)
                ) {
                    items(items = uiLogItems, key = { it.key }, contentType = { "log_item" }) { item ->
                        LogEntryCard(item = item)
                    }
                }
            }
        }
    }
}

private suspend fun LazyListState.scrollToLatestItem(lastIndex: Int) {
    if (lastIndex < 0) return
    scrollToItem(lastIndex)
    val lastItem = layoutInfo.visibleItemsInfo.lastOrNull { it.index == lastIndex } ?: return
    val viewportBottom = layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding
    val overflow = lastItem.offset + lastItem.size - viewportBottom
    if (overflow > 0) scrollBy(overflow.toFloat())
}

@Composable
private fun LogStatCard(
    modifier: Modifier,
    title: String,
    value: String,
    accent: Color
) {
    SdvCardLow(modifier = modifier.heightIn(min = 64.dp)) {
        Column(modifier = Modifier.padding(horizontal = SdvSpace.S3, vertical = SdvSpace.S2)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = SdvColor.OnSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LogEntryCard(item: UiLogItem) {
    val sourceLabel = when (item.source) {
        VpnManager.LogSource.CORE -> stringResource(R.string.logs_filter_core)
        VpnManager.LogSource.ANDROID -> stringResource(R.string.logs_filter_android)
    }
    val (severityLabel, severityColor, severityContainer) = severityUi(item.severity)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        SdvCardLow(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SdvSpace.S3)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SdvSpace.S1),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToneChip(
                        label = severityLabel,
                        contentColor = severityColor,
                        containerColor = severityContainer
                    )
                    ToneChip(
                        label = sourceLabel,
                        contentColor = SdvColor.Primary,
                        containerColor = SdvColor.PrimaryContainer.copy(alpha = 0.16f)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                    if (!item.timestamp.isNullOrBlank()) {
                        Text(
                            text = item.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = SdvColor.OnSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDirection = TextDirection.Ltr,
                        textAlign = TextAlign.Start
                    ),
                    color = SdvColor.OnSurface,
                    modifier = Modifier.padding(top = SdvSpace.S2)
                )

                if (item.details.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = SdvSpace.S2),
                        shape = RoundedCornerShape(SdvRadius.Md),
                        color = SdvColor.SurfaceLowest
                    ) {
                        Text(
                            text = item.details.joinToString("\n"),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                textDirection = TextDirection.Ltr,
                                textAlign = TextAlign.Start
                            ),
                            color = SdvColor.OnSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = SdvSpace.S2, vertical = SdvSpace.S2)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToneChip(
    label: String,
    contentColor: Color,
    containerColor: Color
) {
    Surface(
        modifier = Modifier.widthIn(min = 52.dp),
        shape = RoundedCornerShape(SdvRadius.Sm),
        color = containerColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = SdvSpace.S2, vertical = SdvSpace.S1)
        )
    }
}

private fun severityUi(severity: LogSeverity): Triple<String, Color, Color> {
    return when (severity) {
        LogSeverity.ERROR -> Triple("ERROR", SdvColor.Error, SdvColor.ErrorContainer.copy(alpha = 0.38f))
        LogSeverity.WARN -> Triple("WARN", SdvColor.LogWarn, SdvColor.LogWarnContainer)
        LogSeverity.INFO -> Triple("INFO", SdvColor.LogInfo, SdvColor.LogInfoContainer)
        LogSeverity.DEBUG -> Triple("DEBUG", SdvColor.Secondary, SdvColor.SurfaceHighest)
        LogSeverity.VERBOSE -> Triple("TRACE", SdvColor.Secondary, SdvColor.SurfaceHighest)
        LogSeverity.PLAIN -> Triple("LOG", SdvColor.OnSurfaceVariant, SdvColor.SurfaceHighest)
    }
}

private fun buildLogStats(items: List<UiLogItem>): LogStats {
    val errors = items.count { it.severity == LogSeverity.ERROR }.toLong()
    val warnings = items.count { it.severity == LogSeverity.WARN }.toLong()
    return LogStats(total = items.size.toLong(), errors = errors, warnings = warnings)
}

internal fun buildUiLogItems(entries: List<VpnManager.LogEntry>): List<UiLogItem> {
    if (entries.isEmpty()) return emptyList()

    val result = mutableListOf<UiLogItem>()

    var pendingSource: VpnManager.LogSource? = null
    var pendingTimestamp: String? = null
    var pendingSeverity: LogSeverity = LogSeverity.PLAIN
    var pendingMessage: String = ""
    val pendingDetails = mutableListOf<String>()
    var pendingKey: String = ""

    fun flushPending() {
        val source = pendingSource ?: return
        result.add(
            UiLogItem(
                key = pendingKey,
                source = source,
                timestamp = pendingTimestamp,
                severity = pendingSeverity,
                message = pendingMessage,
                details = pendingDetails.toList()
            )
        )
        pendingSource = null
        pendingTimestamp = null
        pendingSeverity = LogSeverity.PLAIN
        pendingMessage = ""
        pendingDetails.clear()
        pendingKey = ""
    }

    entries.forEach { entry ->
        val parsed = parseLine(entry.line)
        val startsNewBlock = parsed.timestamp != null || parsed.explicitSeverity || pendingSource == null

        if (startsNewBlock) {
            flushPending()
            pendingSource = entry.source
            pendingTimestamp = parsed.timestamp
            pendingSeverity = parsed.severity
            pendingMessage = parsed.message
            pendingKey = entry.seq.toString()
        } else {
            pendingDetails.add(entry.line)
            pendingSeverity = prioritizeSeverity(pendingSeverity, parsed.severity)
        }
    }

    flushPending()
    return result
}

private fun parseLine(line: String): ParsedLogFields {
    val stampMatch = stampPattern.find(line)
    if (stampMatch != null) {
        val stamp = stampMatch.groupValues[1]
        val remainder = stampMatch.groupValues[2].trim()
        val levelMatch = explicitSeverityPattern.find(remainder)
        if (levelMatch != null) {
            val level = levelMatch.groupValues[1]
            val message = levelMatch.groupValues[2].ifBlank { remainder }
            return ParsedLogFields(
                timestamp = stamp,
                message = message,
                severity = toSeverity(level),
                explicitSeverity = true
            )
        }
        return ParsedLogFields(
            timestamp = stamp,
            message = remainder.ifBlank { line },
            severity = inferSeverity(line),
            explicitSeverity = false
        )
    }

    val levelMatch = explicitSeverityPattern.find(line)
    if (levelMatch != null) {
        val level = levelMatch.groupValues[1]
        val message = levelMatch.groupValues[2].ifBlank { line }
        return ParsedLogFields(
            timestamp = null,
            message = message,
            severity = toSeverity(level),
            explicitSeverity = true
        )
    }

    return ParsedLogFields(
        timestamp = null,
        message = line,
        severity = inferSeverity(line),
        explicitSeverity = false
    )
}

private fun inferSeverity(line: String): LogSeverity {
    val normalized = line.uppercase(Locale.US)
    return when {
        "[ERROR]" in normalized || " ERROR " in normalized || "EXCEPTION" in normalized || "FAILED" in normalized -> LogSeverity.ERROR
        "[WARN]" in normalized || "WARNING" in normalized -> LogSeverity.WARN
        "[INFO]" in normalized || " CONNECTED" in normalized -> LogSeverity.INFO
        "[DEBUG]" in normalized || "DEBUG" in normalized -> LogSeverity.DEBUG
        "[TRACE]" in normalized || "TRACE" in normalized || "VERBOSE" in normalized -> LogSeverity.VERBOSE
        else -> LogSeverity.PLAIN
    }
}

private fun toSeverity(token: String): LogSeverity {
    return when (token.uppercase(Locale.US)) {
        "ERROR", "ERR" -> LogSeverity.ERROR
        "WARN", "WARNING" -> LogSeverity.WARN
        "INFO" -> LogSeverity.INFO
        "DEBUG" -> LogSeverity.DEBUG
        "TRACE", "VERBOSE" -> LogSeverity.VERBOSE
        else -> LogSeverity.PLAIN
    }
}

private fun prioritizeSeverity(current: LogSeverity, incoming: LogSeverity): LogSeverity {
    fun rank(severity: LogSeverity): Int {
        return when (severity) {
            LogSeverity.ERROR -> 5
            LogSeverity.WARN -> 4
            LogSeverity.INFO -> 3
            LogSeverity.DEBUG -> 2
            LogSeverity.VERBOSE -> 1
            LogSeverity.PLAIN -> 0
        }
    }
    return if (rank(incoming) > rank(current)) incoming else current
}
