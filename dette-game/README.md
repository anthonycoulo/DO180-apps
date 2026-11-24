# La Dette de la République — jeu de stratégie Android

Mini-jeu de stratégie tour par tour en Kotlin natif (Jetpack Compose) : vous êtes le
ministre de l'Économie d'un pays endetté. Chaque tour représente une année ; vous avez
**10 ans** pour ramener la **dette sous 100 % du PIB** sans que votre cote de popularité
ne tombe à 0 %.

## Règles

- État initial : dette 2 850 Md€, PIB 2 800 Md€ (ratio ≈ 101,8 %), déficit 5 % du PIB,
  croissance 1 %, taux d'intérêt moyen 2 %, cote 50 %.
- Chaque année, vous choisissez **une** décision :
  - **Austérité** : −4 pts de déficit, −0,6 pt de croissance, −4 pts de cote
  - **Relance** : +2 pts de déficit, +1,5 pt de croissance, +2 pts de cote
  - **Hausse des impôts** : −2,5 pts de déficit, −0,3 pt de croissance, −6 pts de cote
  - **Baisses d'impôts** : +1,5 pt de déficit, +0,7 pt de croissance, +6 pts de cote
  - **Emprunter sur les marchés** : +2 % du PIB de dette immédiate, hausse du taux
    (+0,3 pt si la dette dépasse 100 % du PIB), +1 pt de cote
  - **Attendre** : dérive des dépenses (+0,2 pt de déficit)
- Un événement aléatoire survient 30 % des années (crise, boom, pandémie, inflation…).
- La cote remonte avec la croissance (+2 pts par point de croissance).
- **Fin de partie** :
  - dette ≥ 150 % du PIB → défaut de paiement ;
  - cote ≤ 0 % → vous êtes renversé ;
  - après 10 ans → victoire si dette < 100 % du PIB et cote ≥ 25 %.
- Un score est calculé à la fin (ratio de dette, cote, croissance).

## Prérequis

- Android Studio Hedgehog (2023.1.1) ou ultérieur
- JDK 17
- Un émulateur Android (API 26+) ou un téléphone en mode développeur

## Lancer le jeu

1. Ouvrir le dossier `dette-game/` dans Android Studio (*File > Open*).
2. Si le wrapper Gradle manque (`gradle-wrapper.jar`), Android Studio le génère
   automatiquement, ou exécutez une fois `gradle wrapper` sur votre machine.
3. Laisser Gradle synchroniser, puis cliquer sur **Run** (▶) en visant un émulateur
   ou votre appareil.

## Commandes utiles

```bash
./gradlew testDebugUnitTest   # tests unitaires du moteur de jeu
./gradlew assembleDebug       # génère app/build/outputs/apk/debug/app-debug.apk
```

## Structure

```
dette-game/
├── app/src/main/java/com/do180/dettegame/
│   ├── MainActivity.kt            # point d'entrée
│   ├── game/
│   │   ├── Models.kt              # état du jeu, décisions, statuts
│   │   └── GameEngine.kt          # règles pures (sans dépendance Android, testable)
│   └── ui/
│       ├── GameScreen.kt          # écrans Compose
│       ├── GameViewModel.kt       # état exposé à l'UI
│       └── theme/Theme.kt
└── app/src/test/.../GameEngineTest.kt  # tests JUnit du moteur
```

Le moteur (`GameEngine`) est du Kotlin pur : les tests unitaires valident que
l'austérité réduit le déficit, que la dette croît avec les intérêts, qu'une stratégie
équilibrée peut gagner et que la relance permanente mène au défaut de paiement.
