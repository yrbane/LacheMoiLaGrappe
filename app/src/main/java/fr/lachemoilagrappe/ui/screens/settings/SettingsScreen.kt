package fr.lachemoilagrappe.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.lachemoilagrappe.BuildConfig
import fr.lachemoilagrappe.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSmsTemplateDialog by remember { mutableStateOf(false) }
    var showCooldownDialog by remember { mutableStateOf(false) }
    var showPrefixesDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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
                .verticalScroll(rememberScrollState())
        ) {
            // === FILTRAGE ===
            SettingsSection(title = "Filtrage")

            SwitchSettingsItem(
                title = "Filtrer les appels inconnus",
                subtitle = "Rejeter les numéros non enregistrés",
                checked = uiState.filterUnknownEnabled,
                onCheckedChange = viewModel::setFilterUnknownEnabled
            )

            SwitchSettingsItem(
                title = "Bloquer les démarcheurs",
                subtitle = "Préfixes ARCEP réservés (0162, 0163, etc.)",
                checked = uiState.blockTelemarketersEnabled,
                onCheckedChange = viewModel::setBlockTelemarketersEnabled
            )

            val totalPrefixes = uiState.arcepPrefixes.size + uiState.customTelemarketerPrefixes.size
            val customCount = uiState.customTelemarketerPrefixes.size
            SettingsItem(
                title = "Préfixes démarcheurs",
                subtitle = "$totalPrefixes préfixes (${uiState.arcepPrefixes.size} ARCEP" +
                    if (customCount > 0) " + $customCount personnalisés)" else ")",
                onClick = { showPrefixesDialog = true }
            )

            SwitchSettingsItem(
                title = "Bloquer les numéros masqués",
                subtitle = "Rejeter les appels sans numéro visible",
                checked = uiState.blockHiddenNumbersEnabled,
                onCheckedChange = viewModel::setBlockHiddenNumbersEnabled
            )

            HorizontalDivider()

            // === SMS ===
            SettingsSection(title = "SMS & Sécurité")
            
            SwitchSettingsItem(
                title = "Détection de Phishing",
                subtitle = "Analyser les SMS entrants pour bloquer les arnaques (CPF, Colis...)",
                checked = uiState.phishingProtectionEnabled,
                onCheckedChange = viewModel::setPhishingProtectionEnabled
            )

            SwitchSettingsItem(
                title = "Activer les SMS auto",
                subtitle = "Envoyer un SMS aux appelants inconnus",
                checked = uiState.autoSmsEnabled,
                onCheckedChange = viewModel::setAutoSmsEnabled
            )

            SwitchSettingsItem(
                title = "Mode confirmation",
                subtitle = "Demander avant chaque envoi",
                checked = uiState.smsConfirmationMode,
                onCheckedChange = viewModel::setSmsConfirmationMode,
                enabled = uiState.autoSmsEnabled
            )

            SettingsItem(
                title = "Template SMS",
                subtitle = uiState.smsTemplate.take(50) + if (uiState.smsTemplate.length > 50) "..." else "",
                onClick = { showSmsTemplateDialog = true }
            )

            SettingsItem(
                title = "Délai entre SMS",
                subtitle = "${uiState.smsCooldownHours} heures",
                onClick = { showCooldownDialog = true }
            )

            HorizontalDivider()

            // === SÉRÉNITÉ ===
            SettingsSection(title = "Sérénité")

            SwitchSettingsItem(
                title = "Mode Sommeil",
                subtitle = "Laisser passer tous les appels la nuit",
                checked = uiState.sleepModeEnabled,
                onCheckedChange = viewModel::setSleepModeEnabled
            )

            if (uiState.sleepModeEnabled) {
                SettingsItem(
                    title = "Plage horaire",
                    subtitle = "De ${uiState.sleepModeStartTime} à ${uiState.sleepModeEndTime}",
                    onClick = { /* Could show a TimePicker here later */ }
                )
            }

            HorizontalDivider()

            // === À PROPOS ===
            SettingsSection(title = "À propos")

            SettingsItem(
                title = "Version",
                subtitle = BuildConfig.VERSION_NAME,
                onClick = { }
            )

            SettingsItem(
                title = "Politique de confidentialité",
                subtitle = "Données stockées localement uniquement",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://yrbane.github.io/LacheMoiLaGrappe/privacy"))
                    context.startActivity(intent)
                }
            )

            HorizontalDivider()

            // === SOUTENIR ===
            SettingsSection(title = "Soutenir le projet")

            SettingsItem(
                title = "Faire un don",
                subtitle = "Gratuit, open-source, sans pub. Un petit coup de pouce ?",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/sphilippe1209"))
                    context.startActivity(intent)
                }
            )

            SettingsItem(
                title = "Code source",
                subtitle = "github.com/yrbane/LacheMoiLaGrappe",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yrbane/LacheMoiLaGrappe"))
                    context.startActivity(intent)
                }
            )
        }
    }

    // Dialog Template SMS
    if (showSmsTemplateDialog) {
        SmsTemplateDialog(
            currentTemplate = uiState.smsTemplate,
            onDismiss = { showSmsTemplateDialog = false },
            onSave = { newTemplate ->
                viewModel.setSmsTemplate(newTemplate)
                showSmsTemplateDialog = false
            }
        )
    }

    // Dialog Cooldown
    if (showCooldownDialog) {
        CooldownDialog(
            currentHours = uiState.smsCooldownHours,
            onDismiss = { showCooldownDialog = false },
            onSave = { hours ->
                viewModel.setSmsCooldownHours(hours)
                showCooldownDialog = false
            }
        )
    }

    // Dialog Préfixes démarcheurs
    if (showPrefixesDialog) {
        TelemarketerPrefixesDialog(
            arcepPrefixes = uiState.arcepPrefixes,
            customPrefixes = uiState.customTelemarketerPrefixes,
            onAddPrefix = viewModel::addCustomPrefix,
            onRemovePrefix = viewModel::removeCustomPrefix,
            validatePrefix = viewModel::validatePrefix,
            onDismiss = { showPrefixesDialog = false }
        )
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}

