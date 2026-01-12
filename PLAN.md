# Plan d'Implémentation - CallFilter

## Vue d'ensemble

Ce document détaille le plan d'implémentation de l'application CallFilter, une application Android de filtrage d'appels intelligente.

---

## Phase 1 : Fondations du projet

### 1.1 Configuration du projet Android

- [ ] Créer le projet Android Studio avec Kotlin
- [ ] Configurer Gradle (Kotlin DSL)
- [ ] Définir minSdk 24, targetSdk 34
- [ ] Ajouter les dépendances Jetpack (Room, Hilt, WorkManager, Compose)
- [ ] Configurer libphonenumber

### 1.2 Structure des packages

```
com.callfilter/
├── di/                     # Injection de dépendances (Hilt)
├── domain/
│   ├── model/              # Entités métier
│   ├── repository/         # Interfaces repositories
│   └── usecase/            # Use-cases
├── data/
│   ├── local/
│   │   ├── db/             # Room (entities, DAOs)
│   │   └── preferences/    # DataStore
│   ├── remote/             # API spam (optionnel)
│   └── repository/         # Implémentations
├── service/
│   ├── CallScreeningServiceImpl.kt
│   └── SmsService.kt
├── worker/                 # WorkManager jobs
├── ui/
│   ├── theme/
│   ├── components/
│   ├── screens/
│   │   ├── home/
│   │   ├── history/
│   │   ├── settings/
│   │   ├── spam/
│   │   └── sms/
│   └── navigation/
└── util/                   # Helpers (phone parsing, etc.)
```

### 1.3 Manifest et permissions

```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<service
    android:name=".service.CallScreeningServiceImpl"
    android:permission="android.permission.BIND_SCREENING_SERVICE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.telecom.CallScreeningService" />
    </intent-filter>
</service>
```

---

## Phase 2 : Couche Data

### 2.1 Base de données Room

#### Entités

```kotlin
// CallLogEntry - Historique des appels filtrés
@Entity(tableName = "call_log")
data class CallLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val normalizedNumber: String,
    val timestamp: Long,
    val decision: CallDecision, // ALLOWED, REJECTED, BLOCKED
    val reason: String, // "contact", "allowlist", "spam", "unknown"
    val spamTag: String? = null,
    val spamScore: Int? = null
)

// SpamEntry - Base de données spam
@Entity(tableName = "spam_db")
data class SpamEntry(
    @PrimaryKey val normalizedNumber: String,
    val tag: String, // "démarchage", "arnaque", etc.
    val score: Int, // 0-100
    val source: String,
    val lastSeen: Long,
    val updatedAt: Long
)

// UserListEntry - Allowlist/Blocklist utilisateur
@Entity(tableName = "user_list")
data class UserListEntry(
    @PrimaryKey val normalizedNumber: String,
    val listType: ListType, // ALLOW, BLOCK
    val label: String? = null,
    val addedAt: Long
)

// SmsLogEntry - Historique des SMS envoyés
@Entity(tableName = "sms_log")
data class SmsLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val timestamp: Long,
    val status: SmsStatus, // SENT, FAILED, PENDING
    val templateUsed: String
)
```

#### DAOs

- `CallLogDao` : CRUD historique appels
- `SpamDao` : Lookup + bulk insert/update
- `UserListDao` : Gestion allow/block
- `SmsLogDao` : Rate limiting + historique

### 2.2 Repositories

