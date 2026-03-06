package fr.lachemoilagrappe.ui.screens.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.lachemoilagrappe.domain.model.CallAction
import fr.lachemoilagrappe.domain.model.SmsDecision
import fr.lachemoilagrappe.ui.theme.Error
import fr.lachemoilagrappe.ui.theme.Success
import fr.lachemoilagrappe.ui.theme.Warning
import fr.lachemoilagrappe.ui.theme.Info

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onNavigateBack: () -> Unit,
    viewModel: DebugViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var phoneNumber by remember { mutableStateOf("+33612345678") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test du filtrage") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Simuler un appel entrant",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Entrez un numéro de téléphone pour tester la décision de filtrage sans recevoir de vrai appel.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Numéro de téléphone") },
                placeholder = { Text("+33612345678") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.testCall(phoneNumber) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tester")
                }

                Button(
                    onClick = { viewModel.simulateFullCall(phoneNumber) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Simuler complet")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Résultat appel
            uiState.lastResult?.let { result ->
                ResultCard(result = result)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === SECTION TEST SMS ===
            Text(
                text = "Test envoi SMS",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Testez si un SMS serait envoyé à ce numéro selon les paramètres actuels.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.testSms(phoneNumber) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tester SMS")
                }

                Button(
                    onClick = { viewModel.sendTestSms(phoneNumber) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Envoyer SMS")
                }
            }

            // Résultat SMS
            uiState.lastSmsResult?.let { result ->
                SmsResultCard(result = result)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logs
            if (uiState.logs.isNotEmpty()) {
                Text(
                    text = "Logs",
                    style = MaterialTheme.typography.titleMedium
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.logs.forEach { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Numéros de test rapides
            Text(
                text = "Tests rapides",
                style = MaterialTheme.typography.titleMedium
            )

            QuickTestButton(
                label = "Numéro inconnu",
                number = "+33698765432",
                onClick = { viewModel.simulateFullCall(it) }
            )

            QuickTestButton(
                label = "Numéro masqué",
                number = "",
                onClick = { viewModel.simulateFullCall(it) }
            )

            QuickTestButton(
                label = "Numéro court (urgence)",
                number = "15",
                onClick = { viewModel.simulateFullCall(it) }
            )

            QuickTestButton(
                label = "Démarcheur (0162)",
                number = "0162123456",
                onClick = { viewModel.simulateFullCall(it) }
            )

            QuickTestButton(
                label = "Démarcheur DOM (09475)",
                number = "0947512345",
                onClick = { viewModel.simulateFullCall(it) }
            )

            QuickTestButton(
                label = "Démarcheur (0163)",
                number = "0163876543",
                onClick = { viewModel.simulateFullCall(it) }
            )

            QuickTestButton(
                label = "Démarcheur (0270)",
                number = "0270654321",
                onClick = { viewModel.simulateFullCall(it) }
            )

            QuickTestButton(
                label = "Numéro inconnu 2",
                number = "+33745123987",
                onClick = { viewModel.simulateFullCall(it) }
            )

            Button(
                onClick = { viewModel.clearLogs() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Effacer les logs")
            }
        }
    }
}

@Composable
private fun ResultCard(result: TestResult) {
    val (color, icon) = when (result.action) {
        is CallAction.Allow -> Success to "✓"
        is CallAction.Reject -> Warning to "✗"
        is CallAction.RejectAsTelemarketer -> Error to "📞"
        is CallAction.RejectAsHidden -> Error to "❓"
        is CallAction.Block -> Error to "⛔"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineMedium
                )
                Column {
                    Text(
                        text = result.actionName,
                        style = MaterialTheme.typography.titleLarge,
                        color = color
                    )
                    Text(
                        text = result.reason,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Numéro: ${result.phoneNumber}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Normalisé: ${result.normalizedNumber}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Dans contacts: ${if (result.isInContacts) "Oui" else "Non"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Mobile: ${if (result.isMobile) "Oui" else "Non"}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun QuickTestButton(
    label: String,
    number: String,
    onClick: (String) -> Unit
) {
    Button(
        onClick = { onClick(number) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("$label ($number)")
    }
}

@Composable
private fun SmsResultCard(result: SmsTestResult) {
    val (color, icon) = when {
        result.smsSent -> Success to "✓"
        result.error != null -> Error to "✗"
        result.decision is SmsDecision.Send -> Success to "→"
        result.decision is SmsDecision.AskConfirmation -> Info to "?"
        else -> Warning to "⊘"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineMedium
                )
                Column {
                    Text(
                        text = if (result.smsSent) "SMS ENVOYÉ" else "Test SMS",
                        style = MaterialTheme.typography.titleLarge,
                        color = color
                    )
                    Text(
                        text = result.decisionText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Numéro: ${result.phoneNumber}",
                style = MaterialTheme.typography.bodySmall
            )
            if (result.smsId != null) {
                Text(
                    text = "SMS ID: ${result.smsId}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (result.error != null) {
                Text(
                    text = "Erreur: ${result.error}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Error
                )
            }
        }
    }
}

data class TestResult(
    val phoneNumber: String,
    val normalizedNumber: String,
    val action: CallAction,
    val actionName: String,
    val reason: String,
    val isInContacts: Boolean,
    val isMobile: Boolean
)
