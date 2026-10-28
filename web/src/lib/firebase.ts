import { initializeApp, getApps, getApp, FirebaseApp } from 'firebase/app';
import { 
  getAuth, 
  GoogleAuthProvider, 
  setPersistence, 
  browserLocalPersistence,
  onIdTokenChanged,
  getIdToken,
  getIdTokenResult,
  User,
  Auth,
  ParsedToken
} from 'firebase/auth';
import { 
  getFirestore, 
  initializeFirestore, 
  persistentLocalCache, 
  persistentMultipleTabManager,
  Firestore 
} from 'firebase/firestore';

/**
 * Authoritative Firebase Project ID matching the Amal Tracker platform.
 * Synchronized with the Android application and unified Firestore schema.
 */
export const FIREBASE_PROJECT_ID = 
  process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID || "amal-tracker-prod";

export const FIREBASE_SENDER_ID = 
  process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID || "619784299024";

export const firebaseConfig = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY || "AIzaSyDummyKeyForAmalTrackerWeb2026",
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN || `${FIREBASE_PROJECT_ID}.firebaseapp.com`,
  projectId: FIREBASE_PROJECT_ID,
  storageBucket: process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET || `${FIREBASE_PROJECT_ID}.appspot.com`,
  messagingSenderId: FIREBASE_SENDER_ID,
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID || `1:${FIREBASE_SENDER_ID}:web:amaltracker98f7e2a4`,
};

// Singleton Firebase Application instance
const app: FirebaseApp = !getApps().length 
  ? initializeApp(firebaseConfig) 
  : getApp();

// Firebase Authentication Instance
export const auth: Auth = getAuth(app);

// Configure persistent browser session (survives tab closing & browser restarts)
if (typeof window !== 'undefined') {
  setPersistence(auth, browserLocalPersistence).catch((err) => {
    console.warn('[AmalTracker Auth] Local persistence warning:', err);
  });
}

// Google Auth Provider with account selector
export const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({
  prompt: 'select_account',
});

// Configure Firestore with multi-tab offline persistence matching Android's Room offline support
let firestoreInstance: Firestore;
if (typeof window !== 'undefined') {
  try {
    firestoreInstance = initializeFirestore(app, {
      localCache: persistentLocalCache({
        tabManager: persistentMultipleTabManager()
      })
    });
  } catch {
    firestoreInstance = getFirestore(app);
  }
} else {
  firestoreInstance = getFirestore(app);
}

export const db: Firestore = firestoreInstance;

// ============================================================================
// Session Token & Cross-Platform Auth Utilities
// ============================================================================

/**
 * Retrieves the current Firebase ID Token for the logged-in user.
 * Automatically refreshes expired tokens.
 *
 * @param forceRefresh - If true, bypasses local cache and requests a new token.
 * @returns JWT Bearer token string or null if unauthenticated.
 */
export async function getCurrentUserToken(forceRefresh = false): Promise<string | null> {
  const user = auth.currentUser;
  if (!user) return null;
  try {
    return await getIdToken(user, forceRefresh);
  } catch (error) {
    console.error('[AmalTracker Auth] Failed to retrieve ID token:', error);
    return null;
  }
}

/**
 * Convenience helper to build Authorization headers for authenticated API requests
 * (e.g. Google Sheets synchronization backend, Super Admin verification, Cloud Functions).
 */
export async function getAuthHeaders(forceRefresh = false): Promise<Record<string, string>> {
  const token = await getCurrentUserToken(forceRefresh);
  if (!token) return {};
  return {
    Authorization: `Bearer ${token}`,
    'X-Amal-Platform': 'Web',
    'X-Amal-Project-Id': FIREBASE_PROJECT_ID,
  };
}

/**
 * Listens for token refreshes or user sign-in/sign-out events across tabs.
 * Allows components to reactively maintain synchronized auth state.
 */
export function onSessionTokenChanged(
  callback: (token: string | null, user: User | null) => void
) {
  return onIdTokenChanged(auth, async (currentUser) => {
    if (currentUser) {
      try {
        const token = await getIdToken(currentUser);
        callback(token, currentUser);
      } catch {
        callback(null, currentUser);
      }
    } else {
      callback(null, null);
    }
  });
}

/**
 * Fetches decoded claims from the current user's ID token.
 * Useful for inspecting admin privileges or custom claims.
 */
export async function getParsedTokenClaims(): Promise<ParsedToken | null> {
  const user = auth.currentUser;
  if (!user) return null;
  try {
    const result = await getIdTokenResult(user);
    return result.claims;
  } catch (error) {
    console.error('[AmalTracker Auth] Failed to parse token claims:', error);
    return null;
  }
}

/**
 * Quick helper to check if a valid session is currently active.
 */
export function isUserAuthenticated(): boolean {
  return auth.currentUser !== null;
}

export default app;
