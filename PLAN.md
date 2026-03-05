# Plan d'Implémentation - CallFilter

## Vue d'ensemble

Ce document détaille le plan d'implémentation de l'application CallFilter, une application Android de filtrage d'appels intelligente.

---

## Phase 1 : Fondations du projet [COMPLÉTÉ]

### 1.1 Configuration du projet Android

- [x] Créer le projet Android Studio avec Kotlin
- [x] Configurer Gradle (Kotlin DSL)
- [x] Définir minSdk 24, targetSdk 35
- [x] Ajouter les dépendances Jetpack (Room, Hilt, WorkManager, Compose)
- [x] Configurer libphonenumber

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
│   ├── CallFilterScreeningService.kt
│   ├── NotificationHelper.kt
│   └── SmsService.kt
├── worker/                 # WorkManager jobs
├── ui/
│   ├── theme/
│   ├── components/
│   ├── screens/
│   │   ├── home/
│   │   ├── history/
│   │   ├── settings/
│   │   ├── debug/          # Écran de test
│   │   ├── spam/
│   │   └── sms/
│   └── navigation/
└── util/                   # Helpers (phone parsing, etc.)
```

### 1.3 Manifest et permissions [COMPLÉTÉ]

```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_CALL_LOG" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<service
    android:name=".service.CallFilterScreeningService"
    android:permission="android.permission.BIND_SCREENING_SERVICE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.telecom.CallScreeningService" />
    </intent-filter>
</service>
```

---

## Phase 2 : Couche Data [COMPLÉTÉ]

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
    val decision: CallDecision, // ALLOWED, REJECTED, REJECTED_SPAM, REJECTED_TELEMARKETER, REJECTED_HIDDEN, BLOCKED
    val reason: String,
    val contactName: String? = null,
    val spamTag: String? = null,
    val spamScore: Int? = null
)

// SpamEntry - Base de données spam
@Entity(tableName = "spam_db")
data class SpamEntry(
    @PrimaryKey val normalizedNumber: String,
    val tag: String,
    val score: Int,
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
    val normalizedNumber: String,
    val timestamp: Long,
    val status: SmsStatus, // SENT, FAILED, PENDING, DELIVERED
    val template: String
)
```

#### DAOs [COMPLÉTÉ]

- [x] `CallLogDao` : CRUD historique appels
- [x] `SpamDao` : Lookup + bulk insert/update
- [x] `UserListDao` : Gestion allow/block
- [x] `SmsLogDao` : Rate limiting + historique

### 2.2 Repositories [COMPLÉTÉ]

- [x] ContactsRepository
- [x] SpamRepository
- [x] UserListRepository
- [x] CallLogRepository
- [x] SmsRepository
- [x] SettingsRepository (DataStore)

---

## Phase 3 : Couche Domain [COMPLÉTÉ]

### 3.1 Use Cases [COMPLÉTÉ]

- [x] DecideCallActionUseCase - Logique de décision avec préfixes démarcheurs
- [x] LookupSpamUseCase - Recherche dans la base spam
- [x] ShouldSendSmsUseCase - Décision d'envoi SMS
- [x] SendIdentitySmsUseCase - Envoi SMS avec status tracking
- [x] LogCallEventUseCase - Journalisation des appels

### 3.2 Modèles Domain [COMPLÉTÉ]

```kotlin
sealed class CallAction {
    data object Allow : CallAction()
    data object Reject : CallAction()
    data class RejectAsSpam(val tag: String, val score: Int) : CallAction()
    data object RejectAsTelemarketer : CallAction()  // Nouveau
    data object RejectAsHidden : CallAction()        // Nouveau
    data object Block : CallAction()
}

enum class CallDecision {
    ALLOWED,
    REJECTED,
    REJECTED_SPAM,
    REJECTED_TELEMARKETER,  // Nouveau
    REJECTED_HIDDEN,        // Nouveau
    BLOCKED
}

sealed class SmsDecision {
    data object Send : SmsDecision()
    data object AskConfirmation : SmsDecision()
    data class Skip(val reason: String) : SmsDecision()
}
```

---

## Phase 4 : Service de filtrage [COMPLÉTÉ]

### 4.1 CallFilterScreeningService [COMPLÉTÉ]

Fonctionnalités implémentées :
- [x] Interception des appels entrants
- [x] Vérification blocklist/allowlist
- [x] Détection des 17 préfixes démarcheurs ARCEP
- [x] Détection des numéros masqués (configurable)
- [x] Recherche dans base spam
- [x] Vérification contacts
- [x] Décision de filtrage
- [x] Notification d'appel rejeté
- [x] Journalisation dans Room
- [x] Gestion SMS automatique

### 4.2 Préfixes démarcheurs ARCEP [COMPLÉTÉ]

France métropolitaine :
- 0162, 0163, 0270, 0271, 0377, 0378
- 0424, 0425, 0568, 0569, 0948, 0949

