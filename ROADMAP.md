# Roadmap - CallFilter

## Vue d'ensemble

Cette roadmap définit les phases de développement de CallFilter, de la version MVP jusqu'aux fonctionnalités avancées.

---

## Version 0.1.0 - MVP (Milestone 1) [COMPLÉTÉ]

**Objectif** : Application fonctionnelle minimale capable de filtrer les appels.

### Fonctionnalités

- [x] Structure du projet Android (Kotlin, Gradle)
- [x] CallScreeningService basique
- [x] Lookup contacts (ContentResolver)
- [x] Décision simple : contact = allow, inconnu = reject
- [x] Logging basique des appels filtrés (Room)
- [x] UI minimale : toggle ON/OFF + historique simple
- [x] Gestion des permissions runtime
- [x] Demande automatique des permissions au démarrage

### Critères de validation

- [x] L'app peut être installée sur Android 7+
- [x] Les appels de contacts passent
- [x] Les appels inconnus sont rejetés (si activé)
- [x] L'historique affiche les derniers appels filtrés

---

## Version 0.2.0 - Base Spam (Milestone 2) [COMPLÉTÉ]

**Objectif** : Intégration de la détection de spam.

### Fonctionnalités

- [x] Entité SpamEntry + SpamDao (Room)
- [x] SpamRepository avec lookup
- [x] Import manuel de base spam (JSON)
- [ ] WorkManager : synchronisation périodique (partiel)
- [x] Affichage tag + score dans l'historique
- [x] Notification enrichie pour spam détecté
- [ ] Écran SpamDB (stats, dernière MAJ)

### Critères de validation

- [x] Un numéro présent dans la base spam est rejeté avec le tag affiché
- [ ] La synchronisation automatique fonctionne
- [ ] L'utilisateur peut voir les statistiques de la base

---

## Version 0.3.0 - Allowlist / Blocklist (Milestone 3) [COMPLÉTÉ]

**Objectif** : Contrôle utilisateur sur les numéros.

### Fonctionnalités

- [x] Entité UserListEntry + UserListDao
- [x] UserListRepository
- [x] Actions dans l'historique : "Autoriser" / "Bloquer"
- [ ] Écran de gestion des listes
- [x] Priorité : blocklist > allowlist > spam > contacts
- [ ] Import/Export des listes (CSV)

### Critères de validation

- [x] Un numéro en allowlist passe même s'il est inconnu
- [x] Un numéro en blocklist est rejeté même s'il est contact
- [ ] L'utilisateur peut gérer ses listes facilement

---

## Version 0.4.0 - SMS Automatique (Milestone 4) [COMPLÉTÉ]

**Objectif** : Envoi de SMS aux appelants inconnus.

### Fonctionnalités

- [x] Permission SEND_SMS avec explication UX
- [x] SmsRepository (rate limiting)
- [x] ShouldSendSmsUseCase (logique de décision)
- [x] SendIdentitySmsUseCase
- [x] Détection mobile via libphonenumber
- [x] Template SMS personnalisable
- [x] Mode confirmation (notification avant envoi)
- [x] Historique des SMS envoyés
- [x] Liste d'exclusion (numéros courts, urgences)

### Critères de validation

- [x] SMS envoyé uniquement si opt-in activé
- [x] Cooldown respecté (pas de spam)
- [x] Mode confirmation fonctionne
- [x] Numéros exclus ne reçoivent pas de SMS

---

## Version 0.4.1 - Blocage des démarcheurs ARCEP [COMPLÉTÉ]

**Objectif** : Blocage automatique des numéros démarcheurs.

### Fonctionnalités

- [x] Détection des 17 préfixes ARCEP (métropole + DOM-TOM)
- [x] Option configurable dans les paramètres
- [x] Notification spécifique pour démarcheurs
- [x] Affichage dans l'historique

### Préfixes bloqués

- France métropolitaine : 0162, 0163, 0270, 0271, 0377, 0378, 0424, 0425, 0568, 0569, 0948, 0949
- DOM-TOM : 09475, 09476, 09477, 09478, 09479

---

## Version 0.4.2 - Blocage des numéros masqués [COMPLÉTÉ]

**Objectif** : Option pour filtrer les appels masqués.

### Fonctionnalités

- [x] Détection des numéros masqués/cachés
- [x] Option configurable dans les paramètres
- [x] Notification spécifique pour numéros masqués
- [x] Affichage dans l'historique

---

## Version 0.4.3 - Tests et Debug [COMPLÉTÉ]

**Objectif** : Outils de test et validation.

### Fonctionnalités

- [x] Écran de debug avec simulation d'appels
- [x] Test des décisions de filtrage
- [x] Tests rapides (inconnu, masqué, urgence, démarcheur)
- [x] Test SMS (vérification des conditions)
- [x] Envoi SMS de test réel
- [x] Tests unitaires pour use cases

### Tests unitaires

- [x] DecideCallActionUseCaseTest (20+ tests)
- [x] ShouldSendSmsUseCaseTest (15+ tests)

---

## Version 0.5.0 - UI Complète (Milestone 5) [EN COURS]

**Objectif** : Interface utilisateur aboutie.

### Fonctionnalités

- [x] Design Material 3 complet
- [ ] Thème clair / sombre
- [x] Écran d'accueil avec statistiques
- [x] Historique avec affichage par type
- [x] Paramètres complets
- [ ] Onboarding (première utilisation)
- [ ] Animations et transitions
- [ ] Accessibilité (TalkBack, grands textes)

### Critères de validation

- [ ] L'app est intuitive et agréable à utiliser
- [ ] Tous les écrans sont responsive
- [ ] L'accessibilité est validée

---

## Version 0.6.0 - Notifications Avancées (Milestone 6)

**Objectif** : Système de notifications intelligent.

### Fonctionnalités

- [ ] Canaux de notification (spam, inconnu, SMS)
- [ ] Regroupement des notifications
- [x] Actions rapides depuis notification (partiel)
- [ ] Résumé quotidien (optionnel)
- [ ] Widget home screen

### Critères de validation

- [ ] Les notifications sont non intrusives mais informatives
- [ ] L'utilisateur peut agir directement depuis la notification

---

## Version 1.0.0 - Release (Milestone 7)

**Objectif** : Version stable prête pour le Play Store.

### Fonctionnalités

- [x] Tests unitaires complets (use cases)
- [ ] Tests d'intégration
- [ ] Tests UI
- [ ] Performance optimisée
- [ ] ProGuard / R8 configuration
- [ ] Privacy Policy
- [ ] Déclarations Play Store
- [ ] Screenshots et assets marketing
- [x] Documentation utilisateur (README)

### Critères de validation

- [ ] Tous les tests passent
- [ ] Pas de crash en production (Firebase Crashlytics)
- [ ] Conforme aux guidelines Play Store

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
| 0.1.0 MVP | Complété | 100% |
| 0.2.0 Base Spam | Complété | 90% |
| 0.3.0 Allowlist/Blocklist | Complété | 80% |
| 0.4.0 SMS Auto | Complété | 100% |
| 0.4.1 Démarcheurs ARCEP | Complété | 100% |
| 0.4.2 Numéros masqués | Complété | 100% |
| 0.4.3 Tests/Debug | Complété | 100% |
| 0.5.0 UI Complète | En cours | 60% |
| 0.6.0 Notifications | Planifié | 20% |
| 1.0.0 Release | Planifié | 40% |

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
