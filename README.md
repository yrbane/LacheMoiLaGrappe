# LacheMoiLaGrappe

> *Je prefere avoir au bout du fil les gens qui m'aiment moi, pas ceux qui aiment mon porte-monnaie.*

**Marre des appels a la con ?** Marre de ces types qui vous appellent en plein repas pour vous vendre une mutuelle dont vous n'avez pas besoin, une formation CPF bidon, ou des panneaux solaires alors que vous vivez en studio ? **Marre qu'on pietine votre consentement ?**

Bienvenue. **LacheMoiLaGrappe** est la reponse.

## Le probleme

De nos jours, le consentement est cense etre la regle de base du savoir-vivre en societe. Mais les demarcheurs telephoniques s'en fichent completement. Ils appellent sans votre accord, a n'importe quelle heure, pour vous vendre des trucs que vous n'avez jamais demandes. Vous avez dit non ? Ils rappellent. Vous etes inscrit sur Bloctel ? Ils s'en tamponnent.

**C'est fini.**

## Ce que fait l'appli

LacheMoiLaGrappe est un **bouclier anti-emmerdeurs** pour votre telephone Android. Elle filtre automatiquement les appels non desires avant meme que votre telephone sonne :

- **Demarcheurs ARCEP** : Les 17 prefixes reserves au demarchage (0162, 0163, 0270...) sont bloques net. L'ARCEP les a identifies, on les degomme
- **Numeros inconnus** : Pas dans vos contacts ? Pas de sonnerie. Simple, efficace, radical
- **Numeros masques** : Ceux qui se cachent derriere un numero prive n'ont qu'a assumer. Option activable
- **Base spam** : Les numeros signales comme spam sont detectes et rejetes avec notification
- **SMS automatique** : Un message poli est envoye a l'appelant inconnu pour lui demander de s'identifier. Parce qu'on est pas des sauvages non plus
- **Allowlist / Blocklist** : Vous gardez le controle total. Autorisez ou bloquez qui vous voulez
- **Prefixes personnalises** : Vous repérez un nouveau schema de demarchage ? Ajoutez le prefix vous-meme

## Votre telephone, vos regles

- **Zero collecte de donnees** : Tout reste sur votre telephone. Pas de serveur, pas de tracking, pas de business louche avec vos donnees
- **Open source** : Le code est la, lisez-le. On n'a rien a cacher (contrairement aux demarcheurs)
- **Gratuit** : Pas de pub, pas d'abonnement, pas de "version premium". Juste la paix

## Prefixes demarcheurs bloques (ARCEP)

### France metropolitaine
- 0162, 0163, 0270, 0271, 0377, 0378
- 0424, 0425, 0568, 0569, 0948, 0949

### DOM-TOM
- 09475 (Guadeloupe, Saint-Martin, Saint-Barthelemy)
- 09476 (Guyane)
- 09477 (Martinique)
- 09478, 09479 (La Reunion, Mayotte)

### Prefixes personnalises
En plus des 17 prefixes ARCEP officiels, ajoutez vos propres prefixes depuis les parametres (4-5 chiffres).

## Installation

```bash
git clone https://github.com/yrbane/LacheMoiLaGrappe.git
cd LacheMoiLaGrappe
./gradlew assembleDebug
```

Ou directement depuis le [Google Play Store](https://play.google.com/store/apps/details?id=fr.lachemoilagrappe).

## Prerequis

- Android 7.0+ (API 24+)
- Permissions :
  - `READ_CONTACTS` : Pour laisser passer vos vrais contacts
  - `READ_PHONE_STATE` / `READ_CALL_LOG` : Pour intercepter les appels
  - `SEND_SMS` (optionnel) : Pour repondre poliment aux inconnus
  - `POST_NOTIFICATIONS` : Pour vous informer des appels bloques

## Parametres

| Parametre | Description | Defaut |
|-----------|-------------|--------|
| Filtrer les inconnus | Rejeter les appels non-contacts | ON |
| Base spam | Detection des numeros spam | ON |
| Bloquer les demarcheurs | Prefixes ARCEP | ON |
| Numeros masques | Rejeter les appels prives | OFF |
| SMS automatique | Reponse aux inconnus | OFF |
| Mode confirmation | Valider avant envoi SMS | ON |
| Cooldown SMS | Delai entre SMS (meme numero) | 24h |
| Template SMS | Message personnalisable | Defaut |

## Architecture

Clean Architecture en Kotlin :

- **Domain** : UseCases (DecideCallAction, ShouldSendSms, SendIdentitySms, LogCallEvent)
- **Data** : Room DB, DataStore, Repositories
- **UI** : Jetpack Compose + Material 3 + Hilt ViewModels
- **Service** : CallScreeningService natif Android

### Stack

Kotlin 2.0 | Jetpack Compose | Room | Hilt | WorkManager | Flow/Coroutines | libphonenumber | DataStore

## Tests

```bash
./gradlew test
```

## Licence

MIT License - Voir [LICENSE](LICENSE)

## Soutenir le projet

Cette appli est **gratuite, open-source, sans pub, sans tracking**. Contrairement aux demarcheurs, je ne vous vendrai jamais rien. Mais si LacheMoiLaGrappe vous a rendu un peu de serenite telephonique et que vous voulez filer un coup de pouce a un dev qui galere (mais qui au moins n'essaye pas de vendre des trucs a des gens innocents), c'est par ici :

| Methode | Lien |
|---------|------|
| PayPal | [paypal.me/sphilippe1209](https://paypal.me/sphilippe1209) |
| GitHub Sponsors | [Sponsoriser sur GitHub](https://github.com/sponsors/yrbane) |
| Bitcoin | `bc1qgs7qk2zstyu4c6vs4m60757zh9tr0umznlxf5k` |

Meme un cafe, ca fait plaisir. Et ca me motive a continuer de faire chier les demarcheurs.

## Contribuer

Les contributions sont les bienvenues. Plus on est nombreux contre les demarcheurs, mieux c'est.

---

*LacheMoiLaGrappe - Parce que je prefere avoir au bout du fil les gens qui m'aiment moi, pas ceux qui aiment mon porte-monnaie.*