```kotlin
interface ContactsRepository {
    suspend fun isNumberInContacts(number: String): Boolean
    suspend fun getContactName(number: String): String?
}

interface SpamRepository {
    suspend fun lookupNumber(number: String): SpamEntry?
    suspend fun updateDatabase(entries: List<SpamEntry>)
    suspend fun getLastUpdateTime(): Long
    suspend fun getStats(): SpamDbStats
}

interface UserListRepository {
    suspend fun isAllowed(number: String): Boolean
    suspend fun isBlocked(number: String): Boolean
    suspend fun addToAllowlist(number: String, label: String?)
    suspend fun addToBlocklist(number: String, label: String?)
    suspend fun remove(number: String)
}

interface CallLogRepository {
    suspend fun logCall(entry: CallLogEntry)
    fun getCallHistory(): Flow<List<CallLogEntry>>
    suspend fun getCallsByNumber(number: String): List<CallLogEntry>
}

interface SmsRepository {
    suspend fun canSendSms(number: String): Boolean // Rate limit check
    suspend fun logSmsSent(number: String, status: SmsStatus)
    suspend fun getLastSmsSentTo(number: String): Long?
}

interface SettingsRepository {
    val filterUnknown: Flow<Boolean>
    val rejectUnknown: Flow<Boolean>
    val useSpamDb: Flow<Boolean>
    val autoSmsEnabled: Flow<Boolean>
    val smsConfirmationMode: Flow<Boolean>
    val smsCooldownHours: Flow<Int>
    val smsTemplate: Flow<String>
    // ... setters
}
```

---

## Phase 3 : Couche Domain

### 3.1 Use Cases

```kotlin
class DecideCallActionUseCase(
    private val contactsRepo: ContactsRepository,
    private val spamRepo: SpamRepository,
    private val userListRepo: UserListRepository,
    private val settingsRepo: SettingsRepository
) {
    suspend operator fun invoke(phoneNumber: String): CallAction
}

class LookupSpamUseCase(
    private val spamRepo: SpamRepository
) {
    suspend operator fun invoke(number: String): SpamInfo?
}

class ShouldSendSmsUseCase(
    private val smsRepo: SmsRepository,
    private val settingsRepo: SettingsRepository,
    private val phoneUtil: PhoneNumberUtil
) {
    suspend operator fun invoke(number: String): SmsDecision
}

class SendIdentitySmsUseCase(
    private val smsRepo: SmsRepository,
    private val settingsRepo: SettingsRepository,
    private val smsSender: SmsSender
) {
    suspend operator fun invoke(number: String): Result<Unit>
}

class LogCallEventUseCase(
    private val callLogRepo: CallLogRepository
) {
    suspend operator fun invoke(event: CallEvent)
}
```

### 3.2 Modèles Domain

```kotlin
sealed class CallAction {
    object Allow : CallAction()
    object Reject : CallAction()
    data class RejectAsSpam(val tag: String, val score: Int) : CallAction()
}

data class CallEvent(
    val number: String,
    val action: CallAction,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class SmsDecision {
    object Send : SmsDecision()
    object AskConfirmation : SmsDecision()
    data class Skip(val reason: String) : SmsDecision()
}
```

---

## Phase 4 : Service de filtrage

### 4.1 CallScreeningService

```kotlin
class CallScreeningServiceImpl : CallScreeningService() {

    @Inject lateinit var decideCallAction: DecideCallActionUseCase
    @Inject lateinit var logCallEvent: LogCallEventUseCase
    @Inject lateinit var shouldSendSms: ShouldSendSmsUseCase
    @Inject lateinit var sendIdentitySms: SendIdentitySmsUseCase
    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val action = decideCallAction(number)
            val response = buildResponse(action)

            respondToCall(callDetails, response)
            logCallEvent(CallEvent(number, action, getReasonString(action)))

            if (action is CallAction.Reject || action is CallAction.RejectAsSpam) {
                handlePostReject(number, action)
            }
        }
    }

    private fun buildResponse(action: CallAction): CallResponse {
        return CallResponse.Builder().apply {
            when (action) {
                is CallAction.Allow -> setDisallowCall(false)
                is CallAction.Reject,
                is CallAction.RejectAsSpam -> {
                    setDisallowCall(true)
                    setRejectCall(true)
                    setSkipNotification(false)
                }
            }
        }.build()
    }

    private suspend fun handlePostReject(number: String, action: CallAction) {
        // Notification
        notificationHelper.showRejectedCallNotification(number, action)

        // SMS si applicable
        when (val decision = shouldSendSms(number)) {
            is SmsDecision.Send -> sendIdentitySms(number)
            is SmsDecision.AskConfirmation -> {
                notificationHelper.showSmsConfirmationNotification(number)
            }
            is SmsDecision.Skip -> { /* Log reason */ }
        }
    }
}
```

---

## Phase 5 : WorkManager - Synchronisation spam DB

