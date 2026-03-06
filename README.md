# LacheMoiLaGrappe

> *Je préfère avoir au bout du fil les gens qui m'aiment moi, pas ceux qui aiment mon porte-monnaie.*

**Marre des appels à la con ?** Marre de ces types qui vous appellent en plein repas pour vous vendre une mutuelle dont vous n'avez pas besoin, une formation CPF bidon, ou des panneaux solaires alors que vous vivez en studio ? **Marre qu'on piétine votre consentement ?**

Bienvenue. **LacheMoiLaGrappe** est la réponse.

## Le problème

De nos jours, le consentement est censé être la règle de base du savoir-vivre en société. Mais les démarcheurs téléphoniques s'en fichent complètement. Ils appellent sans votre accord, à n'importe quelle heure, pour vous vendre des trucs que vous n'avez jamais demandés. Vous avez dit non ? Ils rappellent. Vous êtes inscrit sur Bloctel ? Ils s'en tamponnent.

**C'est fini.**

## Ce que fait l'appli

LacheMoiLaGrappe est un **bouclier anti-emmerdeurs** pour votre téléphone Android. Elle filtre automatiquement les appels non désirés avant même que votre téléphone sonne :

- **Démarcheurs ARCEP** : Les 17 préfixes réservés au démarchage (0162, 0163, 0270...) sont bloqués net. L'ARCEP les a identifiés, on les dégomme
- **Numéros inconnus** : Pas dans vos contacts ? Pas de sonnerie. Simple, efficace, radical
- **Numéros masqués** : Ceux qui se cachent derrière un numéro privé n'ont qu'à assumer. Option activable
- **Blocklist** : Les numéros indésirables sont bloqués avec notification
- **SMS automatique** : Un message poli est envoyé à l'appelant inconnu pour lui demander de s'identifier. Parce qu'on n'est pas des sauvages non plus
- **Allowlist / Blocklist** : Écran dédié pour gérer vos listes, avec ajout rapide et swipe-to-delete
- **Préfixes personnalisés** : Vous repérez un nouveau schéma de démarchage ? Ajoutez le préfixe vous-même

## Fonctionnalités UX

- **Onboarding** : Écran d'accueil en 3 pages pour guider la configuration initiale et les permissions
- **Swipe-to-action** : Dans l'historique, glissez à droite pour autoriser, à gauche pour bloquer
- **Pull-to-refresh** : Tirez vers le bas pour rafraîchir l'historique
- **Recherche** : Cherchez dans l'historique par numéro ou nom de contact
- **Compteur total** : Nombre d'appels bloqués depuis l'installation, affiché sur l'accueil
- **Notifications groupées** : Les notifications de blocage sont regroupées pour ne pas spammer
- **Export CSV** : Exportez votre historique d'appels filtrés en CSV
- **Retour haptique** : Vibration de confirmation sur les actions autoriser/bloquer
- **Widget** : Widget home screen affichant le nombre d'appels bloqués aujourd'hui
- **Icône thématique** : Icône monochrome adaptative pour Android 13+

## Votre téléphone, vos règles

- **Zéro collecte de données** : Tout reste sur votre téléphone. Pas de serveur, pas de tracking, pas de business louche avec vos données
- **Base de données chiffrée** : Vos données sont protégées par SQLCipher (AES-256) avec une clé gérée par le Android Keystore matériel
- **Aucune permission réseau** : L'appli ne demande même pas l'accès à Internet. Rien ne sort
- **Open source** : Le code est là, lisez-le. On n'a rien à cacher (contrairement aux démarcheurs)
- **Gratuit** : Pas de pub, pas d'abonnement, pas de "version premium". Juste la paix

## Préfixes démarcheurs bloqués (ARCEP)

### France métropolitaine
- 0162, 0163, 0270, 0271, 0377, 0378
- 0424, 0425, 0568, 0569, 0948, 0949

### DOM-TOM
- 09475 (Guadeloupe, Saint-Martin, Saint-Barthélemy)
- 09476 (Guyane)
- 09477 (Martinique)
- 09478, 09479 (La Réunion, Mayotte)

