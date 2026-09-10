# Universe Society Fund — Web Version

Same 3 screens as the Android app (Flats grid, Receipt entry, Reports), built
as a plain static site (HTML/CSS/JS, no build step) so it can be hosted
directly on Netlify. It uses **Firebase Firestore** for storage — the same
backend the Android app uses — so receipts entered on the web and on phones
show up for everyone, live.

## Make the Save button actually save data

Right now `js/firebase-config.js` has placeholder values, so the app will
show a red "Backend not configured" banner and refuse to save. To fix that:

1. Go to your Firebase project (the same one from the Android app setup —
   [console.firebase.google.com](https://console.firebase.google.com)). If
   you haven't created one yet, create it there first.
2. Project settings (gear icon) → **Your apps** → **Add app** → choose the
   **Web** (`</>`) icon.
3. Register the app (any nickname, e.g. "Society Fund Web"). Firebase will
   show you a `firebaseConfig` object.
4. Copy those exact values into `js/firebase-config.js` in this folder,
   replacing the `YOUR_...` placeholders.
5. Make sure Firestore Database is enabled (Build → Firestore Database →
   Create database) — see the root README for the security rules to set.
6. Refresh the page. The red banner should disappear and Save will write a
   real document to your `receipts` collection.

No build/compile step is needed — this is plain HTML/CSS/JS loading the
Firebase SDK from Google's CDN, so it works as-is on Netlify, GitHub Pages,
or any static host.

## Files

```
html version/
├── index.html          # the 3 screens (markup only)
├── css/style.css        # visual styling
├── js/firebase-config.js  # <-- fill this in (see above)
├── js/app.js            # all app logic + Firestore read/write
└── assets/flats.json    # same flat/owner directory used by the Android app
```

Editing `assets/flats.json` updates the flat list here — keep it in sync
with `android-app/app/src/main/assets/flats.json` if you edit one.
