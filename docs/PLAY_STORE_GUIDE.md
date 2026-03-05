# Guide de publication - Google Play Store

## 1. Creer l'application

1. Va sur https://play.google.com/console
2. Clique **"Creer une application"**
3. Remplis :
   - **Nom** : LacheMoiLaGrappe
   - **Langue par defaut** : Francais
   - **Application ou jeu** : Application
   - **Gratuite ou payante** : Gratuite
4. Accepte les declarations et clique **"Creer l'application"**

## 2. Fiche Play Store

### Informations principales
- **Titre** : LacheMoiLaGrappe
- **Description courte** (80 car. max) :

```
Bloquez demarcheurs, spams et appels indesirables. Votre telephone, vos regles.
```

- **Description longue** (4000 car. max) :

```
Marre des appels a la con ?

Marre de ces types qui vous appellent en plein repas pour vous vendre une mutuelle dont vous n'avez pas besoin, une formation CPF bidon, ou des panneaux solaires alors que vous vivez en studio ?

C'est fini. LacheMoiLaGrappe est un bouclier anti-emmerdeurs pour votre telephone Android.

FILTRAGE INTELLIGENT :

- Blocage des demarcheurs ARCEP : Les 17 prefixes reserves au demarchage sont bloques automatiquement
- Filtrage des numeros inconnus : Pas dans vos contacts ? Pas de sonnerie
- Blocage des numeros masques : Ceux qui se cachent n'ont qu'a assumer
- Detection des spams : Base de donnees locale de numeros signales
- SMS automatique : Reponse polie aux inconnus pour demander leur identite (opt-in)
- Allowlist / Blocklist : Ecran dedie pour gerer vos listes
- Prefixes personnalises : Ajoutez vos propres prefixes a bloquer

UX SOIGNEE :

- Onboarding guide pour la premiere utilisation
- Historique avec recherche, swipe-to-action et export CSV
- Widget home screen pour suivre les appels bloques
- Notifications groupees et non intrusives
- Retour haptique sur les actions

RESPECT DE LA VIE PRIVEE :

- Zero collecte de donnees : tout reste sur votre telephone
- Pas de serveur, pas de tracking, pas de pub
- Open source : le code est public sur GitHub
- Gratuit : pas d'abonnement, pas de version premium

Parce que je prefere avoir au bout du fil les gens qui m'aiment moi, pas ceux qui aiment mon porte-monnaie.
```

### Elements graphiques

Tu as besoin de :
- **Icone** : 512x512 PNG (genere depuis Android Studio : Build > Generate Signed Bundle > ou exporte l'adaptive icon)
- **Feature graphic** : 1024x500 PNG (image de banniere)
- **Screenshots** : minimum 2 captures d'ecran telephone (1080x1920 ou similaire)

Pour generer les screenshots :
1. Lance l'app sur un emulateur
2. Capture l'ecran d'accueil, l'historique, les parametres
3. Ou utilise Android Studio > Device Manager > Screenshot

## 3. Categorisation

- **Categorie** : Outils
- **Classification du contenu** : Remplis le questionnaire IARC (prend 2 min, reponse "non" a tout)

## 4. Coordonnees

- **Email** : (ton email de contact)
- **Politique de confidentialite** : https://yrbane.github.io/LacheMoiLaGrappe/privacy.html

## 5. Section "Securite des donnees"

Reponds au questionnaire :
- **Collecte de donnees utilisateur** : Non
- **Partage de donnees** : Non
- **Donnees collectees** : Aucune transmise a des tiers
- **Pratiques de securite** : Donnees stockees localement, pas de chiffrement en transit (pas de transfert)

## 6. Declarations de permissions sensibles

### SEND_SMS
Google va demander une justification. Reponds :
```
L'application utilise SEND_SMS pour envoyer un SMS de reponse automatique aux appelants inconnus, uniquement si l'utilisateur active explicitement cette fonctionnalite (opt-in). Le SMS demande a l'appelant de s'identifier. L'utilisateur peut configurer un mode confirmation qui demande validation avant chaque envoi. Un cooldown empeche l'envoi repete au meme numero.
```

### CallScreeningService (BIND_SCREENING_SERVICE)
```
L'application est un filtreur d'appels qui utilise CallScreeningService pour intercepter et filtrer les appels entrants non souhaites (demarcheurs, spams, numeros inconnus). C'est la fonctionnalite principale de l'application.
```

### READ_CALL_LOG
```
L'application utilise READ_CALL_LOG pour acceder a l'historique des appels et enrichir les informations affichees dans l'historique de filtrage de l'application.
```

## 7. Upload de l'AAB

1. Va dans **Production** > **Creer une release**
2. **App signing by Google Play** : Active (recommande)
3. Upload le fichier : `app/build/outputs/bundle/release/app-release.aab`
4. **Nom de la release** : 1.0.0
5. **Notes de version** :
```
Fini les demarcheurs. Filtrage auto des appels indesirables, spams et numeros masques. Votre diner est sauve.
```
6. Clique **"Examiner la release"** puis **"Lancer le deploiement en production"**

## 8. Soumission

- La review Google prend generalement **1 a 7 jours** pour une premiere soumission
- Possible rejet initial a cause des permissions sensibles (SEND_SMS, CallScreeningService)
- Si rejete : lis le motif, ajuste la justification, et re-soumets

## Checklist finale

- [ ] Compte dev Google Play actif
- [ ] Application creee sur la console
- [ ] Fiche Store remplie (titre, descriptions, screenshots)
- [ ] Icone 512x512 uploadee
- [ ] Feature graphic 1024x500 uploadee
- [ ] Min 2 screenshots telephone uploades
- [ ] Categorie : Outils
- [ ] Classification IARC remplie
- [ ] Email de contact renseigne
- [ ] URL privacy policy renseignee
- [ ] Securite des donnees remplie
- [ ] Justification SEND_SMS redigee
- [ ] Justification CallScreeningService redigee
- [ ] AAB uploade
- [ ] Notes de version redigees
- [ ] Release soumise pour review
