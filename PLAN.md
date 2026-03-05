# Plan d'Implementation - LacheMoiLaGrappe

## Vue d'ensemble

Ce document detaille le plan d'implementation de LacheMoiLaGrappe, une application Android de filtrage d'appels.

---

## Phase 1 : Fondations du projet [COMPLETE]

### 1.1 Configuration du projet Android

- [x] Creer le projet Android Studio avec Kotlin
- [x] Configurer Gradle (Kotlin DSL)
- [x] Definir minSdk 24, targetSdk 35
- [x] Ajouter les dependances Jetpack (Room, Hilt, WorkManager, Compose)
- [x] Configurer libphonenumber

### 1.2 Structure des packages

```
fr.lachemoilagrappe/
├── di/                     # Injection de dependances (Hilt)
├── domain/
│   ├── model/              # Entites metier
│   ├── repository/         # Interfaces repositories
│   └── usecase/            # Use-cases
├── data/
│   ├── local/
│   │   ├── db/             # Room (entities, DAOs)
│   │   └── preferences/    # DataStore
│   ├── remote/             # API spam (optionnel)
│   └── repository/         # Implementations
├── service/
│   ├── CallFilterScreeningService.kt
│   ├── NotificationHelper.kt
│   ├── ActionReceiver.kt
│   └── SmsService.kt
├── widget/                 # Widget home screen
├── worker/                 # WorkManager jobs
├── ui/
│   ├── theme/
│   ├── screens/
│   │   ├── home/
│   │   ├── history/
│   │   ├── settings/
│   │   ├── onboarding/
│   │   ├── userlists/
│   │   └── debug/          # Ecran de test (debug only)
│   └── navigation/
└── util/                   # Helpers (phone parsing, etc.)
```

### 1.3 Manifest et permissions [COMPLETE]

- [x] READ_CONTACTS, READ_PHONE_STATE, READ_CALL_LOG
- [x] SEND_SMS, POST_NOTIFICATIONS, INTERNET
- [x] RECEIVE_BOOT_COMPLETED, FOREGROUND_SERVICE
- [x] CallScreeningService declaration
- [x] FileProvider pour export CSV
- [x] ActionReceiver pour actions notifications
- [x] Widget AppWidgetProvider

---

## Phase 2 : Couche Data [COMPLETE]

### 2.1 Base de donnees Room

#### Entites
- [x] CallLogEntry - Historique des appels filtres
- [x] SpamEntry - Base de donnees spam
- [x] UserListEntry - Allowlist/Blocklist utilisateur
- [x] SmsLogEntry - Historique des SMS envoyes

#### DAOs [COMPLETE]
- [x] CallLogDao : CRUD historique + recherche + compteurs
- [x] SpamDao : Lookup + bulk insert/update
- [x] UserListDao : Gestion allow/block
- [x] SmsLogDao : Rate limiting + historique

### 2.2 Repositories [COMPLETE]

- [x] ContactsRepository
- [x] SpamRepository
- [x] UserListRepository
- [x] CallLogRepository (avec recherche et compteur total)
- [x] SmsRepository
- [x] SettingsRepository (DataStore, avec onboarding state)

---

## Phase 3 : Couche Domain [COMPLETE]

### 3.1 Use Cases [COMPLETE]

- [x] DecideCallActionUseCase - Logique de decision avec prefixes demarcheurs
- [x] LookupSpamUseCase - Recherche dans la base spam
- [x] ShouldSendSmsUseCase - Decision d'envoi SMS
- [x] SendIdentitySmsUseCase - Envoi SMS avec status tracking
- [x] LogCallEventUseCase - Journalisation des appels

### 3.2 Modeles Domain [COMPLETE]

- [x] CallAction (Allow, Reject, RejectAsSpam, RejectAsTelemarketer, RejectAsHidden, Block)
- [x] CallDecision (ALLOWED, REJECTED, REJECTED_SPAM, REJECTED_TELEMARKETER, REJECTED_HIDDEN, BLOCKED)
- [x] SmsDecision (Send, AskConfirmation, Skip)
- [x] ListType (ALLOW, BLOCK)

---

## Phase 4 : Service de filtrage [COMPLETE]

### 4.1 CallFilterScreeningService [COMPLETE]

- [x] Interception des appels entrants
- [x] Verification blocklist/allowlist
- [x] Detection des 17 prefixes demarcheurs ARCEP
- [x] Detection des numeros masques (configurable)
- [x] Recherche dans base spam
- [x] Verification contacts
- [x] Decision de filtrage
- [x] Notification d'appel rejete (avec groupement)
- [x] Journalisation dans Room
- [x] Gestion SMS automatique

