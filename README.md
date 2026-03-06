# LacheMoiLaGrappe 🍇

> *Je préfère avoir au bout du fil les gens qui m'aiment moi, pas ceux qui aiment mon porte-monnaie.*

**Marre des appels à la con ?** Marre de ces types qui vous appellent en plein repas pour vous vendre une mutuelle dont vous n'avez pas besoin, une formation CPF bidon, ou des panneaux solaires alors que vous vivez en studio ? **Marre qu'on piétine votre consentement ?**

Bienvenue. **LacheMoiLaGrappe** est la réponse.

## Le problème

De nos jours, le consentement est censé être la règle de base du savoir-vivre en société. Mais les démarcheurs téléphoniques s'en fichent complètement. Ils appellent sans votre accord, à n'importe quelle heure, pour vous vendre des trucs que vous n'avez jamais demandés. Vous avez dit non ? Ils rappellent. Vous êtes inscrit sur Bloctel ? Ils s'en tamponnent.

**C'est fini.**

## Ce que fait l'appli

LacheMoiLaGrappe est un **bouclier anti-emmerdeurs** pour votre téléphone Android. Elle filtre automatiquement les appels non désirés avant même que votre téléphone sonne :

- **Démarcheurs ARCEP** : Les 17 préfixes réservés au démarchage (0162, 0163, 0270...) sont bloqués net.
- **Numéros inconnus** : Pas dans vos contacts ? Pas de sonnerie. Simple, efficace, radical.
- **Numéros masqués** : Ceux qui se cachent derrière un numéro privé n'ont qu'à assumer.
- **Blocklist** : Les numéros indésirables sont bloqués avec notification.
- **SMS automatique** : Un message poli est envoyé à l'appelant inconnu pour lui demander de s'identifier.
- **Détection phishing SMS** : Analyse les SMS entrants pour détecter les tentatives de phishing (CPF, colis, ANTAI...). Historique dédié avec notifications prioritaires.

## Fonctionnalités UX

- **Onboarding** : Configuration guidée et gestion simplifiée des permissions.
- **Graphique d'activité** : Visualisez les 7 derniers jours d'appels bloqués sur l'accueil.
- **Quick Settings Tile** : Activez/désactivez le bouclier depuis vos raccourcis Android.
- **Historique complet** : Onglets séparés pour les appels et les SMS de phishing.
- **Swipe-to-action** : Dans l'historique, glissez pour autoriser ou bloquer définitivement.
- **Widget** : Statut du bouclier et compteur du jour directement sur votre écran d'accueil.
- **Internationalisation** : Disponible en Français et Anglais.
- **Material You** : Couleurs dynamiques s'adaptant à votre fond d'écran.

## Votre téléphone, vos règles (Sécurité & Vie Privée)

- **Zéro collecte** : Aucune donnée ne quitte votre téléphone. Pas de serveur, pas de cloud.
- **Base de données blindée** : Chiffrement **SQLCipher (AES-256)** protégé par le **Android Keystore matériel**.
- **Aucune permission INTERNET** : L'application est techniquement incapable de transmettre vos données.
- **Open source** : Code transparent et audité.
- **Gratuit** : Pas de pub, pas d'abonnement. Juste la paix.

## Paramètres

| Paramètre | Description | Défaut |
|-----------|-------------|--------|
| Filtrer les inconnus | Rejeter les appels non-contacts | ON |
| Bloquer les démarcheurs | Préfixes ARCEP officiels | ON |
| Détection phishing | Analyser les SMS malveillants | ON |
| SMS automatique | Demander l'identité par SMS | OFF |
| Mode confirmation | Valider le SMS avant envoi | ON |
| Cooldown SMS | Délai de 24h par numéro | ON |

## Architecture

Clean Architecture en Kotlin :
- **Hilt** (Injection), **Room** (Persistance), **DataStore** (Préférences).
- **Jetpack Compose** (UI réactive et moderne).
- **WorkManager** (Tâches de fond sécurisées).

## Installation

```bash
git clone https://github.com/yrbane/LacheMoiLaGrappe.git
cd LacheMoiLaGrappe
./gradlew assembleDebug
```

## Soutenir le projet

Si LacheMoiLaGrappe vous a rendu votre sérénité, vous pouvez soutenir le développement :
- PayPal : [paypal.me/sphilippe1209](https://paypal.me/sphilippe1209)
- GitHub Sponsors : [Sponsoriser](https://github.com/sponsors/yrbane)

---

*LacheMoiLaGrappe - Parce que je préfère avoir au bout du fil les gens qui m'aiment moi, pas ceux qui aiment mon porte-monnaie.*
