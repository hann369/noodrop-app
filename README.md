# Noodrop Android — Firebase MVP

**Kotlin · Jetpack Compose · Firebase Auth + Firestore · Hilt · Vico Charts**

---

## Schnellstart

### 1. Android Studio öffnen
`File → Open → noodrop-firebase/`
Android Studio Ladybug (2024.2+) oder neuer empfohlen.

### 2. google-services.json hinzufügen  ← PFLICHT
```
app/google-services.json
```
Download: Firebase Console → Projektübersicht → ⚙️ Einstellungen → Eigene Apps → Android-App hinzufügen  
Package Name: `com.noodrop.app`

### 3. Clash Display Fonts hinzufügen  ← PFLICHT
Erstelle `app/src/main/res/font/` und lege dort folgende Dateien ab:
```
clashdisplay_regular.ttf
clashdisplay_medium.ttf
clashdisplay_semibold.ttf
clashdisplay_bold.ttf
```
Download: https://www.fontshare.com/fonts/clash-display  
*(Wenn die Fonts fehlen, einfach alle `R.font.*`-Referenzen in `Theme.kt` durch `FontFamily.Default` ersetzen – die App baut dann trotzdem.)*

### 4. Firebase Firestore aktivieren
Firebase Console → Firestore Database → Erstellen → Testmodus (für Entwicklung)

### 5. Firestore Security Rules (Produktion)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 6. App bauen
```bash
./gradlew assembleDebug
```
Oder direkt in Android Studio ▶️ ausführen.

---

## Architektur

```
com.noodrop.app
├── data
│   ├── firebase/
│   │   └── FirebaseDataSource.kt   ← Alle Firestore-Operationen + Auth-Listener
│   ├── model/
│   │   ├── Models.kt               ← Domain-Modelle (Compound, DayLog, Protocol …)
│   │   └── StaticData.kt           ← 20 Compounds + 6 Protokolle (hardcoded)
│   ├── repository/
│   │   └── NoodropRepository.kt    ← Single Source of Truth
│   └── ThemePreferences.kt         ← Dark/Light via DataStore
├── di/
│   └── FirebaseModule.kt           ← Hilt: FirebaseAuth, Firestore (mit 100MB Cache)
└── ui
    ├── common/Components.kt        ← Shared Composables (NdCard, ChecklistRow …)
    ├── theme/Theme.kt              ← Farben, Typography, NoodropTheme
    ├── auth/                       ← Login / Sign Up
    ├── dashboard/                  ← Home + Streak + Heatmap
    ├── stack/                      ← Stack Builder + Timeline
    ├── tracker/                    ← Checklist + Metric-Sliders
    ├── metrics/                    ← Vico Charts + Log-Tabelle
    ├── library/                    ← 6 Protokolle + Detail-Sheet
    ├── Navigation.kt               ← Screen-Routen + Bottom-Nav-Items
    └── MainActivity.kt             ← Auth-Gate, TopBar, NavHost
```

## Tech Stack

| Layer        | Technologie                            |
|--------------|----------------------------------------|
| UI           | Jetpack Compose + Material 3           |
| Navigation   | Navigation Compose                     |
| State        | StateFlow + collectAsState             |
| DI           | Hilt (2.52)                            |
| Auth         | Firebase Auth (E-Mail + Passwort)      |
| Datenbank    | Firestore (offline persistence, 100MB) |
| Charts       | Vico 2.x (CartesianChart)              |
| Theme        | DataStore Preferences                  |
| Fonts        | Clash Display (fontshare.com)          |

## Firestore-Schema

```
users/{uid}/
  stack/{docId}        → { compoundName, dose, timing, sortOrder }
  logs/{dateString}    → { date, mood, fog, energy, focus, health, notes,
                           checkedCompounds[], stackSize }
  meta/notes           → { text }
  timeline/{docId}     → { dateLabel, text, timestampMs }
```

## Features (MVP)

| Feature | Beschreibung |
|---|---|
| **Auth** | Login + Sign Up (Email/Passwort), Auth-Gate vor dem App-Inhalt |
| **Dashboard** | Greeting, 4-Stat-Grid (Streak, Mood, Fog, Compounds), Protocol-Checklist (read-only), 7-Tage-Trend, 30-Tage-Heatmap |
| **Stack Builder** | Compound hinzufügen (20 zur Auswahl), entfernen, Presets laden, Notizen, Timeline |
| **Daily Tracker** | Checklist mit Echtzeit-Save, 5 Metrik-Slider (Mood, Fog, Energy, Health, Focus), Tagesnotizen, "Log Today" |
| **Metrics** | Zeitraum-Toggle 7/14/30 Tage, Ø-Werte, 3 Vico-Charts (Mood+Fog, Energy+Focus, Health-Bars), Log-Tabelle |
| **Protocol Library** | 6 Protokolle (Free/Paid/Coming Soon), Filter-Chips, Detail-Sheet mit "Load into Stack" |
| **Dark Mode** | Toggle im TopBar, persistiert via DataStore |

## Nächste Schritte

- [ ] Firestore Security Rules schärfen (Production)
- [ ] Google Sign-In ergänzen
- [ ] Push Notifications (tägliche Erinnerung via FCM)
- [ ] Onboarding Flow (3-Screen-Intro)
- [ ] In-App Purchase (RevenueCat) für Premium-Protokolle
- [ ] CSV-Export der Logs
- [ ] Widget (Checklist auf dem Homescreen)
