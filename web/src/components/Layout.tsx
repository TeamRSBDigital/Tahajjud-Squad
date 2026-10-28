import React, { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/router';
import { 
  Home, 
  Clock, 
  Moon, 
  BookOpen, 
  Users, 
  Fingerprint, 
  BarChart2, 
  User as UserIcon, 
  ShieldAlert,
  Languages,
  LogIn,
  LogOut,
  Sparkles,
  CheckCircle2
} from 'lucide-react';
import { useAuth } from '@/context/AuthContext';

interface LayoutProps {
  children: React.ReactNode;
}

export default function Layout({ children }: LayoutProps) {
  const router = useRouter();
  const [lang, setLang] = useState<'bn' | 'en'>('bn');
  const [showAuthModal, setShowAuthModal] = useState(false);
  const { user, profile, signInWithGoogle, signOutUser, loading, isSuperAdmin } = useAuth();

  const navItems = [
    { path: '/', labelBn: 'হোম', labelEn: 'Home', icon: Home },
    { path: '/prayer', labelBn: 'সালাত', labelEn: 'Prayer', icon: Clock },
    { path: '/tahajjud', labelBn: 'তাহাজ্জুদ', labelEn: 'Tahajjud', icon: Moon },
    { path: '/amal', labelBn: 'আমল', labelEn: 'Amal', icon: BookOpen },
    { path: '/squads', labelBn: 'স্কোয়াড', labelEn: 'Squad', icon: Users },
    { path: '/dhikr', labelBn: 'যিকির', labelEn: 'Dhikr', icon: Fingerprint },
    { path: '/records', labelBn: 'রেকর্ড', labelEn: 'Records', icon: BarChart2 },
    { path: '/profile', labelBn: 'প্রোফাইল', labelEn: 'Profile', icon: UserIcon },
  ];

  return (
    <div className="min-h-screen bg-[#F7F5EC] text-[#252B27] flex flex-col font-sans selection:bg-[#123C2D] selection:text-[#FFFDF5]">
      {/* Top Navigation Bar in Deep Emerald Green #123C2D with Royal Gold #D4AF57 Accents */}
      <header className="sticky top-0 z-40 bg-[#123C2D] border-b border-[#D4AF57]/40 text-[#FFFDF5] shadow-md">
        <div className="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
          <Link href="/" className="flex items-center space-x-2.5">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-[#0C291E] to-[#1A5641] border border-[#D4AF57] flex items-center justify-center text-[#D4AF57] font-bold text-xl shadow-md">
              ☪
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="text-lg font-bold text-[#FFFDF5] tracking-wide">Amal Tracker</span>
                <span className="hidden sm:inline-block text-[11px] bg-[#0C291E] text-[#E5C678] px-2 py-0.5 rounded-full border border-[#D4AF57]/40 font-semibold">
                  Tahajjud Squad
                </span>
              </div>
              <span className="hidden md:block text-[10px] text-[#D8E2DC]">
                দৈনন্দিন সালাত, তাহাজ্জুদ ও আমল প্ল্যাটফর্ম
              </span>
            </div>
          </Link>

          {/* Desktop Nav Links */}
          <nav className="hidden lg:flex items-center space-x-1">
            {navItems.slice(0, 6).map((item) => {
              const Icon = item.icon;
              const isActive = router.pathname === item.path;
              return (
                <Link
                  key={item.path}
                  href={item.path}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-1.5 transition ${
                    isActive
                      ? 'bg-[#0C291E] text-[#D4AF57] border border-[#D4AF57]/40 shadow-sm'
                      : 'text-[#EAF5EE] hover:bg-[#1A5641]/60 hover:text-[#FFFDF5]'
                  }`}
                >
                  <Icon className="w-3.5 h-3.5" />
                  <span>{lang === 'bn' ? item.labelBn : item.labelEn}</span>
                </Link>
              );
            })}
          </nav>

          <div className="flex items-center space-x-2.5">
            {/* Super Admin Quick Link */}
            {isSuperAdmin && (
              <Link
                href="/admin"
                className={`text-xs px-2.5 py-1.5 rounded-lg flex items-center space-x-1 border transition ${
                  router.pathname === '/admin'
                    ? 'bg-[#D4AF57] text-[#123C2D] border-[#D4AF57] font-bold shadow'
                    : 'bg-[#0C291E] text-[#E5C678] border-[#D4AF57]/40 hover:bg-[#1A5641]'
                }`}
              >
                <ShieldAlert className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">{lang === 'bn' ? 'সুপার অ্যাডমিন' : 'Admin'}</span>
              </Link>
            )}

            {/* Language Toggle */}
            <button
              onClick={() => setLang(lang === 'bn' ? 'en' : 'bn')}
              className="px-2.5 py-1.5 text-xs font-semibold rounded-lg bg-[#0C291E] border border-[#D4AF57]/40 text-[#EAF5EE] flex items-center space-x-1 hover:bg-[#1A5641] transition"
              title="ভাষা পরিবর্তন"
            >
              <Languages className="w-3.5 h-3.5 text-[#D4AF57]" />
              <span>{lang === 'bn' ? 'বাংলা' : 'EN'}</span>
            </button>

            {/* Firebase Auth Button / Profile Avatar */}
            {user ? (
              <div className="flex items-center space-x-2 pl-1">
                <Link href="/profile" className="flex items-center space-x-2 group">
                  {user.photoURL ? (
                    <img 
                      src={user.photoURL} 
                      alt={user.displayName || 'User'} 
                      className="w-8 h-8 rounded-full border-2 border-[#D4AF57] object-cover shadow"
                    />
                  ) : (
                    <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-[#0C291E] to-[#1A5641] border border-[#D4AF57] flex items-center justify-center text-xs font-bold text-[#D4AF57] shadow">
                      {user.displayName ? user.displayName.charAt(0) : 'U'}
                    </div>
                  )}
                  <div className="hidden xl:block text-left">
                    <span className="text-xs font-semibold text-[#FFFDF5] block leading-tight group-hover:text-[#D4AF57] transition">
                      {user.displayName?.split(' ')[0] || 'মুমিন'}
                    </span>
                    <span className="text-[10px] text-[#D8E2DC] block leading-tight">
                      সিঙ্ক সক্রিয়
                    </span>
                  </div>
                </Link>

                <button
                  onClick={() => signOutUser()}
                  className="p-1.5 rounded-lg text-[#D8E2DC] hover:text-[#E5C678] hover:bg-white/10 transition"
                  title="সাইন আউট"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <Link
                href="/login"
                className="px-3 py-1.5 rounded-xl bg-gradient-to-r from-[#1A5641] to-[#123C2D] hover:from-[#123C2D] hover:to-[#0C291E] text-[#FFFDF5] border border-[#D4AF57]/60 font-semibold text-xs flex items-center space-x-1.5 shadow-md transition"
              >
                <LogIn className="w-3.5 h-3.5 text-[#D4AF57]" />
                <span>{lang === 'bn' ? 'লগইন' : 'Sign In'}</span>
              </Link>
            )}
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-6xl w-full mx-auto px-4 py-6 mb-20 lg:mb-8">
        {children}
      </main>

      {/* Bottom Navigation for Mobile / Tablet in Emerald Green #123C2D */}
      <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#123C2D] border-t border-[#D4AF57]/30 py-1.5 px-2 shadow-2xl lg:hidden">
        <div className="grid grid-cols-8 gap-1 items-center">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = router.pathname === item.path;
            return (
              <Link
                key={item.path}
                href={item.path}
                className={`flex flex-col items-center justify-center py-1 rounded-lg transition ${
                  isActive
                    ? 'text-[#D4AF57] font-bold'
                    : 'text-[#EAF5EE]/80 hover:text-[#FFFDF5]'
                }`}
              >
                <div className={`p-1 rounded-md transition ${isActive ? 'bg-[#0C291E]' : ''}`}>
                  <Icon className="w-4 h-4" />
                </div>
                <span className="text-[10px] mt-0.5 truncate max-w-[42px]">
                  {lang === 'bn' ? item.labelBn : item.labelEn}
                </span>
              </Link>
            );
          })}
        </div>
      </nav>

      {/* Google Sign-in Authentication Modal */}
      {showAuthModal && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#FFFDF5] rounded-3xl max-w-md w-full p-6 sm:p-8 shadow-2xl border border-[#E5DFD1] text-[#252B27] relative space-y-5">
            <button
              onClick={() => setShowAuthModal(false)}
              className="absolute top-4 right-4 text-[#6B7870] hover:text-[#252B27] w-8 h-8 rounded-full flex items-center justify-center bg-[#F2EFE4] hover:bg-[#E5DFD1] transition"
            >
              ✕
            </button>

            <div className="text-center space-y-2">
              <div className="w-14 h-14 bg-[#EBF4F0] border border-[#123C2D]/20 rounded-2xl flex items-center justify-center mx-auto text-[#123C2D]">
                <Sparkles className="w-7 h-7 text-[#D4AF57]" />
              </div>
              <h2 className="text-xl font-bold text-[#123C2D]">গুগল অ্যাকাউন্ট দিয়ে সাইন-ইন</h2>
              <p className="text-xs text-[#434E47] leading-relaxed max-w-xs mx-auto">
                অ্যান্ড্রয়েড অ্যাপ ও ওয়েব অ্যাপ্লিকেশনের মধ্যে তাৎক্ষণিক ক্লাউড সিঙ্ক নিশ্চিত করতে আপনার গুগল আইডি যুক্ত করুন।
              </p>
            </div>

            <div className="p-3.5 bg-[#EBF4F0] border border-[#123C2D]/20 rounded-2xl text-xs space-y-1.5 text-[#123C2D]">
              <div className="flex items-center space-x-2 font-semibold">
                <CheckCircle2 className="w-4 h-4 text-[#1E7E52]" />
                <span>যৌথ সেশন ও সিঙ্ক (Unified Session):</span>
              </div>
              <p className="text-[11px] text-[#252B27] pl-6 leading-relaxed">
                আপনার অ্যান্ড্রয়েড ফোনের Amal Tracker-এর সকল সালাত, তাহাজ্জুদ ও আমল রেকর্ড ওয়েব ড্যাশবোর্ডে স্বয়ংক্রিয়ভাবে সমন্বয় হবে।
              </p>
            </div>

            <button
              onClick={async () => {
                await signInWithGoogle();
                setShowAuthModal(false);
              }}
              disabled={loading}
              className="w-full py-3 px-4 bg-[#FFFDF5] hover:bg-[#F2EFE4] border border-[#E5DFD1] rounded-2xl font-semibold text-sm text-[#252B27] shadow-sm flex items-center justify-center space-x-3 transition hover:shadow-md"
            >
              <svg className="w-5 h-5" viewBox="0 0 24 24">
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
              <span>{loading ? 'সংযুক্ত হচ্ছে...' : 'Continue with Google'}</span>
            </button>

            <p className="text-[10px] text-center text-[#6B7870]">
              ইসলামিক নীতি ও কঠোর গোপনীয়তা রক্ষা করা হয়। কোনো ব্যক্তিগত নোট শেয়ার হয় না।
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
