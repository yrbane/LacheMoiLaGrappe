# CallFilter

Application Android de filtrage intelligent des appels entrants.

## Description

CallFilter est une application Android qui filtre automatiquement les appels entrants non souhaités. Elle rejette les appels provenant de numéros inconnus (non enregistrés dans vos contacts) et détecte les spams grâce à une base de données locale actualisable.

## Fonctionnalités principales

- **Filtrage des appels inconnus** : Rejette automatiquement les appels dont le numéro n'est pas dans vos contacts
- **Détection des spams** : Consultation d'une base de données locale (catégorie, score, source) avec notifications
- **SMS automatique** : Envoi d'un message poli aux appelants inconnus demandant leur identité (opt-in)
- **Historique complet** : Consultation de tous les appels filtrés avec actions possibles
- **Allowlist / Blocklist** : Gestion manuelle des numéros autorisés ou bloqués
- **Respect de la vie privée** : Toutes les données restent locales par défaut

## Prérequis

- Android 7.0+ (API 24+)
- Permissions requises :
  - `READ_CONTACTS` : Vérification des contacts
  - `READ_PHONE_STATE` : Identification des appels
  - `SEND_SMS` : Envoi de SMS automatiques (optionnel)
  - `POST_NOTIFICATIONS` : Notifications d'appels rejetés

## Architecture

L'application suit une architecture Clean Architecture avec :

- **Domain Layer** : Use-cases métier (DecideCallAction, LookupSpam, SendIdentitySms...)
- **Data Layer** : Repositories (Contacts, Spam, Settings, CallLog, SMS)
- **Presentation Layer** : UI Jetpack Compose + ViewModels

### Stack technique

- Kotlin
- Jetpack Compose (UI)
- Room (base de données locale)
- Hilt (injection de dépendances)
- WorkManager (synchronisation en arrière-plan)
- Flow / Coroutines (programmation réactive)
- libphonenumber (normalisation des numéros)

## Installation

```bash
git clone https://github.com/USERNAME/callfilter.git
cd callfilter
./gradlew assembleDebug
```

## Configuration

### Activation du service

L'application utilise `CallScreeningService` d'Android. Au premier lancement :

1. Accordez les permissions demandées
2. Définissez CallFilter comme service de filtrage d'appels
3. Configurez vos préférences dans les paramètres

### Paramètres disponibles

| Paramètre | Description | Défaut |
|-----------|-------------|--------|
| Filtrer les inconnus | Rejeter les appels non-contacts | ON |
| Base spam | Activer la détection spam | ON |
| SMS automatique | Envoyer un SMS aux inconnus | OFF |
| Mode confirmation | Valider avant envoi SMS | ON |
| Cooldown SMS | Délai entre SMS (même numéro) | 24h |

## Conformité Google Play

- Utilisation exclusive de `CallScreeningService` (pas d'abus d'accessibilité)
- SMS opt-in uniquement avec consentement explicite
- Pas de collecte de données distante sans consentement
- Respect des guidelines de permissions runtime

## Limitations connues

- Détection mobile/fixe non fiable à 100% (heuristique libphonenumber)
- Numéros masqués non gérables
- Certains appels VoIP peuvent ne pas être interceptés
- Double SIM : comportement selon configuration système

## Licence

MIT License - Voir [LICENSE](LICENSE)

## Contribution

Les contributions sont bienvenues. Voir [CONTRIBUTING.md](CONTRIBUTING.md) pour les guidelines.

## Contact

Pour signaler un bug ou proposer une fonctionnalité, ouvrez une issue sur GitHub.