### Préfixes personnalisés
En plus des 17 préfixes ARCEP officiels, ajoutez vos propres préfixes depuis les paramètres (4-5 chiffres).

## Installation

```bash
git clone https://github.com/yrbane/LacheMoiLaGrappe.git
cd LacheMoiLaGrappe
./gradlew assembleDebug
```

Ou directement depuis le [Google Play Store](https://play.google.com/store/apps/details?id=fr.lachemoilagrappe) (en attente de validation).

## Prérequis

- Android 7.0+ (API 24+)
- Permissions :
  - `READ_CONTACTS` : Pour laisser passer vos vrais contacts
  - `READ_PHONE_STATE` / `READ_CALL_LOG` : Pour intercepter les appels
  - `SEND_SMS` (optionnel) : Pour répondre poliment aux inconnus
  - `POST_NOTIFICATIONS` : Pour vous informer des appels bloqués

## Paramètres

| Paramètre | Description | Défaut |
|-----------|-------------|--------|
| Filtrer les inconnus | Rejeter les appels non-contacts | ON |
| Bloquer les démarcheurs | Préfixes ARCEP | ON |
| Numéros masqués | Rejeter les appels privés | OFF |
| SMS automatique | Réponse aux inconnus | OFF |
| Mode confirmation | Valider avant envoi SMS | ON |
| Cooldown SMS | Délai entre SMS (même numéro) | 24h |
| Template SMS | Message personnalisable | Défaut |

## Architecture

Clean Architecture en Kotlin :

```
fr.lachemoilagrappe/
├── di/                     # Injection de dépendances (Hilt)
├── domain/
│   ├── model/              # Entités métier
│   ├── repository/         # Interfaces repositories
│   └── usecase/            # Use-cases
├── data/
│   ├── local/
│   │   ├── db/             # Room (entities, DAOs)
│   │   └── preferences/    # DataStore

│   └── repository/         # Implémentations
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
│   │   ├── home/           # Accueil + stats + toggles
│   │   ├── history/        # Historique avec swipe/search
│   │   ├── settings/       # Paramètres complets
│   │   ├── onboarding/     # Écran de première utilisation
│   │   ├── userlists/      # Gestion allow/blocklist
│   │   └── debug/          # Écran de test (debug only)
│   └── navigation/
└── util/                   # Helpers (phone parsing, etc.)
```

### Stack

Kotlin 2.0 | Jetpack Compose | Material 3 | Room | Hilt | WorkManager | Flow/Coroutines | libphonenumber | DataStore | SQLCipher

## Tests

```bash
./gradlew test
```

## Vie privée

Politique de confidentialité : [yrbane.github.io/LacheMoiLaGrappe/privacy](https://yrbane.github.io/LacheMoiLaGrappe/privacy.html)

## Licence

MIT License - Voir [LICENSE](LICENSE)

## Soutenir le projet

Cette appli est **gratuite, open-source, sans pub, sans tracking**. Contrairement aux démarcheurs, je ne vous vendrai jamais rien. Mais si LacheMoiLaGrappe vous a rendu un peu de sérénité téléphonique et que vous voulez filer un coup de pouce à un dev qui galère (mais qui au moins n'essaye pas de vendre des trucs à des gens innocents), c'est par ici :

| Méthode | Lien |
|---------|------|
| PayPal | [paypal.me/sphilippe1209](https://paypal.me/sphilippe1209) |
| GitHub Sponsors | [Sponsoriser sur GitHub](https://github.com/sponsors/yrbane) |
| Bitcoin | `bc1qgs7qk2zstyu4c6vs4m60757zh9tr0umznlxf5k` |

Même un café, ça fait plaisir. Et ça me motive à continuer de faire chier les démarcheurs.

## Contribuer

Les contributions sont les bienvenues. Plus on est nombreux contre les démarcheurs, mieux c'est.

---

*LacheMoiLaGrappe - Parce que je préfère avoir au bout du fil les gens qui m'aiment moi, pas ceux qui aiment mon porte-monnaie.*