DOM-TOM :
- 09475 (Guadeloupe, Saint-Martin, Saint-Barthélemy)
- 09476 (Guyane)
- 09477 (Martinique)
- 09478, 09479 (La Réunion, Mayotte)

---

## Phase 5 : WorkManager - Synchronisation spam DB [PARTIEL]

### 5.1 SpamDbSyncWorker

- [x] Structure de base du Worker
- [ ] Téléchargement depuis URL configurable
- [ ] Vérification de signature/checksum
- [ ] Mise à jour incrémentale

---

## Phase 6 : Interface utilisateur (Compose) [COMPLÉTÉ]

### 6.1 Écrans principaux

1. **HomeScreen** [COMPLÉTÉ]
   - [x] Statistiques d'appels filtrés
   - [x] Accès rapide aux paramètres
   - [x] Navigation vers historique et debug

2. **HistoryScreen** [COMPLÉTÉ]
   - [x] Liste des appels filtrés (LazyColumn)
   - [x] Affichage par type (spam, démarcheur, masqué, inconnu)
   - [x] Actions par item

3. **SettingsScreen** [COMPLÉTÉ]
   - [x] Filtrer les inconnus (toggle)
   - [x] Base spam (toggle)
   - [x] Bloquer les démarcheurs (toggle)
   - [x] Bloquer les numéros masqués (toggle)
   - [x] SMS automatique (toggle)
   - [x] Mode confirmation SMS (toggle)
   - [x] Template SMS (éditeur)
   - [x] Cooldown SMS (slider)

4. **DebugScreen** [COMPLÉTÉ]
   - [x] Simulation d'appels entrants
   - [x] Tests de décision de filtrage
   - [x] Tests rapides (inconnu, masqué, urgence, démarcheur)
   - [x] Test SMS (vérification des conditions)
   - [x] Envoi SMS de test réel
   - [x] Affichage des logs

5. **SpamDbScreen** [À FAIRE]
   - [ ] Source actuelle
   - [ ] Dernière mise à jour
   - [ ] Statistiques
   - [ ] Actions (sync manuel, export, vider)

### 6.2 Navigation [COMPLÉTÉ]

```kotlin
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object Debug : Screen("debug")
}
```

---

## Phase 7 : Sécurité et conformité [PARTIEL]

### 7.1 Checklist sécurité

- [x] Données stockées localement uniquement
- [x] Pas de logs de numéros en production
- [x] Validation des inputs (numéros de téléphone)
- [ ] HTTPS + certificate pinning pour sync spam DB
- [x] Pas de permissions superflues

### 7.2 Conformité Google Play

- [x] Déclaration d'usage CallScreeningService
- [x] Justification SEND_SMS (SMS envoyés uniquement sur opt-in)
- [ ] Privacy Policy obligatoire
- [x] Pas d'usage abusif de l'accessibilité
- [x] Demande automatique des permissions au démarrage

### 7.3 Limitations documentées

| Limitation | Explication | Mitigation |
|------------|-------------|------------|
| Numéros masqués | Configurable maintenant | Option dans les paramètres |
| Détection mobile | Heuristique imparfaite | Option désactivable |
| VoIP | Comportement variable | Documenter |
| Double SIM | Dépend du système | Tester sur plusieurs devices |

---

## Phase 8 : Tests [COMPLÉTÉ]

### 8.1 Tests unitaires [COMPLÉTÉ]

```kotlin
// DecideCallActionUseCaseTest
- [x] returns Block when number is in blocklist
- [x] returns Allow when number is in allowlist
- [x] returns RejectAsTelemarketer for ARCEP prefixes
- [x] returns RejectAsSpam when in spam database
- [x] returns Allow when number is in contacts
- [x] returns Reject when unknown and filtering enabled
- [x] normalizes phone numbers correctly (+33, 0033, spaces, dashes)
- [x] respects priority order (blocklist > allowlist > telemarketer > spam > contact)

// ShouldSendSmsUseCaseTest
- [x] returns Skip when SMS disabled
- [x] returns Skip for excluded numbers (emergency, short, hidden)
- [x] returns Skip for non-mobile numbers
- [x] returns Skip when cooldown not elapsed
- [x] returns AskConfirmation when confirmation mode enabled
- [x] returns Send when all conditions met
```

### 8.2 Tests d'intégration [À FAIRE]

- [ ] Room DAOs avec base in-memory
- [ ] Repositories avec faux contacts
- [ ] WorkManager avec TestDriver

### 8.3 Tests UI [À FAIRE]

- [ ] Compose UI tests avec ComposeTestRule
- [ ] Navigation tests
- [ ] Screenshot tests

---

## Ressources

- [CallScreeningService Documentation](https://developer.android.com/reference/android/telecom/CallScreeningService)
- [libphonenumber](https://github.com/google/libphonenumber)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [ARCEP - Préfixes démarcheurs](https://www.arcep.fr)