@Composable
private fun SwitchSettingsItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    ListItem(
        headlineContent = {
            Text(
                title,
                color = if (enabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        },
        supportingContent = {
            Text(
                subtitle,
                color = if (enabled)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SmsTemplateDialog(
    currentTemplate: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var template by remember { mutableStateOf(currentTemplate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Template SMS") },
        text = {
            Column {
                Text(
                    "Message envoyé aux appelants inconnus :",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = template,
                    onValueChange = { template = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${template.length} caractères",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(template) }) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun CooldownDialog(
    currentHours: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var hours by remember { mutableFloatStateOf(currentHours.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Délai entre SMS") },
        text = {
            Column {
                Text(
                    "Temps minimum entre deux SMS vers le même numéro :",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${hours.toInt()} heures",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = hours,
                    onValueChange = { hours = it },
                    valueRange = 1f..72f,
                    steps = 70
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1h", style = MaterialTheme.typography.bodySmall)
                    Text("72h", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(hours.toInt()) }) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TelemarketerPrefixesDialog(
    arcepPrefixes: Set<String>,
    customPrefixes: Set<String>,
    onAddPrefix: (String) -> Unit,
    onRemovePrefix: (String) -> Unit,
    validatePrefix: (String) -> String?,
    onDismiss: () -> Unit
) {
    var newPrefix by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Préfixes démarcheurs") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Section ARCEP
                Text(
                    "PRÉFIXES ARCEP (${arcepPrefixes.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    arcepPrefixes.sorted().forEach { prefix ->
                        ArcepPrefixChip(prefix = prefix)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section Custom
                Text(
                    "MES PRÉFIXES (${customPrefixes.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (customPrefixes.isEmpty()) {
                    Text(
                        "Aucun préfixe personnalisé",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        customPrefixes.sorted().forEach { prefix ->
                            CustomPrefixChip(
                                prefix = prefix,
                                onRemove = { onRemovePrefix(prefix) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add new prefix
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = newPrefix,
                        onValueChange = {
                            newPrefix = it.filter { c -> c.isDigit() }.take(5)
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Nouveau préfixe") },
                        placeholder = { Text("0199") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = errorMessage != null,
                        supportingText = if (errorMessage != null) {
                            { Text(errorMessage!!) }
                        } else {
                            { Text("4-5 chiffres") }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val error = validatePrefix(newPrefix)
                            if (error != null) {
                                errorMessage = error
                            } else {
                                onAddPrefix(newPrefix)
                                newPrefix = ""
                                errorMessage = null
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Ajouter",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@Composable
private fun ArcepPrefixChip(prefix: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = prefix,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "ARCEP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomPrefixChip(
    prefix: String,
    onRemove: () -> Unit
) {
    InputChip(
        selected = false,
        onClick = { },
        label = { Text(prefix) },
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = "Supprimer",
                modifier = Modifier
                    .size(InputChipDefaults.IconSize)
                    .clickable { onRemove() }
            )
        }
    )
}