### 5.1 SpamDbSyncWorker

```kotlin
class SpamDbSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @Inject lateinit var spamRepo: SpamRepository
    @Inject lateinit var settingsRepo: SettingsRepository

    override suspend fun doWork(): Result {
        // 1. Récupérer l'URL de la source
        // 2. Télécharger le JSON
        // 3. Vérifier signature/checksum
        // 4. Parser et mettre à jour Room
        // 5. Notifier le succès
        return Result.success()
    }
}

// Scheduling
object SpamDbSyncScheduler {
    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SpamDbSyncWorker>(
            24, TimeUnit.HOURS
        ).setConstraints(constraints).build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "spam_db_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
}
```

---

## Phase 6 : Interface utilisateur (Compose)

### 6.1 Écrans principaux

1. **HomeScreen**
   - Toggles principaux (filtrage, spam, SMS)
   - Stats rapides (appels rejetés aujourd'hui)
   - Accès rapide à l'historique

2. **HistoryScreen**
   - Liste des appels filtrés (LazyColumn)
   - Filtres (tous, rejetés, spam)
   - Actions par item (allowlist, blocklist, ajouter contact)

3. **SettingsScreen**
   - Paramètres de filtrage
   - Configuration SMS
   - Gestion des permissions
   - À propos / Aide

4. **SpamDbScreen**
   - Source actuelle
   - Dernière mise à jour
   - Statistiques
   - Actions (sync manuel, export, vider)

5. **SmsTemplateScreen**
   - Éditeur de template
   - Variables disponibles
   - Aperçu
   - Historique des SMS envoyés

### 6.2 Navigation

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object History : Screen("history")
    object Settings : Screen("settings")
    object SpamDb : Screen("spam_db")
    object SmsTemplate : Screen("sms_template")
}
```

---

## Phase 7 : Sécurité et conformité

### 7.1 Checklist sécurité

- [ ] Données chiffrées au repos (EncryptedSharedPreferences pour settings sensibles)
- [ ] Pas de logs de numéros en production
- [ ] Validation des inputs (numéros de téléphone)
- [ ] HTTPS + certificate pinning (optionnel) pour sync spam DB
- [ ] Pas de permissions superflues

### 7.2 Conformité Google Play

- [ ] Déclaration d'usage CallScreeningService
- [ ] Justification SEND_SMS (SMS envoyés uniquement sur opt-in)
- [ ] Privacy Policy obligatoire
- [ ] Pas d'usage abusif de l'accessibilité
- [ ] Explications claires des permissions à l'utilisateur

### 7.3 Limitations à documenter

| Limitation | Explication | Mitigation |
|------------|-------------|------------|
| Numéros masqués | Impossible de filtrer | Informer l'utilisateur |
| Détection mobile | Heuristique imparfaite | Option désactivable |
| VoIP | Comportement variable | Documenter |
| Double SIM | Dépend du système | Tester sur plusieurs devices |

---

## Phase 8 : Tests

### 8.1 Tests unitaires

```kotlin
// Use cases
class DecideCallActionUseCaseTest {
    @Test fun `returns Allow when number is in contacts`()
    @Test fun `returns Reject when number is unknown and setting enabled`()
    @Test fun `returns RejectAsSpam when number is in spam database`()
    @Test fun `allowlist takes priority over spam database`()
    @Test fun `blocklist takes priority over contacts`()
}

class ShouldSendSmsUseCaseTest {
    @Test fun `returns Skip when SMS disabled`()
    @Test fun `returns Skip when cooldown not elapsed`()
    @Test fun `returns Skip for short numbers`()
    @Test fun `returns Send for valid mobile number`()
    @Test fun `returns AskConfirmation when confirmation mode enabled`()
}
```

### 8.2 Tests d'intégration

- Room DAOs avec base in-memory
- Repositories avec faux contacts
- WorkManager avec TestDriver

### 8.3 Tests UI

- Compose UI tests avec ComposeTestRule
- Navigation tests
- Screenshot tests (optionnel)

---

## Ressources

- [CallScreeningService Documentation](https://developer.android.com/reference/android/telecom/CallScreeningService)
- [libphonenumber](https://github.com/google/libphonenumber)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
