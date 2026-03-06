# Plan d'Implémentation - LacheMoiLaGrappe 🍇

## Vue d'ensemble

Ce document détaille l'architecture et les phases techniques de **LacheMoiLaGrappe**.

---

## Phase 8 : v1.1.0 - Phishing, Stats & i18n [COMPLETE]

- [x] **Détection Phishing SMS** : Implémentation de `AnalyzeSmsContentUseCase` et `SmsReceiver`.
- [x] **Persistance** : Migration Room v3 avec table `phishing_sms`.
- [x] **UI History** : Ajout d'onglets pour séparer Appels et SMS.
- [x] **UI Home** : Composant `StatsChart` pour visualisation sur 7 jours.
- [x] **Système** : `LacheMoiLaGrappeTileService` pour la tuile de réglages rapides.
- [x] **i18n** : Extraction des strings et traduction EN.

---

## Phase 9 : Sécurité & Accessibilité [COMPLETE]

- [x] **Audit de Sécurité** : Chiffrement SQLCipher validé.
- [x] **Gestion des Clés** : Keystore Android validé.
- [x] **Accessibilité** : Ajout des rôles sémantiques et descriptions TalkBack sur tous les écrans.
- [x] **Privacy Policy** : Rédaction conforme "Zero Data Collection".

---

## Phase 10 : Tests & Qualité [EN COURS]

### 10.1 Tests unitaires [COMPLETE]
- [x] `DecideCallActionUseCaseTest`
- [x] `ShouldSendSmsUseCaseTest`
- [x] `AnalyzeSmsContentUseCaseTest`
- [x] `LogCallEventUseCaseTest`

### 10.2 Tests d'intégration [COMPLETE]
- [x] `DecideCallActionIntegrationTest` (validation des priorités de filtrage).

### 10.3 Tests UI [PARTIEL]
- [x] Refactoring `HomeScreen` pour testabilité.
- [x] Premier test instrumenté `HomeScreenTest`.
- [ ] Tests de navigation complets.

---

## Phase 11 : v1.2.0 - Sérénité Familiale [COMPLETE]

- [x] **Mode Urgence Intelligent** : Implémentation du compteur de tentatives (3 appels en 5 min).
- [x] **Whitelist Services Publics** : Numéros d'urgence et administrations (SAMU, Police, Ameli...).
- [x] **Wizard de Configuration** : Refonte de l'onboarding vers un assistant simplifié (Questions Oui/Non).
- [x] **Mode Sommeil** : Désactivation automatique du filtrage sur une plage horaire configurable.
- [x] **Tests d'Intégration** : Validation de la logique `DecideCallActionUseCase`.

---

## Prochaine Étape : Phase 12 - v1.3.0 Intelligence & Automatisme

*LacheMoiLaGrappe - Sécurité, Transparence, Simplicité.*
