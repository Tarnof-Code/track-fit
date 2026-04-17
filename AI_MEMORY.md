# Mémoire projet — TrackFit (GymTracker)

Document de **contexte global** pour l’assistant (et les humains). À **relire en début de session** sur ce dépôt. Pour Cursor : référencer ce fichier (`@AI_MEMORY.md`) ou l’inclure dans les règles du projet si besoin.

---

## 1. Produit & objectif

- **Nom affiché** : TrackFit (`app_name` dans `res/values/strings.xml`).
- **But** : application de suivi d’entraînement (musculation / salle) **100 % locale** : séances, exercices, modèles, statistiques, chronomètre de repos, bibliothèque d’exercices (« blueprints »).
- **Public / langue** : UI en français (libellés métier, groupes musculaires, difficultés, etc. dans `domain/` et écrans).

---

## 2. Stack technique (référence rapide)

| Élément | Valeur |
|--------|--------|
| IDE cible | Android Studio, AGP **9.1.1** |
| Langage | Kotlin **2.2.10**, JVM **17** |
| Build | Gradle **9.3.1** (wrapper), **KSP** **2.3.6** |
| UI | Jetpack **Compose**, **Material 3** |
| Navigation | Navigation Compose (`AppNav.kt`) |
| Persistance | **Room** 2.8.4 (schéma DB **18**), génération via KSP |
| Package | `com.sport.gymtracker` |
| SDK | `minSdk` **26**, `compileSdk` / `targetSdk` **35** |

**Kotlin sur `:app`** : avec AGP **9+**, compilation Kotlin **intégrée** au plugin Android — ne pas appliquer `org.jetbrains.kotlin.android` ; cible JVM dans `android { kotlin { compilerOptions { jvmTarget… } } }` (`app/build.gradle.kts`). KSP **≥ 2.3.4** pour Room sans erreur `kotlin.sourceSets` / Kotlin intégré.

Fichiers racine utiles : `settings.gradle.kts` (projet **GymTracker**), `app/build.gradle.kts`, `gradle.properties`, `README.md`, `.gitignore`, `.cursorrules`, **`AI_MEMORY.md`** (ce fichier).

**Gradle (méta)** : `gradle.properties` inclut entre autres **`android.dependency.useConstraints=false`** (alignement AGP 9+ ; remplace d’anciennes options dépréciées liées aux contraintes). `.vscode/settings.json` peut fixer `java.configuration.updateBuildConfiguration` sur `automatic` (Cursor / VS Code).

---

## 3. Architecture du code

```
app/src/main/java/com/sport/gymtracker/
├── MainActivity.kt, GymTrackerApp.kt
├── ui/
│   ├── navigation/AppNav.kt      # NavHost, barre du bas, routes
│   ├── screens/                  # *Screen.kt (accueil, séances, détail, modèles, stats, backup, progression, bibliothèque, éditeurs)
│   ├── components/               # Cartes, graphes, timer de repos, sélecteurs, etc.
│   ├── theme/                    # Theme, couleurs, typo
│   └── viewmodel/                # ViewModels + factories
├── data/
│   ├── GymRepository.kt, StatisticsOverview.kt
│   ├── backup/ # Export / import JSON (GymDataJson, DataImport, matching blueprints)
│   └── local/                    # AppDatabase, entités, DAOs, migrations (voir ci‑dessous)
├── util/                         # ex. FrenchDateTime.kt
└── domain/
    ├── Models.kt, ExerciseWorkMode.kt, ExercisePrescriptionFormat.kt, LoadSpecParse.kt
    ├── ExerciseEditorSaveParams.kt
    └── OnAirLaDefenseCatalog.kt  # Catalogue d’équipements (contexte métier)
```

**Migrations Room** : historique principal dans `DatabaseMigrations.kt` (`MIGRATION_1_2` … `MIGRATION_13_14`) ; migrations récentes aussi dans `Migration14To15.kt` … `Migration17To18.kt` (enregistrées dans `AppDatabase`).

