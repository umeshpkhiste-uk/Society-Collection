# Society Collection — Universe (Wing E)

Annual society fund collection app for Universe, Life Republic Sector R10,
built two ways from the same data and the same backend:

- **`android-app/`** — native Kotlin + Jetpack Compose Android app
- **`html version/`** — static HTML/CSS/JS web app (deployable to Netlify)

Both clients read the same flat/owner directory (digitised from the Wing-E
notice board) and write to the same **Firebase Firestore** database, so a
receipt entered on a phone or in a browser shows up live everywhere else.

## Backend setup (do this once, before either app can save data)

1. Create a Firebase project at
   [console.firebase.google.com](https://console.firebase.google.com).
2. Enable **Firestore Database** (Build → Firestore Database → Create
   database → production mode → pick a nearby region).
3. Set security rules for now (tighten later with Firebase Auth):
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if true;
       }
     }
   }
   ```
4. Register **both** a Web app and an Android app (package name
   `com.universe.societyfund`) inside that same Firebase project:
   - Android → download `google-services.json` → place at
     `android-app/app/google-services.json`
   - Web → copy the `firebaseConfig` values → paste into
     `html version/js/firebase-config.js`
5. That's it — the receipt number counter and every receipt is shared
   automatically between both apps from here.

See `android-app/README.md` and `html version/README.md` for the
per-app details.

## Deploying the web app to Netlify

This repo includes a `netlify.toml` that publishes the `html version`
folder as-is (no build step needed — it's plain HTML/CSS/JS).

1. In your [Netlify dashboard](https://app.netlify.com), click
   **Add new site → Import an existing project**.
2. Connect it to GitHub and pick this repository
   (`umeshpkhiste-uk/Society-Collection`).
3. Netlify will read `netlify.toml` automatically — publish directory
   `html version`, no build command. Click **Deploy**.
4. Every future push to this repo's default branch will auto-redeploy.

(I can't reach netlify.com from this sandbox to trigger a deploy directly —
this Git-connected import is also the standard/recommended way to run
Netlify anyway, since it gives you auto-deploys on every push for free.)

## Repo structure

```
Society-Collection/
├── android-app/        # Kotlin/Compose Android project
├── html version/       # static web app (Netlify-ready)
├── netlify.toml
└── README.md            # this file
```
