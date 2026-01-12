#CallFilter

Tu es un·e expert·e Android (Kotlin) et sécurité mobile. Conçois et implémente une application Android de filtrage d’appels, en respectant strictement les contraintes Android récentes (API 24+), les permissions runtime, et les règles Google Play (notamment sur l’usage de l’accessibilité, SMS, call screening, données personnelles). 

Objectif
- Filtrer (rejeter automatiquement) tous les appels entrants dont le numéro n’est PAS enregistré dans les contacts du téléphone.
- Consulter une base de données “spam / démarchage / emmerdeurs” pour tagguer l’appel (catégorie, score, source, date de mise à jour) et appliquer une action plus stricte (rejet + journalisation + notification).
- Si l’appelant inconnu semble être un numéro mobile, envoyer automatiquement un SMS poli demandant :
  1) l’identité (nom/prénom/entreprise),
  2) l’objet de l’appel,
  3) et de partager ses coordonnées pour ajout au carnet d’adresses.
- L’utilisateur doit pouvoir activer/désactiver chaque comportement, éditer les modèles de SMS, et consulter l’historique.

Contraintes & conformité (à appliquer)
- Utiliser Kotlin + Jetpack (Room, ViewModel, Flow, Hilt/Koin, WorkManager).
- Utiliser CallScreeningService (Telecom) pour filtrer et rejeter les appels (Android 7+ / API 24+).
- Ne pas utiliser d’astuces non conformes (accessibility abuse, services permanents non justifiés).
- Les SMS automatiques doivent être soumis à opt-in explicite et permission SEND_SMS. Prévoir un mode “validation avant envoi” si nécessaire pour rester conforme (et mentionner clairement les limites Android/Play).
- Respect vie privée : tout est local par défaut. Si une BD spam distante est proposée, expliquer la collecte minimale, la sécurité (HTTPS, pinning optionnel), et le consentement.
- Gérer les permissions avec explications UX (READ_CONTACTS, READ_PHONE_STATE si nécessaire, SEND_SMS, POST_NOTIFICATIONS, etc.).
- Mentionner les limites : détection “mobile vs fixe” n’est pas fiable à 100% sans service externe (HLR). Proposer :
  - Option A : heuristique via libphonenumber + préfixes nationaux
  - Option B : API externe (configurable), désactivée par défaut

Fonctionnalités détaillées
1) Filtrage appels
- Si numéro présent dans Contacts : laisser passer (allow).
- Sinon : rejeter l’appel automatiquement.
- Journaliser l’évènement (timestamp, numéro, décision, raison : “unknown contact”, “spam DB”, “blocked list”, etc.).

2) Base “spam”
- Supporter une base locale (Room) + mise à jour (WorkManager) depuis une source configurable :
  - Format JSON signé ou checksum (au minimum SHA-256) + versioning.
  - Enrichir : tag (ex: “démarchage énergie”), score (0–100), source, lastSeen.
- Si numéro match “spam” : rejeter + notification “Spam détecté : {tag} (score {x})”.
- Prévoir un mécanisme d’override : allowlist et blocklist utilisateur (prioritaires).

3) SMS de demande d’identité
- Condition : appel rejeté car “unknown contact” ET (optionnel) “numéro mobile probable”.
- Envoyer un SMS avec un template personnalisable. Exemple par défaut :
  “Bonjour, je filtre les appels inconnus. Peux-tu me donner ton identité, l’objet de ton appel, et tes coordonnées pour que je te rappelle ? Merci.”
- Anti-abus :
  - 1 SMS max par numéro / 24h (configurable).
  - Ne pas envoyer si numéro masqué/invalide.
  - Respecter une liste d’exclusion (numéros courts, urgences, services).
- Logguer l’envoi (status envoyé/échoué).

4) UI / UX
- Écran principal : toggles
  - Filtrer inconnus (ON/OFF)
  - Rejeter inconnus (ON/OFF)
  - Activer base spam (ON/OFF)
  - Envoyer SMS auto (OFF par défaut) + “demander confirmation avant envoi”
- Écran Historique : liste des appels filtrés + tags + action (autoriser/bloquer/ajouter contact).
- Écran Spam DB : source, dernière MAJ, importer/exporter, vider cache, statistiques.
- Écran SMS : template, cooldown, mode confirmation, aperçu.
- Notifications : résumé des appels rejetés (avec regroupement).

Architecture attendue (SOLID)
- Domain layer : use-cases (DecideCallAction, LookupSpam, ShouldSendSms, SendIdentitySms, LogEvent).
- Data layer : repositories (ContactsRepository, SpamRepository, SettingsRepository, CallLogRepository, SmsRepository).
- Android layer : CallScreeningService, WorkManager, UI Compose/XML.
- Dépendre d’abstractions (interfaces) + injection DI.

Livrables demandés dans ta réponse
- Un plan d’implémentation clair (étapes).
- La structure des packages.
- Le Manifest + déclaration CallScreeningService + permissions.
- Le modèle Room (entités + DAO).
- Le code Kotlin essentiel (squelettes complets) :
  - CallScreeningService qui lit Contacts + Spam + règles + renvoie CallResponse
  - Repository contacts (lookup numéro)
  - Spam lookup + mise à jour WorkManager
  - Service d’envoi SMS (avec rate limit)
  - Écran settings + historique (Compose recommandé)
- Une section “sécurité & conformité” : risques, limitations Android/Play, et mitigations.
- Une section “tests” : unit tests (use-cases), tests instrumentation (service), faux numéros.

Hypothèses
- Pays par défaut : France (E.164 +33). Utiliser libphonenumber pour normalisation.
- Android minSdk 24, targetSdk le plus récent.
- Ne pas exiger d’être l’appli Téléphone par défaut : s’appuyer sur CallScreeningService + role si nécessaire et expliquer.

Important
- Reste poli dans les SMS, pas de message agressif.
- Ne pas “doxxer” ni partager les numéros. Données locales et chiffrables (optionnel).
- Explique les cas limites (numéro masqué, VoIP, doubles SIM, appels Wi-Fi).

Commence par proposer l’architecture, puis le code, puis la checklist conformité.
