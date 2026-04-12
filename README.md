# TrackFit (GymTracker)

Application Android pour suivre des séances de musculation : modèles d’entraînement, bibliothèque d’exercices, enregistrement des séances, statistiques et chronomètre de repos entre les séries. Les données sont stockées localement sur l’appareil.

## Prérequis

- [Android Studio](https://developer.android.com/studio) (version récente, compatible AGP **8.7**)
- **JDK 17**
- Un appareil ou émulateur **API 26+** (Android 8.0)

## Technique

| Élément | Détail |
|--------|--------|
| Langage | Kotlin **2.0** |
| UI | Jetpack **Compose**, Material 3 |
| Navigation | Navigation Compose |
| Persistance | **Room** (KSP) |
| `applicationId` | `com.sport.gymtracker` |
| `minSdk` / `compileSdk` | **26** / **35** |

## Lancer le projet

1. Cloner le dépôt et ouvrir le dossier racine dans Android Studio.
2. Laisser Gradle synchroniser les dépendances.
3. Choisir le module **app** et exécuter sur un appareil ou un AVD (**Run**).

En ligne de commande (Windows / macOS / Linux) :

```bash
./gradlew :app:assembleDebug
```

L’APK de debug est généré sous `app/build/outputs/apk/debug/`.

## Structure (aperçu)

- `app/src/main/java/com/sport/gymtracker/` — code source (UI, navigation, données, domaine)
- `app/src/main/res/` — ressources Android (thème, chaînes, drawables)
