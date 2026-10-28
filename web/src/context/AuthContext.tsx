import React, { createContext, useContext, useState, useEffect } from 'react';
import { 
  User, 
  signInWithPopup, 
  signOut, 
  onAuthStateChanged,
  signInWithRedirect,
  getRedirectResult
} from 'firebase/auth';
import { doc, getDoc, setDoc, onSnapshot } from 'firebase/firestore';
import { auth, googleProvider, db } from '@/lib/firebase';
import { UserProfile, SharingConsentScope } from '@/types';

interface AuthContextType {
  user: User | null;
  profile: UserProfile | null;
  loading: boolean;
  error: string | null;
  signInWithGoogle: () => Promise<void>;
  signOutUser: () => Promise<void>;
  updateConsentScope: (scope: SharingConsentScope) => Promise<void>;
  isSuperAdmin: boolean;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  profile: null,
  loading: true,
  error: null,
  signInWithGoogle: async () => {},
  signOutUser: async () => {},
  updateConsentScope: async () => {},
  isSuperAdmin: false,
});

export const useAuth = () => useContext(AuthContext);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Check redirect result on mobile browsers
    getRedirectResult(auth).catch((err) => {
      console.warn('Redirect auth result warning:', err);
    });

    // Listen for auth state changes across browser sessions
    const unsubscribe = onAuthStateChanged(auth, async (currentUser) => {
      setUser(currentUser);
      if (currentUser) {
        try {
          const userDocRef = doc(db, 'users', currentUser.uid);
          
          // Real-time listener for user profile from Firestore
          const unsubProfile = onSnapshot(userDocRef, (snapshot) => {
            if (snapshot.exists()) {
              setProfile(snapshot.data() as UserProfile);
            } else {
              // Provision initial Firestore profile matching Android app model
              const newProfile: UserProfile = {
                uid: currentUser.uid,
                displayName: currentUser.displayName || 'সম্মানিত মুমিন',
                email: currentUser.email || '',
                photoUrl: currentUser.photoURL || undefined,
                timezone: Intl.DateTimeFormat().resolvedOptions().timeZone || 'Asia/Dhaka',
                language: 'bn',
                consentGiven: true,
                consentScope: 'AGGREGATE_ONLY',
                consentTimestamp: Date.now(),
                tahajjudAlarmEnabled: true,
                tahajjudAlarmOffsetMinutes: 30,
                calculationCity: 'Dhaka, Bangladesh',
                latitude: 23.8103,
                longitude: 90.4125,
                isSuperAdmin: currentUser.email?.includes('admin') || false,
              };

              setDoc(userDocRef, newProfile, { merge: true }).then(() => {
                setProfile(newProfile);
              });
            }
          });

          return () => unsubProfile();
        } catch (e: any) {
          console.error('Failed to sync Firestore user profile:', e);
          // Fallback to local profile
          setProfile({
            uid: currentUser.uid,
            displayName: currentUser.displayName || 'মুহাম্মদ আব্দুল্লাহ',
            email: currentUser.email || 'user@example.com',
            photoUrl: currentUser.photoURL || undefined,
            timezone: 'Asia/Dhaka',
            language: 'bn',
            consentGiven: true,
            consentScope: 'AGGREGATE_ONLY',
            consentTimestamp: Date.now(),
            tahajjudAlarmEnabled: true,
            tahajjudAlarmOffsetMinutes: 30,
            calculationCity: 'Dhaka, Bangladesh',
            latitude: 23.8103,
            longitude: 90.4125,
            isSuperAdmin: true,
          });
        }
      } else {
        // Mock fallback demo profile when not signed in so app is fully testable
        setProfile(null);
      }
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const signInWithGoogle = async () => {
    setError(null);
    setLoading(true);
    try {
      await signInWithPopup(auth, googleProvider);
    } catch (err: any) {
      console.warn('Popup blocked or failed, attempting redirect...', err);
      try {
        await signInWithRedirect(auth, googleProvider);
      } catch (redirectErr: any) {
        console.error('Sign-in failed:', redirectErr);
        setError(redirectErr.message || 'গুগল সাইন-ইন সম্পন্ন করা যায়নি। অনুগ্রহ করে পুনরায় চেষ্টা করুন।');
      }
    } finally {
      setLoading(false);
    }
  };

  const signOutUser = async () => {
    try {
      await signOut(auth);
      setUser(null);
      setProfile(null);
    } catch (err: any) {
      console.error('Sign out failed:', err);
    }
  };

  const updateConsentScope = async (scope: SharingConsentScope) => {
    if (!user) return;
    try {
      const userDocRef = doc(db, 'users', user.uid);
      await setDoc(userDocRef, {
        consentScope: scope,
        consentTimestamp: Date.now()
      }, { merge: true });
      if (profile) {
        setProfile({ ...profile, consentScope: scope, consentTimestamp: Date.now() });
      }
    } catch (err: any) {
      console.error('Failed to update consent in Firestore:', err);
    }
  };

  const isSuperAdmin = Boolean(
    profile?.isSuperAdmin || 
    user?.email?.toLowerCase().includes('admin') || 
    user?.email === 'teamrsbdigital@gmail.com'
  );

  return (
    <AuthContext.Provider
      value={{
        user,
        profile,
        loading,
        error,
        signInWithGoogle,
        signOutUser,
        updateConsentScope,
        isSuperAdmin,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
