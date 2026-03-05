package fr.lachemoilagrappe.ui.screens.userlists

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.lachemoilagrappe.R
import fr.lachemoilagrappe.data.local.db.entity.UserListEntry
import fr.lachemoilagrappe.ui.theme.Error
import fr.lachemoilagrappe.ui.theme.Success
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListsScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserListsViewModel = hiltViewModel()
) {
    val allowlist by viewModel.allowlist.collectAsState()
    val blocklist by viewModel.blocklist.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Autoris\u00e9s", "Bloqu\u00e9s")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes listes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un num\u00e9ro")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (index == 0) Icons.Default.Check else Icons.Default.Block,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (selectedTabIndex == index) {
                                        if (index == 0) Success else Error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(title)
                            }
                        }
                    )
                }
            }

            val currentList = if (selectedTabIndex == 0) allowlist else blocklist

            if (currentList.isEmpty()) {
                EmptyListState(
                    isAllowlist = selectedTabIndex == 0,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    items(
                        items = currentList,
                        key = { it.normalizedNumber }
                    ) { entry ->
                        SwipeToDeleteItem(
                            entry = entry,
                            isAllowlist = selectedTabIndex == 0,
                            onDelete = {
                                viewModel.removeEntry(entry)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "${entry.normalizedNumber} supprim\u00e9"
                                    )
                                }
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddNumberDialog(
            isAllowlist = selectedTabIndex == 0,
            onDismiss = { showAddDialog = false },
            onConfirm = { number, label ->
                if (selectedTabIndex == 0) {
                    viewModel.addToAllowlist(number, label)
                    scope.launch {
                        snackbarHostState.showSnackbar("$number ajout\u00e9 aux autoris\u00e9s")
                    }
                } else {
                    viewModel.addToBlocklist(number, label)
                    scope.launch {
                        snackbarHostState.showSnackbar("$number ajout\u00e9 aux bloqu\u00e9s")
                    }
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun EmptyListState(
    isAllowlist: Boolean,
    modifier: Modifier = Modifier
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
                imageVector = if (isAllowlist) Icons.Default.Check else Icons.Default.Block,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = if (isAllowlist) {
                    Success.copy(alpha = 0.4f)
                } else {
                    Error.copy(alpha = 0.4f)
                }
            )
            Text(
                text = if (isAllowlist) "Aucun num\u00e9ro autoris\u00e9" else "Aucun num\u00e9ro bloqu\u00e9",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isAllowlist) {
                    "Les num\u00e9ros autoris\u00e9s ne seront jamais filtr\u00e9s"
                } else {
                    "Les num\u00e9ros bloqu\u00e9s seront toujours rejet\u00e9s"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteItem(
    entry: UserListEntry,
    isAllowlist: Boolean,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> Error
                    else -> MaterialTheme.colorScheme.surface
                },
                label = "swipe_bg_color"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        UserListEntryCard(entry = entry, isAllowlist = isAllowlist)
    }
}

@Composable
private fun UserListEntryCard(
    entry: UserListEntry,
    isAllowlist: Boolean
) {
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
                imageVector = if (isAllowlist) Icons.Default.Check else Icons.Default.Block,
                contentDescription = null,
                tint = if (isAllowlist) Success else Error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.label ?: entry.normalizedNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (entry.label != null) {
                        Text(
                            text = entry.normalizedNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Ajout\u00e9 le ${formatDate(entry.addedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddNumberDialog(
    isAllowlist: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (number: String, label: String?) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (isAllowlist) Icons.Default.PersonAdd else Icons.Default.Block,
                contentDescription = null,
                tint = if (isAllowlist) Success else Error
            )
        },
        title = {
            Text(
                text = if (isAllowlist) "Autoriser un num\u00e9ro" else "Bloquer un num\u00e9ro"
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        isError = false
                    },
                    label = { Text("Num\u00e9ro de t\u00e9l\u00e9phone") },
                    placeholder = { Text("06 12 34 56 78") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Veuillez saisir un num\u00e9ro valide") }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Nom (optionnel)") },
                    placeholder = { Text("Ex: Docteur Martin") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (phoneNumber.isNotBlank()) {
                                onConfirm(
                                    phoneNumber.trim(),
                                    label.trim().ifBlank { null }
                                )
                            } else {
                                isError = true
                            }
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (phoneNumber.isNotBlank()) {
                        onConfirm(
                            phoneNumber.trim(),
                            label.trim().ifBlank { null }
                        )
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    return formatter.format(Date(timestamp))
}
