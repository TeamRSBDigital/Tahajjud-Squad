'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { 
  signInWithPopup, 
  signInWithRedirect, 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword,
  onAuthStateChanged,
  User
} from 'firebase/auth';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { auth, googleProvider, db, FIREBASE_PROJECT_ID } from '@/lib/firebase';
import { 
  ShieldCheck, 
  Sparkles, 
  Moon, 
  Clock, 
  CheckCircle2, 
  AlertCircle, 
  ArrowLeft, 
  Lock, 
  Mail, 
  ChevronRight,
  Languages,
  Loader2
} from 'lucide-react';

export default function LoginPage() {
  const router = useRouter();
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [isNewUserMode, setIsNewUserMode] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [lang, setLang] = useState<'bn' | 'en'>('bn');

  // Monitor existing auth state
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      if (user) {
        setCurrentUser(user);
        try {
          const userDocRef = doc(db, 'users', user.uid);
          const snap = await getDoc(userDocRef);
          if (!snap.exists()) {
            await setDoc(userDocRef, {
              uid: user.uid,
              displayName: user.displayName || displayName || 'সম্মানিত মুমিন',
              email: user.email || '',
              photoUrl: user.photoURL || null,
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
              isSuperAdmin: user.email?.includes('admin') || user.email === 'teamrsbdigital@gmail.com' || false,
            }, { merge: true });
          }
        } catch (e) {
          console.warn('Firestore user profile sync notice:', e);
        }
      } else {
        setCurrentUser(null);
      }
    });

    return () => unsubscribe();
  }, [displayName]);

  // Handle Google Sign-In
  const handleGoogleSignIn = async () => {
    setLoading(true);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      const result = await signInWithPopup(auth, googleProvider);
      setSuccessMessage(
        lang === 'bn' 
          ? `স্বাগতম, ${result.user.displayName || 'সম্মানিত মুমিন'}! সফলভাবে লগইন সম্পন্ন হয়েছে।`
          : `Welcome, ${result.user.displayName || 'User'}! Successfully signed in.`
      );
      setTimeout(() => {
        if (typeof window !== 'undefined') {
          window.location.href = '/';
        }
      }, 1000);
    } catch (err: any) {
      console.warn('Popup blocked, attempting redirect fallback:', err);
      try {
        await signInWithRedirect(auth, googleProvider);
      } catch (redirectErr: any) {
        console.error('Google sign-in error:', redirectErr);
        setErrorMessage(
          redirectErr.message || (lang === 'bn' 
            ? 'গুগল সাইন-ইন প্রক্রিয়া সম্পন্ন করা যায়নি। অনুগ্রহ করে পুনরায় চেষ্টা করুন।' 
            : 'Failed to complete Google Sign-in. Please try again.')
        );
      }
    } finally {
      setLoading(false);
    }
  };

  // Handle Email & Password Sign-In or Registration
  const handleEmailAuth = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      setErrorMessage(lang === 'bn' ? 'ইমেইল ও পাসওয়ার্ড প্রদান করুন।' : 'Please enter email and password.');
      return;
    }
    if (password.length < 6) {
      setErrorMessage(lang === 'bn' ? 'পাসওয়ার্ড ন্যূনতম ৬ অক্ষরের হতে হবে।' : 'Password must be at least 6 characters.');
      return;
    }

    setLoading(true);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      if (isNewUserMode) {
        const res = await createUserWithEmailAndPassword(auth, email, password);
        const userDocRef = doc(db, 'users', res.user.uid);
        await setDoc(userDocRef, {
          uid: res.user.uid,
          displayName: displayName || (email.split('@')[0]),
          email: res.user.email,
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
          isSuperAdmin: email.includes('admin') || email === 'teamrsbdigital@gmail.com',
        }, { merge: true });

        setSuccessMessage(lang === 'bn' ? 'নতুন অ্যাকাউন্ট সফলভাবে তৈরি হয়েছে!' : 'Account created successfully!');
      } else {
        await signInWithEmailAndPassword(auth, email, password);
        setSuccessMessage(lang === 'bn' ? 'সফলভাবে লগইন হয়েছে!' : 'Signed in successfully!');
      }

      setTimeout(() => {
        if (typeof window !== 'undefined') {
          window.location.href = '/';
        }
      }, 1000);
    } catch (err: any) {
      console.error('Email auth error:', err);
      let msg = err.message;
      if (err.code === 'auth/user-not-found' || err.code === 'auth/wrong-password' || err.code === 'auth/invalid-credential') {
        msg = lang === 'bn' ? 'ভুল ইমেইল অথবা পাসওয়ার্ড দেওয়া হয়েছে।' : 'Invalid email or password.';
      } else if (err.code === 'auth/email-already-in-use') {
        msg = lang === 'bn' ? 'এই ইমেইলটি আগেই নিবন্ধিত হয়েছে। সাইন-ইন করুন।' : 'Email already registered. Please sign in.';
      }
      setErrorMessage(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F7F5EC] text-[#252B27] flex flex-col justify-between selection:bg-[#123C2D] selection:text-[#FFFDF5]">
      {/* Top Bar with Brand in Emerald Green #123C2D */}
      <header className="w-full bg-[#123C2D] border-b border-[#D4AF57]/40 px-4 py-3.5 shadow-md">
        <div className="max-w-5xl mx-auto flex items-center justify-between">
          <Link href="/" className="flex items-center space-x-3 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-[#0C291E] to-[#1A5641] border border-[#D4AF57] flex items-center justify-center text-[#D4AF57] font-bold text-xl shadow-lg group-hover:scale-105 transition-transform">
              ☪
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="text-lg font-bold text-[#FFFDF5] tracking-wide">Amal Tracker</span>
                <span className="text-[10px] bg-[#0C291E] text-[#E5C678] px-2 py-0.5 rounded-full border border-[#D4AF57]/40 font-semibold uppercase tracking-wider">
                  Tahajjud Squad
                </span>
              </div>
              <p className="text-[11px] text-[#D8E2DC]">
                {lang === 'bn' ? 'ঐক্যবদ্ধ ইবাদত ও ট্র্যাকিং প্ল্যাটফর্ম' : 'Unified Worship & Habit Platform'}
              </p>
            </div>
          </Link>

          <div className="flex items-center space-x-3">
            <button
              onClick={() => setLang(lang === 'bn' ? 'en' : 'bn')}
              className="px-3 py-1.5 text-xs font-semibold rounded-lg bg-[#0C291E] border border-[#D4AF57]/40 text-[#FFFDF5] flex items-center space-x-1.5 hover:bg-[#1A5641] transition"
            >
              <Languages className="w-3.5 h-3.5 text-[#D4AF57]" />
              <span>{lang === 'bn' ? 'English' : 'বাংলা'}</span>
            </button>
            <Link 
              href="/" 
              className="text-xs text-[#D8E2DC] hover:text-[#FFFDF5] flex items-center space-x-1 px-2.5 py-1.5 rounded-lg hover:bg-white/10 transition"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">{lang === 'bn' ? 'হোমে ফিরে যান' : 'Back to Home'}</span>
            </Link>
          </div>
        </div>
      </header>

      {/* Main Authentication Card Container */}
      <main className="flex-1 flex items-center justify-center px-4 py-10">
        <div className="max-w-md w-full">
          {/* Card Container in Warm White #FFFDF5 */}
          <div className="bg-[#FFFDF5] rounded-3xl shadow-xl border border-[#E5DFD1] overflow-hidden">
            {/* Header in Emerald Green #123C2D with Royal Gold #D4AF57 */}
            <div className="bg-gradient-to-br from-[#0C291E] via-[#123C2D] to-[#1A5641] p-7 text-[#FFFDF5] text-center relative">
              <div className="absolute top-3 right-3 opacity-20">
                <Moon className="w-16 h-16 text-[#D4AF57]" />
              </div>
              <div className="w-14 h-14 mx-auto rounded-2xl bg-white/10 backdrop-blur-md border border-[#D4AF57]/60 flex items-center justify-center text-[#D4AF57] mb-3 shadow-inner">
                <Sparkles className="w-7 h-7 text-[#D4AF57]" />
              </div>
              <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-[#FFFDF5] mb-1">
                {lang === 'bn' ? 'আমল ট্র্যাকার সাইন-ইন' : 'Sign in to Amal Tracker'}
              </h1>
              <p className="text-xs text-[#D8E2DC] max-w-xs mx-auto">
                {lang === 'bn'
                  ? 'আপনার অ্যান্ড্রয়েড অ্যাপ এবং ওয়েব অ্যাকাউন্টের মধ্যে নিখুঁত সিঙ্ক উপভোগ করুন'
                  : 'Experience seamless sync between your Android app and Web dashboard'}
              </p>
            </div>

            <div className="p-6 sm:p-8 space-y-6">
              {/* Feedback Messages */}
              {errorMessage && (
                <div className="p-3.5 rounded-xl bg-[#FDF0F0] border border-[#9E4040]/30 text-[#9E4040] text-xs flex items-start space-x-2.5">
                  <AlertCircle className="w-4 h-4 text-[#9E4040] flex-shrink-0 mt-0.5" />
                  <span className="leading-relaxed">{errorMessage}</span>
                </div>
              )}

              {successMessage && (
                <div className="p-3.5 rounded-xl bg-[#EAF5EE] border border-[#1E7E52]/30 text-[#1E7E52] text-xs flex items-center space-x-2.5">
                  <CheckCircle2 className="w-4 h-4 text-[#1E7E52] flex-shrink-0" />
                  <span className="font-medium">{successMessage}</span>
                </div>
              )}

              {/* Already Logged In Quick Status */}
              {currentUser && !successMessage && (
                <div className="p-4 rounded-2xl bg-[#EBF4F0] border border-[#123C2D]/20 text-xs flex items-center justify-between">
                  <div>
                    <p className="font-semibold text-[#123C2D]">
                      {lang === 'bn' ? 'বর্তমান সেশন সক্রিয় আছে:' : 'Active session found:'}
                    </p>
                    <p className="text-[#1A5641] truncate max-w-[200px]">{currentUser.email || currentUser.displayName}</p>
                  </div>
                  <Link
                    href="/"
                    className="px-3 py-1.5 rounded-lg bg-[#123C2D] text-[#FFFDF5] text-xs font-semibold hover:bg-[#0C291E] transition shadow-sm"
                  >
                    {lang === 'bn' ? 'ড্যাশবোর্ডে যান' : 'Go to App'}
                  </Link>
                </div>
              )}

              {/* Primary Sign-In: Google Single Sign-On */}
              <div className="space-y-3">
                <button
                  type="button"
                  onClick={handleGoogleSignIn}
                  disabled={loading}
                  className={`w-full py-3.5 px-4 bg-white hover:bg-[#F7F5EC] border border-[#E5DFD1] hover:border-[#252B27] rounded-2xl font-semibold text-sm text-[#252B27] shadow-sm flex items-center justify-center space-x-3 transition duration-150 hover:shadow-md disabled:opacity-80 ${
                    loading ? 'animate-pulse cursor-wait ring-2 ring-[#123C2D]/20' : ''
                  }`}
                >
                  {loading ? (
                    <>
                      <Loader2 className="w-5 h-5 animate-spin text-[#123C2D] flex-shrink-0" />
                      <span className="text-[#123C2D] font-bold">
                        {lang === 'bn' ? 'যাচাই করা হচ্ছে...' : 'Connecting to Google...'}
                      </span>
                    </>
                  ) : (
                    <>
                      <svg className="w-5 h-5 flex-shrink-0" viewBox="0 0 24 24">
                        <path
                          fill="#4285F4"
                          d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                        />
                        <path
                          fill="#34A853"
                          d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                        />
                        <path
                          fill="#FBBC05"
                          d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
                        />
                        <path
                          fill="#EA4335"
                          d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
                        />
                      </svg>
                      <span>
                        {lang === 'bn' ? 'গুগল দিয়ে প্রবেশ করুন (Google Sign-In)' : 'Continue with Google'}
                      </span>
                    </>
                  )}
                </button>
              </div>

              {/* Or Divider */}
              <div className="relative flex items-center justify-center">
                <div className="border-t border-[#E5DFD1] w-full" />
                <span className="bg-[#FFFDF5] px-3 text-[11px] font-semibold text-[#6B7870] uppercase tracking-wider">
                  {lang === 'bn' ? 'অথবা ইমেইল দিয়ে' : 'or with email'}
                </span>
                <div className="border-t border-[#E5DFD1] w-full" />
              </div>

              {/* Email / Password Form */}
              <form onSubmit={handleEmailAuth} className="space-y-4">
                {isNewUserMode && (
                  <div>
                    <label className="block text-xs font-semibold text-[#434E47] mb-1.5">
                      {lang === 'bn' ? 'আপনার নাম' : 'Full Name'}
                    </label>
                    <input
                      type="text"
                      value={displayName}
                      onChange={(e) => setDisplayName(e.target.value)}
                      placeholder={lang === 'bn' ? 'উদা: আব্দুল্লাহ' : 'e.g. Abdullah'}
                      className="w-full px-4 py-3 rounded-2xl border border-[#E5DFD1] bg-white text-sm text-[#252B27] placeholder:text-[#6B7870]/60 outline-none focus:outline-none focus:ring-0 focus:border-[#252B27] transition-colors duration-150"
                      required={isNewUserMode}
                    />
                  </div>
                )}

                <div>
                  <label className="block text-xs font-semibold text-[#434E47] mb-1.5">
                    {lang === 'bn' ? 'ইমেইল অ্যাড্রেস' : 'Email Address'}
                  </label>
                  <div className="relative">
                    <input
                      type="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      placeholder="user@example.com"
                      className="w-full pl-11 pr-4 py-3 rounded-2xl border border-[#E5DFD1] bg-white text-sm text-[#252B27] placeholder:text-[#6B7870]/60 outline-none focus:outline-none focus:ring-0 focus:border-[#252B27] transition-colors duration-150"
                      required
                    />
                    <Mail className="w-4 h-4 text-[#6B7870] absolute left-4 top-3.5" />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-[#434E47] mb-1.5">
                    {lang === 'bn' ? 'পাসওয়ার্ড' : 'Password'}
                  </label>
                  <div className="relative">
                    <input
                      type="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="••••••••"
                      className="w-full pl-11 pr-4 py-3 rounded-2xl border border-[#E5DFD1] bg-white text-sm text-[#252B27] placeholder:text-[#6B7870]/60 outline-none focus:outline-none focus:ring-0 focus:border-[#252B27] transition-colors duration-150"
                      required
                    />
                    <Lock className="w-4 h-4 text-[#6B7870] absolute left-4 top-3.5" />
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className={`w-full py-3.5 px-4 rounded-2xl bg-gradient-to-r from-[#0C291E] via-[#123C2D] to-[#1A5641] hover:from-[#123C2D] hover:to-[#1A5641] text-[#FFFDF5] font-semibold text-sm shadow-md transition-all duration-200 hover:shadow-lg disabled:opacity-85 flex items-center justify-center space-x-2.5 relative overflow-hidden ${
                    loading ? 'animate-pulse ring-2 ring-[#D4AF57]/40 cursor-wait' : ''
                  }`}
                >
                  {loading ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin text-[#D4AF57] flex-shrink-0" />
                      <span className="tracking-wide">
                        {lang === 'bn' ? 'অপেক্ষা করুন, যাচাই হচ্ছে...' : 'Authenticating...'}
                      </span>
                    </>
                  ) : (
                    <>
                      <span>
                        {isNewUserMode
                          ? (lang === 'bn' ? 'নতুন অ্যাকাউন্ট খুলুন' : 'Create Account')
                          : (lang === 'bn' ? 'লগইন করুন' : 'Sign In')}
                      </span>
                      <ChevronRight className="w-4 h-4 text-[#D4AF57]" />
                    </>
                  )}
                </button>
              </form>

              {/* Mode Toggle */}
              <div className="text-center pt-1">
                <button
                  type="button"
                  onClick={() => {
                    setIsNewUserMode(!isNewUserMode);
                    setErrorMessage(null);
                    setSuccessMessage(null);
                  }}
                  className="text-xs text-[#123C2D] font-semibold hover:underline"
                >
                  {isNewUserMode
                    ? (lang === 'bn' ? 'আগে থেকেই অ্যাকাউন্ট আছে? সাইন-ইন করুন' : 'Already have an account? Sign In')
                    : (lang === 'bn' ? 'নতুন ব্যবহারকারী? একটি অ্যাকাউন্ট তৈরি করুন' : "Don't have an account? Sign Up")}
                </button>
              </div>

              {/* Verified Platform Synchronicity Badges */}
              <div className="pt-2 border-t border-[#E5DFD1] space-y-2">
                <div className="flex items-center space-x-2 text-[11px] text-[#434E47]">
                  <ShieldCheck className="w-4 h-4 text-[#123C2D] flex-shrink-0" />
                  <span>
                    {lang === 'bn' 
                      ? 'অ্যান্ড্রয়েড ও ওয়েবে একাউন্ট সমলয় (Project: ' + FIREBASE_PROJECT_ID + ')'
                      : `Cross-device sync active (Project: ${FIREBASE_PROJECT_ID})`}
                  </span>
                </div>
                <div className="flex items-center space-x-2 text-[11px] text-[#434E47]">
                  <Clock className="w-4 h-4 text-[#D4AF57] flex-shrink-0" />
                  <span>
                    {lang === 'bn' 
                      ? 'তাহাজ্জুদ স্কোয়াড ও পাঁচ ওয়াক্ত সালাত ক্লাউড ব্যাকআপ'
                      : 'Tahajjud Squad & Daily Prayer automatic cloud records'}
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Privacy Note */}
          <p className="text-[11px] text-center text-[#6B7870] mt-4 leading-relaxed">
            {lang === 'bn'
              ? '🔒 আপনার ইবাদতের তথ্য একান্তই আপনার ও আল্লাহর মধ্যকার বিষয়। সংবেদনশীল তথ্য কখনোই প্রকাশ্যে প্রকাশ করা হয় না।'
              : '🔒 Your worship records are strictly private and protected with zero-trust privacy controls.'}
          </p>
        </div>
      </main>

      {/* Footer */}
      <footer className="w-full text-center py-4 text-xs text-[#6B7870] border-t border-[#E5DFD1]">
        Amal Tracker & Tahajjud Squad Platform • {new Date().getFullYear()}
      </footer>
    </div>
  );
}
