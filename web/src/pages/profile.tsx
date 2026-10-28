import React, { useState, useEffect } from 'react';
import { UserProfile, SharingConsentScope } from '../types';
import { 
  User as UserIcon, 
  ShieldCheck, 
  MapPin, 
  Trash2, 
  CheckCircle2, 
  Lock, 
  LogIn, 
  LogOut 
} from 'lucide-react';
import { useAuth } from '@/context/AuthContext';

export default function ProfilePage() {
  const { user, profile: authProfile, signInWithGoogle, signOutUser, updateConsentScope, isSuperAdmin } = useAuth();
  
  const [profile, setProfile] = useState<UserProfile>({
    uid: 'user-default-1',
    displayName: 'মুহাম্মদ আব্দুল্লাহ',
    email: 'abdullah@example.com',
    timezone: 'Asia/Dhaka',
    language: 'bn',
    consentGiven: true,
    consentScope: 'AGGREGATE_ONLY',
    consentTimestamp: Date.now() - 86400000 * 30,
    tahajjudAlarmEnabled: true,
    tahajjudAlarmOffsetMinutes: 30,
    calculationCity: 'Dhaka, Bangladesh',
    latitude: 23.8103,
    longitude: 90.4125,
    isSuperAdmin: true,
    activeSquadId: 'squad-tahajjud-dhaka-1',
  });

  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    if (authProfile) {
      setProfile(authProfile);
    } else {
      const saved = localStorage.getItem('amal_user_profile');
      if (saved) {
        try {
          setProfile(JSON.parse(saved));
        } catch (e) {
          console.error(e);
        }
      }
    }
  }, [authProfile]);

  const saveProfile = (updated: UserProfile) => {
    setProfile(updated);
    localStorage.setItem('amal_user_profile', JSON.stringify(updated));
    setMessage('প্রোফাইল সেটিংস সফলভাবে সংরক্ষিত হয়েছে।');
    setTimeout(() => setMessage(null), 3000);
  };

  const handleConsentScopeChange = async (scope: SharingConsentScope) => {
    const updated = {
      ...profile,
      consentScope: scope,
      consentTimestamp: Date.now(),
    };
    saveProfile(updated);
    if (user) {
      await updateConsentScope(scope);
    }
  };

  const handleDataWipe = () => {
    const confirmed = confirm(
      'সতর্কতা: এটি আপনার সমস্ত সংরক্ষিত সালাত, তাহাজ্জুদ, আমল ও যিকির রেকর্ড স্থায়ীভাবে মুছে ফেলবে। আপনি কি নিশ্চিত?'
    );
    if (confirmed) {
      localStorage.clear();
      setMessage('সমস্ত স্থানীয় রেকর্ড সফলভাবে মুছে ফেলা হয়েছে।');
      setTimeout(() => {
        window.location.reload();
      }, 1500);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6 text-[#252B27]">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-[#123C2D] flex items-center space-x-2">
          <UserIcon className="w-6 h-6 text-[#123C2D]" />
          <span>প্রোফাইল ও গোপনীয়তা সেটিংস</span>
        </h1>
        <p className="text-sm text-[#434E47] mt-1">
          আপনার অ্যাকাউন্ট তথ্য, শেয়ারিং সম্মতি এবং ইবাদতের ব্যক্তিগত নিয়ন্ত্রণ
        </p>
      </div>

      {/* Success Alert */}
      {message && (
        <div className="p-4 bg-[#EAF5EE] border border-[#1E7E52]/30 text-[#123C2D] rounded-2xl text-sm flex items-center space-x-2">
          <CheckCircle2 className="w-4 h-4 text-[#1E7E52] flex-shrink-0" />
          <span>{message}</span>
        </div>
      )}

      {/* Profile Info Card in Warm White #FFFDF5 */}
      <div className="bg-[#FFFDF5] rounded-3xl p-6 md:p-8 border border-[#E5DFD1] shadow-sm space-y-5">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-center space-x-4">
            {user?.photoURL ? (
              <img 
                src={user.photoURL} 
                alt={user.displayName || 'User'} 
                className="w-16 h-16 rounded-full border-2 border-[#D4AF57] object-cover shadow"
              />
            ) : (
              <div className="w-16 h-16 rounded-3xl bg-gradient-to-tr from-[#0C291E] to-[#123C2D] border-2 border-[#D4AF57] flex items-center justify-center text-[#D4AF57] text-2xl font-black shadow">
                {(user?.displayName || profile.displayName).charAt(0)}
              </div>
            )}
            <div>
              <div className="flex items-center space-x-2">
                <h2 className="text-lg font-bold text-[#123C2D]">
                  {user?.displayName || profile.displayName}
                </h2>
                {isSuperAdmin && (
                  <span className="text-[10px] bg-[#FBF6E9] text-[#A68532] border border-[#D4AF57]/40 px-2.5 py-0.5 rounded-full font-bold">
                    Super Admin
                  </span>
                )}
              </div>
              <p className="text-xs text-[#6B7870] mt-0.5">{user?.email || profile.email}</p>
              <p className="text-[11px] text-[#A5B5AC] mt-0.5 font-mono">
                UID: {user?.uid || profile.uid}
              </p>
            </div>
          </div>

          <div>
            {user ? (
              <button
                onClick={() => signOutUser()}
                className="px-4 py-2 rounded-xl border border-[#9E4040]/30 text-[#9E4040] hover:bg-[#FDF0F0] text-xs font-semibold flex items-center space-x-1.5 transition"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span>সাইন আউট</span>
              </button>
            ) : (
              <button
                onClick={() => signInWithGoogle()}
                className="px-4 py-2.5 rounded-2xl bg-[#123C2D] hover:bg-[#0C291E] text-[#FFFDF5] text-xs font-bold flex items-center space-x-2 transition shadow-md"
              >
                <LogIn className="w-4 h-4 text-[#D4AF57]" />
                <span>গুগল আইডি দিয়ে সিঙ্ক করুন</span>
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Privacy & Consent Section in Warm White #FFFDF5 */}
      <div className="bg-[#FFFDF5] rounded-3xl p-6 md:p-8 border border-[#E5DFD1] shadow-sm space-y-4">
        <div className="flex items-center space-x-2 text-[#123C2D]">
          <ShieldCheck className="w-5 h-5 text-[#123C2D]" />
          <h3 className="font-bold text-base">অনুমোদন ও তথ্য শেয়ারিং সম্মতি (Consent)</h3>
        </div>

        <div className="p-4 bg-[#EBF4F0] border border-[#123C2D]/20 rounded-2xl text-xs text-[#123C2D] space-y-2">
          <div className="flex items-start space-x-2">
            <Lock className="w-4 h-4 text-[#123C2D] flex-shrink-0 mt-0.5" />
            <div>
              <span className="font-bold block">ইসলামিক গোপনীয়তা ও স্বচ্ছতার অঙ্গীকার:</span>
              <span className="text-[#434E47] leading-relaxed block mt-0.5">
                আপনার আমল একান্তই আপনার ও আল্লাহর মধ্যকার ইবাদত। সুপার অ্যাডমিন বা স্কোয়াডের সদস্যরা কেবল তখনই দেখতে পারবেন যখন আপনি স্বেচ্ছায় সম্মতি প্রদান করবেন। আপনি যেকোনো সময় আপনার সম্মতি প্রত্যাহার বা পরিবর্তন করতে পারেন।
              </span>
            </div>
          </div>
        </div>

        <div className="space-y-3 pt-1">
          {/* PERSONAL_ONLY */}
          <label 
            className={`p-4 rounded-2xl border cursor-pointer transition flex items-start space-x-3.5 ${
              profile.consentScope === 'PERSONAL_ONLY'
                ? 'border-[#123C2D] bg-[#EBF4F0] ring-2 ring-[#123C2D]'
                : 'border-[#E5DFD1] hover:border-[#123C2D]/40'
            }`}
          >
            <input
              type="radio"
              name="consentScope"
              checked={profile.consentScope === 'PERSONAL_ONLY'}
              onChange={() => handleConsentScopeChange('PERSONAL_ONLY')}
              className="mt-1 text-[#123C2D] focus:ring-[#123C2D]"
            />
            <div className="space-y-1">
              <span className="text-sm font-bold text-[#123C2D] block">
                ১. সম্পূর্ণ ব্যক্তিগত (Personal Only)
              </span>
              <span className="text-xs text-[#434E47] block">
                কোনো তথ্যই গুগল শিটস বা সুপার অ্যাডমিন ড্যাশবোর্ডে সিঙ্ক হবে না। আপনার আমল কেবল আপনার ডিভাইসে সুরক্ষিত থাকবে।
              </span>
            </div>
          </label>

          {/* AGGREGATE_ONLY */}
          <label 
            className={`p-4 rounded-2xl border cursor-pointer transition flex items-start space-x-3.5 ${
              profile.consentScope === 'AGGREGATE_ONLY'
                ? 'border-[#123C2D] bg-[#EBF4F0] ring-2 ring-[#123C2D]'
                : 'border-[#E5DFD1] hover:border-[#123C2D]/40'
            }`}
          >
            <input
              type="radio"
              name="consentScope"
              checked={profile.consentScope === 'AGGREGATE_ONLY'}
              onChange={() => handleConsentScopeChange('AGGREGATE_ONLY')}
              className="mt-1 text-[#123C2D] focus:ring-[#123C2D]"
            />
            <div className="space-y-1">
              <span className="text-sm font-bold text-[#123C2D] block">
                ২. গড় পরিসংখ্যান (Aggregate Only — সুপারিশকৃত)
              </span>
              <span className="text-xs text-[#434E47] block">
                সুপার অ্যাডমিন ও স্কোয়াড কেবল আপনার সামগ্রিক দৈনিক শতাংশ ও উপস্থিতি দেখতে পারবেন। ওয়াক্তভিত্তিক খুঁটিনাটি বা ব্যক্তিগত নোট সম্পূর্ণ গোপন থাকবে।
              </span>
            </div>
          </label>

          {/* DETAILED */}
          <label 
            className={`p-4 rounded-2xl border cursor-pointer transition flex items-start space-x-3.5 ${
              profile.consentScope === 'DETAILED'
                ? 'border-[#123C2D] bg-[#EBF4F0] ring-2 ring-[#123C2D]'
                : 'border-[#E5DFD1] hover:border-[#123C2D]/40'
            }`}
          >
            <input
              type="radio"
              name="consentScope"
              checked={profile.consentScope === 'DETAILED'}
              onChange={() => handleConsentScopeChange('DETAILED')}
              className="mt-1 text-[#123C2D] focus:ring-[#123C2D]"
            />
            <div className="space-y-1">
              <span className="text-sm font-bold text-[#123C2D] block">
                ৩. বিস্তারিত পর্যবেক্ষণ (Detailed Scope)
              </span>
              <span className="text-xs text-[#434E47] block">
                অনুমোদিত সুপার অ্যাডমিন রিপোর্ট ও নির্দেশনার জন্য ওয়াক্তভিত্তিক সালাত ও তাহাজ্জুদ রেকর্ড দেখতে পাবেন। (ব্যক্তিগত নোট কখনই শেয়ার হয় না)।
              </span>
            </div>
          </label>
        </div>
      </div>

      {/* Calculation & Notification Settings */}
      <div className="bg-[#FFFDF5] rounded-3xl p-6 md:p-8 border border-[#E5DFD1] shadow-sm space-y-4">
        <div className="flex items-center space-x-2 text-[#123C2D]">
          <MapPin className="w-5 h-5 text-[#123C2D]" />
          <h3 className="font-bold text-base">সালাতের সময় ও তাহাজ্জুদ অ্যালার্ম</h3>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-bold text-[#434E47] mb-1">নির্ধারিত শহর</label>
            <input
              type="text"
              value={profile.calculationCity}
              onChange={(e) => setProfile({ ...profile, calculationCity: e.target.value })}
              className="w-full px-4 py-2.5 border border-[#E5DFD1] bg-white rounded-xl text-xs text-[#252B27] focus:ring-2 focus:ring-[#123C2D] outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-[#434E47] mb-1">তাহাজ্জুদ অ্যালার্ম অফসেট</label>
            <select
              value={profile.tahajjudAlarmOffsetMinutes}
              onChange={(e) => setProfile({ ...profile, tahajjudAlarmOffsetMinutes: parseInt(e.target.value, 10) })}
              className="w-full px-4 py-2.5 border border-[#E5DFD1] bg-white rounded-xl text-xs text-[#252B27] focus:ring-2 focus:ring-[#123C2D] outline-none"
            >
              <option value={15}>ফজরের ১৫ মিনিট আগে</option>
              <option value={30}>ফজরের ৩০ মিনিট আগে (সুপারিশকৃত)</option>
              <option value={45}>ফজরের ৪৫ মিনিট আগে</option>
              <option value={60}>ফজরের ১ ঘণ্টা আগে</option>
            </select>
          </div>
        </div>
      </div>

      {/* Account Deletion / Data Wipe (Semantic Muted Warning) */}
      <div className="bg-[#FDF0F0] rounded-3xl p-6 md:p-8 border border-[#9E4040]/30 space-y-3">
        <div className="flex items-center space-x-2 text-[#9E4040]">
          <Trash2 className="w-5 h-5 text-[#9E4040]" />
          <h3 className="font-bold text-base">ডাটা মুছে ফেলা ও অ্যাকাউন্ট রিমুভ</h3>
        </div>
        <p className="text-xs text-[#9E4040]">
          আপনি চাইলে আপনার অ্যাকাউন্টের সমস্ত স্থানীয় ও সিঙ্ক হওয়া রেকর্ড যেকোনো মুহূর্তে চিরতরে মুছে ফেলতে পারেন।
        </p>
        <button
          onClick={handleDataWipe}
          className="px-4 py-2.5 bg-[#9E4040] hover:bg-[#8A3636] text-[#FFFDF5] rounded-xl text-xs font-bold transition shadow-sm"
        >
          সমস্ত ডাটা সম্পূর্ণ মুছে ফেলুন (Data Wipe)
        </button>
      </div>
    </div>
  );
}
