# Roadmap - LacheMoiLaGrappe

## Vue d'ensemble

Cette roadmap definit les phases de developpement de LacheMoiLaGrappe, de la version MVP jusqu'aux fonctionnalites avancees.

---

## Version 0.1.0 - MVP [COMPLETE]

**Objectif** : Application fonctionnelle minimale capable de filtrer les appels.

- [x] Structure du projet Android (Kotlin, Gradle)
- [x] CallScreeningService basique
- [x] Lookup contacts (ContentResolver)
- [x] Decision simple : contact = allow, inconnu = reject
- [x] Logging des appels filtres (Room)
- [x] UI minimale : toggle ON/OFF + historique simple
- [x] Gestion des permissions runtime

---

## Version 0.2.0 - Allowlist / Blocklist [COMPLETE]

**Objectif** : Controle utilisateur sur les numeros.

- [x] Entite UserListEntry + UserListDao
- [x] UserListRepository
- [x] Actions dans l'historique : "Autoriser" / "Bloquer"
- [x] Ecran dedie de gestion des listes (onglets, swipe-to-delete, FAB)
- [x] Priorite : blocklist > allowlist > contacts

---

## Version 0.4.0 - SMS Automatique [COMPLETE]

**Objectif** : Envoi de SMS aux appelants inconnus.

- [x] SmsRepository (rate limiting)
- [x] ShouldSendSmsUseCase + SendIdentitySmsUseCase
- [x] Detection mobile via libphonenumber
- [x] Template SMS personnalisable
- [x] Mode confirmation (notification avant envoi)
- [x] Liste d'exclusion (numeros courts, urgences)

---

## Version 0.4.1 - Blocage demarcheurs ARCEP [COMPLETE]

- [x] Detection des 17 prefixes ARCEP (metropole + DOM-TOM)
- [x] Option configurable dans les parametres
- [x] Notification specifique + historique

---

## Version 0.4.2 - Blocage numeros masques [COMPLETE]

- [x] Detection des numeros masques/caches
- [x] Option configurable dans les parametres
- [x] Notification specifique + historique

---

## Version 0.4.3 - Tests et Debug [COMPLETE]

- [x] Ecran de debug avec simulation d'appels
- [x] Tests rapides (inconnu, masque, urgence, demarcheur)
- [x] Tests unitaires (35+ tests)

---

## Version 1.0.0 - Release [COMPLETE]

**Objectif** : Version stable prete pour le Play Store.

### UX complete
- [x] Onboarding 3 pages (bienvenue, fonctionnement, permissions)
- [x] Home screen avec stats, compteur total, toggles avec icones
- [x] Historique avec groupement par jour, swipe-to-action, recherche, pull-to-refresh
- [x] Export CSV de l'historique
- [x] Retour haptique sur les actions
- [x] Ecran gestion allowlist/blocklist avec onglets
- [x] Section test du filtrage en bas de l'accueil (debug only)

### Notifications avancees
- [x] Canaux de notification (rejete, SMS)
- [x] Notifications groupees avec resume
- [x] Actions rapides depuis notification

### Widget et icone
- [x] Widget home screen (appels bloques aujourd'hui)
- [x] Icone monochrome adaptative (Android 13+)

### Securite
- [x] Chiffrement de la base de donnees avec SQLCipher (AES-256)
- [x] Cle de chiffrement protegee par Android Keystore materiel
- [x] Aucune permission INTERNET (zero acces reseau)

### Publication
- [x] Namespace fr.lachemoilagrappe
- [x] Keystore de release + ProGuard/R8
- [x] Privacy Policy (GitHub Pages)
- [x] Assets Play Store (icone 512, feature graphic)
- [x] Documentation complete (README, PLAN, ROADMAP, guide Play Store)
- [x] Structure fastlane pour F-Droid
- [x] Licence MIT

---

## Versions futures (Post 1.0)

### Version 1.1.0 - Phishing, Stats & i18n [COMPLETE]

- [x] Detection phishing SMS (mots-cles : CPF, colis, Chronopost, ANTAI, carte vitale...)
- [x] SmsReceiver + AnalyzeSmsContentUseCase + PhishingSmsDao
- [x] Historique phishing avec onglets dans l'ecran History
- [x] Graphique d'activite des 7 derniers jours sur l'ecran d'accueil
- [x] Quick Settings Tile (activation/desactivation du filtrage)
- [x] Internationalisation : interface en anglais (values-en/strings.xml)
- [x] Chiffrement SQLCipher + Android Keystore
- [x] Suppression feature spam + permission INTERNET
- [x] Structure fastlane + licence MIT (conformite F-Droid)

### Version 1.2.0 - Ameliorations

- [ ] Theme clair / sombre
- [ ] Accessibilite (TalkBack, grands textes)
- [ ] Backup/Restore Google Drive
- [ ] Tests d'integration et UI

### Version 1.3.0 - Intelligence

- [ ] Apprentissage des habitudes utilisateur
- [ ] Suggestions automatiques (allowlist)
- [ ] Analyse des patterns d'appels
- [ ] Plages horaires de filtrage

### Version 1.4.0 - Integrations

- [ ] API externe pour validation HLR (mobile vs fixe)
- [ ] Application Wear OS companion
- [ ] Widget Android Auto

---

## Indicateurs de progression

| Milestone | Status | Progress |
|-----------|--------|----------|
| 0.1.0 MVP | Complete | 100% |
| 0.2.0 Allowlist/Blocklist | Complete | 100% |
| 0.3.0 SMS Auto | Complete | 100% |
| 0.3.1 Demarcheurs ARCEP | Complete | 100% |
| 0.3.2 Numeros masques | Complete | 100% |
| 0.3.3 Tests/Debug | Complete | 100% |
| 1.0.0 Release | Complete | 100% |
| 1.1.0 Phishing/Stats/i18n | Complete | 100% |