**Navigation principale** (barre du bas) : Accueil, **Séances**, **Modèles**, Stats, **Données** (sauvegarde / restauration).  
**Routes secondaires** (sans barre) : détail séance, édition exercice de séance, détail modèle, édition exercice de modèle, bibliothèque d’exercices, éditeur de blueprint, **progression par exercice** (liste + détail par blueprint).

**Données Room** (`AppDatabase`) : `WorkoutSessionEntity`, `ExerciseEntryEntity`, `WorkoutTemplateEntity`, `TemplateExerciseEntity`, `ExerciseBlueprintEntity` — version schéma **18**, chaîne **MIGRATION_1_2** … **MIGRATION_17_18** (ex. **16→17** : notes sur blueprint ; **17→18** : `completedSetsMask` sur les entrées d’exercice en séance).

**Note.** Le builder appelle encore `fallbackToDestructiveMigration()` : acceptable en dev, **à revoir avant une release grand public** (perte de données si migration manquante).

---

## 4. Contexte actif — déjà en place (code + dépôt)

### Application

- Parcours **accueil** → création / ouverture de **séance**, **liste des séances**, **détail séance** avec exercices, édition d’exercice, timer de repos entre séries, suivi des séries complétées (masque en base).
- **Modèles** d’entraînement : liste, détail, ajout / édition d’exercices de modèle.
- **Bibliothèque d’exercices** (blueprints) + écran d’édition blueprint (notes libres sur la fiche).
- **Statistiques** (écran dédié + logique `StatisticsOverview` / composants de graphiques).
- **Sauvegarde & restauration** : écran `BackupScreen` + JSON via `data/backup/` (export / import, alignement des blueprints).
- **Progression** : parcours liste / détail de progression lié aux blueprints (`ExerciseProgressListScreen`, `ExerciseProgressDetailScreen`).
- Thème Compose, splash, icônes / assets drawable.
- Domaine riche : groupes musculaires, prescription, parsing de charges, modes de travail, catalogue équipement.

### Dépôt & outillage (méta)

- `README.md` : description, prérequis, commande `assembleDebug`.
- `.gitignore` adapté Android / Kotlin / signing / IDE.
- `.cursorrules` : conventions pour l’assistant ; renvoie vers **`AI_MEMORY.md`** en début de session.
- `AI_MEMORY.md` : mémo architecture, fait / à faire (ce fichier).

---

## 5. Pistes « reste à faire » (non exhaustif — à prioriser)

À adapter selon la roadmap réelle ; utile pour ne pas repartir de zéro à chaque session :

| Zone | Idées |
|------|--------|
| **Qualité** | Tests unitaires (domain, repository), tests UI Compose sur flux critiques. |
| **Prod** | Retirer ou conditionner `fallbackToDestructiveMigration` ; pipeline release (ProGuard/R8 si besoin). |
| **i18n** | Externaliser toutes les chaînes vers `strings.xml` / ressources si des littéraux restent dans le code. |
| **Données** | Améliorer / durcir le flux import-export (validation, conflits, UX) ; sync cloud seulement si besoin futur — cœur toujours local. |
| **Store** | Fiche Play Console, politique de confidentialité, captures, signing `release`. |
| **Accessibilité** | Content descriptions, tailles tactiles, contrastes. |
| **CI** | Build Gradle sur push (GitHub Actions, etc.) si le dépôt devient distant. |

**Dernière mise à jour mémo** : 17 avril 2026 — stack ci‑dessus ; **Kotlin intégré** + KSP **2.3.6** ; `gradle.properties` (**`useConstraints=false`** et nettoyage options AGP dépréciées).

---

## 6. Comment utiliser ce fichier

1. **Humain** : après une grosse fonctionnalité ou décision d’architecture, mettre à jour les sections **4** et **5** en quelques lignes.
2. **Assistant** : en ouverture de tâche sur ce repo, prendre ce document comme **carte** ; en cas de doute, vérifier le fichier source cité (ex. `AppNav.kt`, `AppDatabase.kt`).

**Cursor.** Une règle **`.cursor/rules/project-memory.mdc`** avec `alwaysApply: true` rappelle de s’appuyer sur ce fichier ; on peut toujours citer explicitement `@AI_MEMORY.md` dans le chat si besoin.
