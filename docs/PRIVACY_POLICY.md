# Privacy Policy - LacheMoiLaGrappe 🍇

**Dernière mise à jour : 6 mars 2026**

L'application **LacheMoiLaGrappe** a été conçue pour protéger votre vie privée. Nous croyons fermement que vos données téléphoniques ne regardent que vous.

## 1. Collecte des données
**Nous ne collectons aucune donnée personnelle.** L'application fonctionne de manière totalement locale sur votre appareil.
- **Historique d'appels** : L'historique des appels filtrés est stocké localement dans une base de données chiffrée sur votre téléphone.
- **Contacts** : L'accès à vos contacts est utilisé uniquement pour identifier les numéros connus et les laisser passer. Vos contacts ne sont jamais téléchargés, partagés ou vendus.
- **SMS** : L'accès aux SMS est utilisé uniquement pour envoyer des messages d'identification aux numéros inconnus (si activé) et pour analyser localement le phishing.

## 2. Partage des données
**Aucune donnée ne quitte votre appareil.** 
- LacheMoiLaGrappe n'utilise aucun serveur externe pour traiter vos appels ou vos messages.
- L'application ne contient aucun outil de suivi (tracking) ou de publicité.

## 3. Sécurité
Vos données locales (historique et listes de blocage) sont protégées par :
- Un chiffrement AES-256 (via SQLCipher).
- Une gestion des clés sécurisée via le Android Keystore matériel.

## 4. Permissions
L'application demande les permissions suivantes, uniquement pour son fonctionnement de base :
- `READ_CONTACTS` : Pour identifier vos proches.
- `ANSWER_PHONE_CALLS` / `READ_PHONE_STATE` : Pour filtrer les appels entrants.
- `SEND_SMS` : Pour envoyer les messages d'identification (optionnel).
- `RECEIVE_SMS` : Pour la protection contre le phishing (optionnel).

## 5. Contact
Pour toute question concernant cette politique, vous pouvez consulter le code source sur GitHub : [https://github.com/yrbane/LacheMoiLaGrappe](https://github.com/yrbane/LacheMoiLaGrappe)
