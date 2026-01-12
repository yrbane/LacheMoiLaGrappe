# Roadmap - CallFilter

## Vue d'ensemble

Cette roadmap définit les phases de développement de CallFilter, de la version MVP jusqu'aux fonctionnalités avancées.

---

## Version 0.1.0 - MVP (Milestone 1)

**Objectif** : Application fonctionnelle minimale capable de filtrer les appels.

### Fonctionnalités

- [x] Structure du projet Android (Kotlin, Gradle)
- [ ] CallScreeningService basique
- [ ] Lookup contacts (ContentResolver)
- [ ] Décision simple : contact = allow, inconnu = reject
- [ ] Logging basique des appels filtrés (Room)
- [ ] UI minimale : toggle ON/OFF + historique simple
- [ ] Gestion des permissions runtime

### Critères de validation

- L'app peut être installée sur Android 7+
- Les appels de contacts passent
- Les appels inconnus sont rejetés (si activé)
- L'historique affiche les derniers appels filtrés

---

## Version 0.2.0 - Base Spam (Milestone 2)

**Objectif** : Intégration de la détection de spam.

### Fonctionnalités

- [ ] Entité SpamEntry + SpamDao (Room)
- [ ] SpamRepository avec lookup
- [ ] Import manuel de base spam (JSON)
- [ ] WorkManager : synchronisation périodique
- [ ] Affichage tag + score dans l'historique
- [ ] Notification enrichie pour spam détecté
- [ ] Écran SpamDB (stats, dernière MAJ)

### Critères de validation

- Un numéro présent dans la base spam est rejeté avec le tag affiché
- La synchronisation automatique fonctionne
- L'utilisateur peut voir les statistiques de la base

---

## Version 0.3.0 - Allowlist / Blocklist (Milestone 3)

**Objectif** : Contrôle utilisateur sur les numéros.

### Fonctionnalités

- [ ] Entité UserListEntry + UserListDao
- [ ] UserListRepository
- [ ] Actions dans l'historique : "Autoriser" / "Bloquer"
- [ ] Écran de gestion des listes
- [ ] Priorité : blocklist > allowlist > spam > contacts
- [ ] Import/Export des listes (CSV)

### Critères de validation

- Un numéro en allowlist passe même s'il est inconnu
- Un numéro en blocklist est rejeté même s'il est contact
- L'utilisateur peut gérer ses listes facilement

---

## Version 0.4.0 - SMS Automatique (Milestone 4)

**Objectif** : Envoi de SMS aux appelants inconnus.

### Fonctionnalités

- [ ] Permission SEND_SMS avec explication UX
- [ ] SmsRepository (rate limiting)
- [ ] ShouldSendSmsUseCase (logique de décision)
- [ ] SendIdentitySmsUseCase
- [ ] Détection mobile via libphonenumber
- [ ] Template SMS personnalisable
- [ ] Mode confirmation (notification avant envoi)
- [ ] Historique des SMS envoyés
- [ ] Liste d'exclusion (numéros courts, urgences)

### Critères de validation

- SMS envoyé uniquement si opt-in activé
- Cooldown respecté (pas de spam)
- Mode confirmation fonctionne
- Numéros exclus ne reçoivent pas de SMS

---

## Version 0.5.0 - UI Complète (Milestone 5)

**Objectif** : Interface utilisateur aboutie.

### Fonctionnalités

- [ ] Design Material 3 complet
- [ ] Thème clair / sombre
- [ ] Écran d'accueil avec statistiques
- [ ] Historique avec filtres et recherche
- [ ] Paramètres complets
- [ ] Onboarding (première utilisation)
- [ ] Animations et transitions
- [ ] Accessibilité (TalkBack, grands textes)

### Critères de validation

- L'app est intuitive et agréable à utiliser
- Tous les écrans sont responsive
- L'accessibilité est validée

---

## Version 0.6.0 - Notifications Avancées (Milestone 6)

**Objectif** : Système de notifications intelligent.

### Fonctionnalités

- [ ] Canaux de notification (spam, inconnu, SMS)
- [ ] Regroupement des notifications
- [ ] Actions rapides depuis notification
- [ ] Résumé quotidien (optionnel)
- [ ] Widget home screen

### Critères de validation

- Les notifications sont non intrusives mais informatives
- L'utilisateur peut agir directement depuis la notification

---

## Version 1.0.0 - Release (Milestone 7)

**Objectif** : Version stable prête pour le Play Store.

### Fonctionnalités

- [ ] Tests unitaires complets (>80% coverage)
- [ ] Tests d'intégration
- [ ] Tests UI
- [ ] Performance optimisée
- [ ] ProGuard / R8 configuration
- [ ] Privacy Policy
- [ ] Déclarations Play Store
- [ ] Screenshots et assets marketing
- [ ] Documentation utilisateur

### Critères de validation

- Tous les tests passent
- Pas de crash en production (Firebase Crashlytics)
- Conforme aux guidelines Play Store

---

## Versions futures (Post 1.0)

### Version 1.1.0 - Intégrations

- [ ] API externe pour validation HLR (mobile vs fixe)
- [ ] Intégration services tiers (Truecaller-like, optionnel)
- [ ] Contribution communautaire à la base spam
- [ ] Backup/Restore Google Drive

### Version 1.2.0 - Intelligence

- [ ] Apprentissage des habitudes utilisateur
- [ ] Suggestions automatiques (allowlist)
- [ ] Analyse des patterns d'appels
- [ ] Heure de filtrage (ne pas filtrer la nuit, etc.)

### Version 1.3.0 - Multi-plateforme

- [ ] Synchronisation multi-devices
- [ ] Application Wear OS companion
- [ ] Widget Android Auto

---

## Indicateurs de progression

| Milestone | Status | Progress |
|-----------|--------|----------|
| 0.1.0 MVP | En cours | 0% |
| 0.2.0 Base Spam | Planifié | 0% |
| 0.3.0 Allowlist/Blocklist | Planifié | 0% |
| 0.4.0 SMS Auto | Planifié | 0% |
| 0.5.0 UI Complète | Planifié | 0% |
| 0.6.0 Notifications | Planifié | 0% |
| 1.0.0 Release | Planifié | 0% |

---

## Priorités

1. **P0 - Critique** : Fonctionnalités de base du filtrage
2. **P1 - Important** : Base spam et listes utilisateur
3. **P2 - Normal** : SMS automatique et UI avancée
4. **P3 - Nice to have** : Notifications avancées et intégrations

---

## Notes

- Chaque milestone correspond à une version déployable
- Les issues seront liées aux milestones correspondants
- Les PR doivent référencer les issues qu'elles résolvent
- Revue de code obligatoire avant merge
