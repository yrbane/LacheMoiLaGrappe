package com.callfilter.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.callfilter.R
import com.callfilter.data.local.db.entity.CallLogEntry
import com.callfilter.domain.model.CallDecision
import com.callfilter.ui.theme.Error
import com.callfilter.ui.theme.Success
import com.callfilter.ui.theme.Warning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val callHistory by viewModel.callHistory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (callHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_calls_yet),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(callHistory, key = { it.id }) { entry ->
                    CallLogItem(
                        entry = entry,
                        onAllow = { viewModel.allowNumber(entry.phoneNumber) },
                        onBlock = { viewModel.blockNumber(entry.phoneNumber) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CallLogItem(
    entry: CallLogEntry,
    onAllow: () -> Unit,
    onBlock: () -> Unit
) {
    val statusColor = when (entry.decision) {
        CallDecision.ALLOWED -> Success
        CallDecision.REJECTED -> Warning
        CallDecision.REJECTED_SPAM -> Error
        CallDecision.BLOCKED -> Error
    }

    val statusText = when (entry.decision) {
        CallDecision.ALLOWED -> stringResource(R.string.call_allowed)
        CallDecision.REJECTED -> stringResource(R.string.call_rejected)
        CallDecision.REJECTED_SPAM -> stringResource(R.string.call_spam)
        CallDecision.BLOCKED -> "Bloqué"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.contactName ?: entry.phoneNumber,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (entry.contactName != null) {
                        Text(
                            text = entry.phoneNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(entry.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (entry.spamTag != null) {
                    Text(
                        text = "${entry.spamTag} (${entry.spamScore})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Error
                    )
                }

                Row {
                    IconButton(onClick = onAllow) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(R.string.allow),
                            tint = Success
                        )
                    }
                    IconButton(onClick = onBlock) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = stringResource(R.string.block),
                            tint = Error
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
    return formatter.format(Date(timestamp))
}
