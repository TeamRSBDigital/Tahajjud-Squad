# Amal Tracker / Tahajjud Squad — Web Application

A responsive web companion to the native Android **Amal Tracker** app, built with Next.js, React 18, Tailwind CSS, TypeScript, and Firebase.

---

## 🕌 Architecture & Capabilities

1. **Shared Authoritative Backend**:
   - Both Android and Web apps connect to the same Firebase Authentication and Cloud Firestore schema.
   - Real-time updates reflect prayers, Tahajjud streaks, and daily Amal across all devices.

2. **Tahajjud Squad Community**:
   - Private and invite-based squads (`inviteCode`).
   - Granular privacy options:
     - `PERSONAL_ONLY`: Device-only records, not visible to squads or admins.
     - `AGGREGATE_ONLY` *(Recommended)*: Only aggregate completion percentages are shared.
     - `DETAILED`: Full prayer statuses shared with consented squads/admins (personal notes are strictly excluded).

3. **Super Admin Monitoring & Google Sheets Integration**:
   - Consented users directory with search and status badges.
   - Batch webhook synchronization with `AmalTracker_AppsScript.js` to populate centralized Google Sheets.
   - Comprehensive administrative audit trail.

---

## 🛠️ Local Development & Build

### Prerequisites
- Node.js 18+ or 20+
- npm or yarn

### Setup Instructions
```bash
# Navigate to web directory
cd web

# Install dependencies
npm install

# Run local development server
npm run dev
```
Open [http://localhost:3000](http://localhost:3000) in your browser.

### Production Build
```bash
# Build the production Next.js bundle
npm run build

# Start production server
npm start
```

---

## 🚀 Deployment

### Option A: Firebase Hosting
```bash
# Initialize Firebase Hosting in the root directory
firebase init hosting

# Specify "web/out" (if using export) or configure Next.js SSR with Cloud Functions
firebase deploy --only hosting
```

### Option B: Vercel
1. Import the repository into Vercel.
2. Set root directory to `web`.
3. Provide environment variables for Firebase configuration.
4. Deploy with zero configuration.

---

## ⏰ Notification & Alarm Differences: Android vs Web

| Feature | Native Android (`Amal Tracker`) | Responsive Web (`/web`) |
| :--- | :--- | :--- |
| **Tahajjud Wakeup Alarm** | Uses `AlarmManager.setExactAndAllowWhileIdle()` to wake the device even in Doze mode or when screen is off. | Uses standard browser **Web Notifications API** (`Notification.requestPermission()`). Active only while browser tab or Service Worker is available. |
| **Local Persistence** | Offline-first with **Room Database** v2 (`prayers`, `tahajjud`, `amal`, `durable_sync_jobs`). | Uses browser **localStorage** and Firebase offline cache. |
| **Background Sync** | Android `WorkManager` & custom durable outbox queue with exponential backoff and retry scheduling. | Firebase real-time listeners and client-initiated webhook batch synchronization. |
| **Haptic Feedback** | Native Android `Vibrator` and `VibrationEffect` with click patterns. | Browser `navigator.vibrate([25])` when supported on mobile browsers. |
