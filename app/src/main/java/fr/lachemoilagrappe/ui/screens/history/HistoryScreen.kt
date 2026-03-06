package fr.lachemoilagrappe.ui.screens.history

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.lachemoilagrappe.R
import fr.lachemoilagrappe.data.local.db.entity.CallLogEntry
import fr.lachemoilagrappe.data.local.db.entity.PhishingSmsEntry
import fr.lachemoilagrappe.domain.model.CallDecision
import fr.lachemoilagrappe.ui.theme.Error
import fr.lachemoilagrappe.ui.theme.Success
import fr.lachemoilagrappe.ui.theme.Warning
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val callHistory by viewModel.callHistory.collectAsState()
    val smsHistory by viewModel.phishingSmsHistory.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var isSearchVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (isSearchVisible) {
                            TextField(
                                value = searchQuery,
                                onValueChange = viewModel::onSearchQueryChanged,
                                placeholder = { Text("Rechercher...") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(stringResource(R.string.history))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isSearchVisible) {
                                isSearchVisible = false
                                viewModel.onSearchQueryChanged("")
                            } else {
                                onNavigateBack()
                            }
                        }) {
                            Icon(
                                if (isSearchVisible) Icons.Default.Close
                                else Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour"
                            )
                        }
                    },
                    actions = {
                        if (!isSearchVisible) {
                            IconButton(onClick = { isSearchVisible = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Rechercher")
                            }
                            IconButton(onClick = {
                                val uri = viewModel.exportCsv(context)
                                if (uri != null) {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Exporter l'historique"))
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Aucun historique à exporter")
                                    }
                                }
                            }) {
                                Icon(Icons.Default.FileDownload, contentDescription = "Exporter CSV")
                            }
                        }
                    }
                )

                // Tabs
                androidx.compose.material3.SecondaryTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {}
                ) {
                    androidx.compose.material3.Tab(
                        selected = selectedTab == HistoryTab.CALLS,
                        onClick = { viewModel.selectTab(HistoryTab.CALLS) },
                        text = { Text("Appels") }
                    )
                    androidx.compose.material3.Tab(
                        selected = selectedTab == HistoryTab.SMS,
                        onClick = { viewModel.selectTab(HistoryTab.SMS) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Phishing")
                                val unreadCount = smsHistory.count { !it.isRead }
                                if (unreadCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    androidx.compose.material3.Badge { Text(unreadCount.toString()) }
                                }
                            }
                        }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                HistoryTab.CALLS -> {
                    if (callHistory.isEmpty()) {
                        EmptyHistoryState(
                            modifier = Modifier.fillMaxSize(),
                            isSearching = searchQuery.isNotBlank(),
                            icon = Icons.Default.Shield,
                            text = if (searchQuery.isNotBlank()) "Aucun résultat" else stringResource(R.string.no_calls_yet)
                        )
                    } else {
                        val grouped = callHistory.groupBy { getDateGroup(it.timestamp) }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            grouped.forEach { (dateLabel, entries) ->
                                item(key = "header_$dateLabel") {
                                    Text(
                                        text = dateLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                    )
                                }

                                items(entries, key = { it.id }) { entry ->
                                    SwipeableCallLogItem(
                                        entry = entry,
                                        onAllow = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.allowNumber(entry.phoneNumber)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("${entry.phoneNumber} autorisé")
                                            }
                                        },
                                        onBlock = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.blockNumber(entry.phoneNumber)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("${entry.phoneNumber} bloqué")
                                            }
                                        }
                                    )
                                }
                            }

                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
                HistoryTab.SMS -> {
                    if (smsHistory.isEmpty()) {
                        EmptyHistoryState(
                            modifier = Modifier.fillMaxSize(),
                            isSearching = false,
                            icon = Icons.Default.Block,
                            text = "Aucun SMS suspect détecté"
                        )
                    } else {
                        val grouped = smsHistory.groupBy { getDateGroup(it.timestamp) }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            grouped.forEach { (dateLabel, entries) ->
                                item(key = "sms_header_$dateLabel") {
                                    Text(
                                        text = dateLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                    )
                                }

                                items(entries, key = { it.id }) { entry ->
                                    PhishingSmsItem(
                                        entry = entry,
                                        onDelete = { viewModel.deletePhishingSms(entry.id) },
                                        onMarkAsRead = { viewModel.markPhishingAsRead(entry.id) }
                                    )
                                }
                            }

                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhishingSmsItem(
    entry: PhishingSmsEntry,
    onDelete: () -> Unit,
    onMarkAsRead: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMarkAsRead() },
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isRead) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.phoneNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (entry.isRead) FontWeight.Medium else FontWeight.Bold
                )
                Text(
                    text = formatTimestamp(entry.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (entry.matchedKeyword != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        tint = Error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mot-clé détecté : ${entry.matchedKeyword}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableCallLogItem(
    entry: CallLogEntry,
    onAllow: () -> Unit,
    onBlock: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onAllow()
                    false // Don't dismiss, just trigger action
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onBlock()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Success.copy(alpha = 0.3f)
                    SwipeToDismissBoxValue.EndToStart -> Error.copy(alpha = 0.3f)
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                label = "swipe_color"
            )
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Block
                SwipeToDismissBoxValue.Settled -> null
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                icon?.let {
                    Icon(
                        it,
                        contentDescription = null,
                        tint = if (direction == SwipeToDismissBoxValue.StartToEnd) Success else Error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true
    ) {
        CallLogItem(entry = entry)
    }
}

@Composable
private fun EmptyHistoryState(
    modifier: Modifier = Modifier,
    isSearching: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Shield,
    text: String = stringResource(R.string.no_calls_yet)
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isSearching) "Essayez un autre terme de recherche"
                else "Les événements filtrés apparaîtront ici",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun CallLogItem(entry: CallLogEntry) {
    val (statusColor, statusIcon) = when (entry.decision) {
        CallDecision.ALLOWED -> Success to Icons.Default.Check
        CallDecision.REJECTED -> Warning to Icons.Default.PhoneDisabled
        CallDecision.REJECTED_TELEMARKETER -> Error to Icons.Default.Block
        CallDecision.REJECTED_HIDDEN -> Error to Icons.Default.Block
        CallDecision.BLOCKED -> Error to Icons.Default.Block
    }

    val statusText = when (entry.decision) {
        CallDecision.ALLOWED -> stringResource(R.string.call_allowed)
        CallDecision.REJECTED -> stringResource(R.string.call_rejected)
        CallDecision.REJECTED_TELEMARKETER -> "Démarcheur"
        CallDecision.REJECTED_HIDDEN -> "Masqué"
        CallDecision.BLOCKED -> "Bloqué"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(20.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = entry.contactName ?: entry.phoneNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                    Text(
                        text = formatTimestamp(entry.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getDateGroup(timestamp: Long): String {
    val cal = Calendar.getInstance()
    val today = Calendar.getInstance()

    cal.timeInMillis = timestamp

    return when {
        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Aujourd'hui"
        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1 -> "Hier"
        else -> SimpleDateFormat("EEEE d MMMM", Locale.FRANCE).format(Date(timestamp))
            .replaceFirstChar { it.uppercase() }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.FRANCE)
    return formatter.format(Date(timestamp))
}
