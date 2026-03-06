package fr.lachemoilagrappe.ui.screens.onboarding

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.lachemoilagrappe.R
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 4

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var permissionsGranted by remember { mutableStateOf(false) }
    var roleHeld by remember { mutableStateOf(checkRoleHeld(context)) }

    val roleRequestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        roleHeld = checkRoleHeld(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            userScrollEnabled = false // Forced wizard flow
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> WizardPage(viewModel)
                2 -> PermissionsPage(
                    permissionsGranted = permissionsGranted,
                    onRequestPermissions = {
                        val permissions = mutableListOf(
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.RECEIVE_SMS
                        ).apply {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }.toTypedArray()
                        permissionLauncher.launch(permissions)
                    }
                )
                3 -> FinalPage(
                    roleHeld = roleHeld,
                    onActivate = {
                        requestCallScreeningRole(context, roleRequestLauncher::launch)
                    }
                )
            }
        }

        BottomBar(
            currentPage = pagerState.currentPage,
            pageCount = PAGE_COUNT,
            isLastPage = pagerState.currentPage == PAGE_COUNT - 1,
            canGoNext = when(pagerState.currentPage) {
                2 -> permissionsGranted
                3 -> roleHeld
                else -> true
            },
            onNext = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            onFinish = {
                viewModel.completeOnboarding()
                onOnboardingComplete()
            }
        )
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "LacheMoiLaGrappe",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Reprenez le contrôle de votre téléphone en 2 minutes.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WizardPage(viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Configuration du bouclier",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        WizardQuestion(
            title = "Bloquer les démarcheurs ?",
            subtitle = "Rejeter les préfixes connus de publicité.",
            icon = Icons.Default.PhoneDisabled,
            onChoice = viewModel::setFilterTelemarketers
        )
        Spacer(modifier = Modifier.height(16.dp))
        WizardQuestion(
            title = "SMS d'identification ?",
            subtitle = "Demander aux inconnus de s'identifier par SMS.",
            icon = Icons.Default.Sms,
            onChoice = viewModel::setAutoSms
        )
        Spacer(modifier = Modifier.height(16.dp))
        WizardQuestion(
            title = "Bouclier Anti-Arnaque ?",
            subtitle = "Détecter les SMS de phishing (CPF, Colis...).",
            icon = Icons.Default.Phishing,
            onChoice = viewModel::setPhishingProtection
        )
    }
}

@Composable
private fun WizardQuestion(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onChoice: (Boolean) -> Unit
) {
    var selected by remember { mutableStateOf<Boolean?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selected == true,
                    onClick = { selected = true; onChoice(true) },
                    label = { Text("Oui") },
                    leadingIcon = if (selected == true) { { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) } } else null
                )
                FilterChip(
                    selected = selected == false,
                    onClick = { selected = false; onChoice(false) },
                    label = { Text("Non") },
                    leadingIcon = if (selected == false) { { Icon(Icons.Default.Close, contentDescription = null, Modifier.size(16.dp)) } } else null
                )
            }
        }
    }
}

@Composable
private fun PermissionsPage(
    permissionsGranted: Boolean,
    onRequestPermissions: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Autorisations", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "L'application a besoin d'accéder à vos contacts pour les laisser passer, et à vos appels pour les filtrer.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermissions,
            modifier = Modifier.fillMaxWidth(),
            enabled = !permissionsGranted
        ) {
            Text(if (permissionsGranted) "Autorisations accordées ✅" else "Accorder les permissions")
        }
    }
}

@Composable
private fun FinalPage(
    roleHeld: Boolean,
    onActivate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Dernière étape !", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Activez LacheMoiLaGrappe comme service de filtrage système pour que le bouclier soit opérationnel.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onActivate,
            modifier = Modifier.fillMaxWidth(),
            enabled = !roleHeld,
            colors = ButtonDefaults.buttonColors(containerColor = if (roleHeld) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
        ) {
            Text(if (roleHeld) "Service Activé ✅" else "Activer le service")
        }
    }
}

@Composable
private fun BottomBar(
    currentPage: Int,
    pageCount: Int,
    isLastPage: Boolean,
    canGoNext: Boolean,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier.size(8.dp).clip(CircleShape)
                        .background(if (index == currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = if (isLastPage) onFinish else onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = canGoNext
        ) {
            Text(text = if (isLastPage) "C'est parti !" else "Suivant")
        }
    }
}

private fun checkRoleHeld(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    } else true
}

private fun requestCallScreeningRole(context: Context, launcher: (Intent) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            launcher(intent)
        }
    }
}
