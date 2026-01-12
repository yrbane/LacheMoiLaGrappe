package com.callfilter.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.callfilter.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
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
            SettingsSection(title = "Filtrage")

            SettingsItem(
                title = "Paramètres de filtrage",
                subtitle = "Configurer le comportement du filtrage",
                onClick = { /* TODO */ }
            )

            SettingsItem(
                title = "Listes personnalisées",
                subtitle = "Gérer l'allowlist et la blocklist",
                onClick = { /* TODO */ }
            )

            HorizontalDivider()

            SettingsSection(title = "Base de données spam")

            SettingsItem(
                title = "Source de données",
                subtitle = "Configurer la source de la base spam",
                onClick = { /* TODO */ }
            )

            SettingsItem(
                title = "Synchronisation",
                subtitle = "Dernière mise à jour : Jamais",
                onClick = { /* TODO */ }
            )

            HorizontalDivider()

            SettingsSection(title = "SMS automatique")

            SettingsItem(
                title = "Template SMS",
                subtitle = "Personnaliser le message envoyé",
                onClick = { /* TODO */ }
            )

            SettingsItem(
                title = "Paramètres SMS",
                subtitle = "Cooldown, confirmation, exclusions",
                onClick = { /* TODO */ }
            )

            HorizontalDivider()

            SettingsSection(title = "À propos")

            SettingsItem(
                title = "Version",
                subtitle = "0.1.0",
                onClick = { }
            )

            SettingsItem(
                title = "Politique de confidentialité",
                subtitle = "Données stockées localement uniquement",
                onClick = { /* TODO */ }
            )
        }
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
