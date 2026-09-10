# Universe Society Fund Collector

Android app (Kotlin + Jetpack Compose) for collecting the annual society fund
for **Universe, Life Republic Sector R10** — Wing E, 240 flats loaded from
`app/src/main/assets/flats.json` (digitised from your notice board photo).

## What's built

**Screen 1 — Flats (Home)**
Grid of flat-number buttons grouped by floor, colour-coded green (collected)
/ orange (pending), with a search box and live Collected/Pending/Total
counters at the top. Tap a flat to go to Screen 2.

**Screen 2 — Receipt entry**
Shows the flat number + owner name (auto-filled from the directory), and a
form matching your printed receipt book: amount, "on account of", payment
mode (Cash/Cheque/NEFT/Mygate — with bank/branch/cheque no. fields that
appear only when relevant), and date. Saving assigns the next receipt
number automatically (continuing from 2477, matching Rec. No. 2478 on your
physical book) and shows a receipt-style confirmation you can screenshot for
the resident.

**Screen 3 — Reports**
Full, searchable/filterable list of every receipt (by flat number, owner
name, receipt number, or date), with a running total for whatever's
currently filtered.

All receipts are stored in **Firebase Firestore**, so every phone running
the app with the same Firebase project sees the same live data — as soon as
one committee member records a receipt, it appears for everyone else too.

## One-time setup (you'll need to do this before it builds)

1. **Install Android Studio** (Hedgehog/2023.1.1 or newer), and open this
   folder as a project (`File > Open`).

2. **Create a free Firebase project**: go to
   [console.firebase.google.com](https://console.firebase.google.com) →
   Add project.

3. **Register the Android app** inside that Firebase project:
   - Package name: `com.universe.societyfund` (must match exactly)
   - Download the generated `google-services.json` file
   - Place it at `app/google-services.json` in this project (same folder as
     `app/build.gradle.kts`)

4. **Enable Firestore**: in the Firebase console, go to
   *Build > Firestore Database > Create database*, start in **production
   mode**, pick a region close to you (e.g. `asia-south1` for Pune/Mumbai).

5. **Set Firestore security rules** so committee phones can read/write but
   randoms on the internet can't. A simple starting point (Firestore
   console → Rules tab):
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if true; // tighten this once you add sign-in
       }
     }
   }
   ```
   This is open to anyone with your Firebase config, which is fine while
   testing. Before real collection season, add Firebase Authentication
   (e.g. phone-number sign-in for the 2-3 committee members) and change the
   rule to `allow read, write: if request.auth != null;`.

6. **Sync Gradle** (Android Studio will prompt you), then **Run** on a
   device/emulator.

## Editing the flat directory

Open `app/src/main/assets/flats.json` — it's a plain list of
`{"number": "705", "wing": "E", "name": "GAURI PAWAR-SHINDE"}` objects. Add,
remove, or correct entries there (a few names on the photographed board were
blank or partly unclear — please double check those) and rebuild; no other
code changes are needed. If you have more wings, add their flats to the
same file with the correct `"wing"` value and they'll show up automatically.

## Adjusting the starting receipt number

The app starts numbering from **2478** to match your physical receipt book.
If your book has moved past that by the time you deploy this, open Firestore
Database in the console and manually create/edit:
`counters/receiptCounter` → field `lastNumber` (number) → set it to
*(last number already used)*.

## Project structure

```
app/src/main/java/com/universe/societyfund/
├── MainActivity.kt              # entry point
├── SocietyFundApp.kt            # Firebase init
├── model/                       # Flat.kt, Receipt.kt
├── data/                        # FlatRepository (assets), ReceiptRepository (Firestore)
├── viewmodel/SocietyViewModel.kt
└── ui/
    ├── home/HomeScreen.kt       # Screen 1 - flat grid
    ├── receipt/                 # Screen 2 - receipt form + preview dialog
    ├── reports/ReportsScreen.kt # Screen 3 - reports
    ├── navigation/AppNavigation.kt
    └── theme/
```

## Notes / next steps you may want

- **Authentication**: currently anyone with the app can enter receipts as
  "Committee" — add Firebase Auth if you want to track who entered what.
- **PDF/print receipts**: the confirmation dialog shows the receipt on
  screen; wiring up an actual PDF export or share-as-image is a natural
  next step if you want to send residents a shareable file.
- **Export reports**: right now Reports is in-app only; a "download as
  Excel/CSV" button can be added on top of the same data.