---

## Phase 5 : Interface utilisateur [COMPLETE]

### 5.1 Ecrans

1. **OnboardingScreen** [COMPLETE]
   - [x] Page bienvenue avec tagline
   - [x] Page "Comment ca marche" avec features
   - [x] Page permissions avec demande groupee
   - [x] Demande du role CallScreening
   - [x] HorizontalPager avec indicateurs dots

2. **HomeScreen** [COMPLETE]
   - [x] Statistiques d'appels filtres (aujourd'hui)
   - [x] Compteur total depuis l'installation
   - [x] Toggles rapides avec icones (filtrage, spam, SMS)
   - [x] Badge Actif/Inactif
   - [x] Carte d'activation si service non actif
   - [x] Section test du filtrage (debug only)
   - [x] Navigation vers historique, parametres, listes

3. **HistoryScreen** [COMPLETE]
   - [x] Liste groupee par jour (Aujourd'hui, Hier, date)
   - [x] Swipe-to-action (droite=autoriser, gauche=bloquer)
   - [x] Pull-to-refresh
   - [x] Barre de recherche extensible
   - [x] Export CSV via partage
   - [x] Retour haptique sur actions
   - [x] Snackbar de confirmation
   - [x] Etat vide adaptatif (recherche vs vide)

4. **UserListsScreen** [COMPLETE]
   - [x] Onglets Autorises / Bloques
   - [x] Swipe-to-delete
   - [x] FAB pour ajouter un numero
   - [x] Dialog avec numero + label optionnel
   - [x] Etats vides par onglet

5. **SettingsScreen** [COMPLETE]
   - [x] Tous les toggles de filtrage
   - [x] Configuration SMS (template, cooldown, confirmation)
   - [x] Prefixes demarcheurs personnalises
   - [x] Lien politique de confidentialite
   - [x] Section soutien (PayPal, GitHub)
   - [x] Version de l'app

6. **DebugScreen** [COMPLETE] (debug builds only)
   - [x] Simulation d'appels entrants
   - [x] Tests rapides (inconnu, masque, urgence, demarcheur)
   - [x] Test SMS
   - [x] Affichage des logs

### 5.2 Navigation [COMPLETE]

- [x] Onboarding -> Home (avec skip si deja complete)
- [x] Home -> History, Settings, UserLists, Debug
- [x] Debug conditionnel (BuildConfig.DEBUG)

### 5.3 Widget [COMPLETE]

- [x] Widget RemoteViews (3x1)
- [x] Affichage appels bloques aujourd'hui
- [x] Badge Actif/Inactif
- [x] Clic ouvre l'app
- [x] Refresh automatique

### 5.4 Icone [COMPLETE]

- [x] Adaptive icon (foreground + background)
- [x] Monochrome layer pour Android 13+

---

## Phase 6 : Notifications [COMPLETE]

- [x] Canaux de notification (rejete, spam, SMS)
- [x] Regroupement des notifications (setGroup)
- [x] Notification resumee (summary)
- [x] Actions rapides depuis notification (autoriser/bloquer)
- [x] Notification confirmation SMS

---

## Phase 7 : Publication [COMPLETE]

- [x] Namespace fr.lachemoilagrappe
- [x] Keystore de release
- [x] ProGuard / R8 configuration
- [x] Privacy Policy (GitHub Pages)
- [x] Assets Play Store (icone 512, feature graphic)
- [x] Guide de publication
- [x] README complet
- [x] FUNDING.yml (GitHub Sponsors, PayPal)
- [x] FileProvider pour export CSV

---

## Phase 8 : Tests [PARTIEL]

### 8.1 Tests unitaires [COMPLETE]

- [x] DecideCallActionUseCaseTest (20+ tests)
- [x] ShouldSendSmsUseCaseTest (15+ tests)

### 8.2 Tests d'integration [A FAIRE]

- [ ] Room DAOs avec base in-memory
- [ ] Repositories avec faux contacts
- [ ] WorkManager avec TestDriver

### 8.3 Tests UI [A FAIRE]

- [ ] Compose UI tests avec ComposeTestRule
- [ ] Navigation tests
- [ ] Screenshot tests

---

## Ressources

- [CallScreeningService Documentation](https://developer.android.com/reference/android/telecom/CallScreeningService)
- [libphonenumber](https://github.com/google/libphonenumber)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [ARCEP - Prefixes demarcheurs](https://www.arcep.fr)
