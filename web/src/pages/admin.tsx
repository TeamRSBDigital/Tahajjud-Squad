import React, { useState } from 'react';
import { 
  ShieldAlert, 
  Users, 
  FileSpreadsheet, 
  RefreshCw, 
  Search, 
  CheckCircle2, 
  History,
  TrendingUp,
  Sparkles
} from 'lucide-react';
import { useAuth } from '@/context/AuthContext';

interface ConsentedUser {
  uid: string;
  displayName: string;
  email: string;
  consentScope: 'PERSONAL_ONLY' | 'AGGREGATE_ONLY' | 'DETAILED';
  consentGiven: boolean;
  tahajjudCount7d: number;
  prayerScore7d: number;
  lastActive: string;
}

export default function AdminPage() {
  const { isSuperAdmin } = useAuth();
  const [searchTerm, setSearchTerm] = useState('');
  const [syncStatus, setSyncStatus] = useState<'IDLE' | 'SYNCING' | 'SUCCESS' | 'ERROR'>('IDLE');
  const [syncLogs, setSyncLogs] = useState<string[]>([
    'System ready. Apps Script endpoint verified.',
    'Schema v2 validated: 10 target sheets active.',
  ]);
  const [auditLogs, setAuditLogs] = useState([
    { id: '1', action: 'GOOGLE_SHEETS_SYNC', details: 'Full batch sync completed for 12 records', time: '10 মিনিট আগে' },
    { id: '2', action: 'AUDIT_USER_CONSENT', details: 'Checked consent scope for user-2', time: '১ ঘণ্টা আগে' },
    { id: '3', action: 'SQUAD_OVERVIEW', details: 'Reviewed Tahajjud Squad Dhaka aggregate stats', time: '৩ ঘণ্টা আগে' },
  ]);

  const [users] = useState<ConsentedUser[]>([
    {
      uid: 'usr-101',
      displayName: 'মুহাম্মদ আব্দুল্লাহ',
      email: 'abdullah@example.com',
      consentScope: 'DETAILED',
      consentGiven: true,
      tahajjudCount7d: 6,
      prayerScore7d: 96,
      lastActive: 'আজ, ০৫:৩০ AM',
    },
    {
      uid: 'usr-102',
      displayName: 'তানভীর হাসান',
      email: 'tanvir@example.com',
      consentScope: 'AGGREGATE_ONLY',
      consentGiven: true,
      tahajjudCount7d: 4,
      prayerScore7d: 88,
      lastActive: 'আজ, ০১:১৫ PM',
    },
    {
      uid: 'usr-103',
      displayName: 'মাহমুদুল হক',
      email: 'mahmud@example.com',
      consentScope: 'PERSONAL_ONLY',
      consentGiven: false,
      tahajjudCount7d: 0,
      prayerScore7d: 0,
      lastActive: 'গতকাল, ০৯:২০ PM',
    },
    {
      uid: 'usr-104',
      displayName: 'রাকিবুল ইসলাম',
      email: 'rakib@example.com',
      consentScope: 'AGGREGATE_ONLY',
      consentGiven: true,
      tahajjudCount7d: 3,
      prayerScore7d: 82,
      lastActive: 'আজ, ০৬:০০ AM',
    },
  ]);

  const filteredUsers = users.filter(u => 
    u.displayName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    u.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleTriggerSync = () => {
    setSyncStatus('SYNCING');
    setSyncLogs(prev => [`[${new Date().toLocaleTimeString()}] Triggering Apps Script Webhook batch sync...`, ...prev]);

    setTimeout(() => {
      setSyncStatus('SUCCESS');
      setSyncLogs(prev => [
        `[${new Date().toLocaleTimeString()}] 200 OK: 14 prayer records, 4 tahajjud records, and 1 squad summary upserted to Google Sheets.`,
        ...prev
      ]);
      setAuditLogs(prev => [
        {
          id: Date.now().toString(),
          action: 'GOOGLE_SHEETS_MANUAL_SYNC',
          details: 'Successfully pushed latest consented records to centralized Google Sheet',
          time: 'এইমাত্র'
        },
        ...prev
      ]);
      setTimeout(() => setSyncStatus('IDLE'), 4000);
    }, 1800);
  };

  if (!isSuperAdmin) {
    return (
      <div className="bg-[#FDF0F0] border border-[#9E4040]/30 text-[#9E4040] rounded-3xl p-8 text-center max-w-lg mx-auto my-12 space-y-4">
        <ShieldAlert className="w-12 h-12 text-[#9E4040] mx-auto" />
        <h2 className="text-xl font-bold">অননুমোদিত প্রবেশাধিকার</h2>
        <p className="text-xs text-[#9E4040]">
          এই ড্যাশবোর্ডটি শুধুমাত্র অনুমোদিত সুপার অ্যাডমিনদের জন্য সংরক্ষিত। আপনার অ্যাকাউন্টে অ্যাডমিন রোল সক্রিয় নেই।
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6 text-[#252B27]">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <span className="px-3 py-1 rounded-full text-xs font-bold bg-[#123C2D] text-[#FFFDF5] shadow-sm">
              SUPER ADMIN DASHBOARD
            </span>
            <span className="text-xs text-[#6B7870]">v2.0 (Google Sheets v2 Ready)</span>
          </div>
          <h1 className="text-2xl font-black text-[#123C2D] mt-2 flex items-center space-x-2">
            <ShieldAlert className="w-6 h-6 text-[#123C2D]" />
            <span>সুপার অ্যাডমিন ও সেন্ট্রাল মনিটরিং হাব</span>
          </h1>
        </div>

        <button
          onClick={handleTriggerSync}
          disabled={syncStatus === 'SYNCING'}
          className="inline-flex items-center space-x-2 px-4 py-2.5 rounded-2xl bg-[#123C2D] hover:bg-[#0C291E] text-[#FFFDF5] font-bold text-xs shadow-md transition disabled:opacity-50"
        >
          <RefreshCw className={`w-4 h-4 text-[#D4AF57] ${syncStatus === 'SYNCING' ? 'animate-spin' : ''}`} />
          <span>{syncStatus === 'SYNCING' ? 'গুগল শিটসে সিঙ্ক হচ্ছে...' : 'গুগল শিটসে এখনই সিঙ্ক করুন'}</span>
        </button>
      </div>

      {/* Sync Status Banner */}
      {syncStatus === 'SUCCESS' && (
        <div className="p-4 bg-[#EAF5EE] border border-[#1E7E52]/30 text-[#123C2D] rounded-2xl flex items-center space-x-3">
          <CheckCircle2 className="w-5 h-5 text-[#1E7E52] flex-shrink-0" />
          <div className="text-xs">
            <span className="font-bold block">গুগল শিটস সিঙ্ক্রোনাইজেশন সফল হয়েছে!</span>
            <span>ব্যবহারকারীদের সম্মতির ফিল্টার অনুসারে অনুমতিপ্রাপ্ত ডাটা গুগল শিটসে যুক্ত হয়েছে।</span>
          </div>
        </div>
      )}

      {/* Top KPI Cards in Warm White #FFFDF5 */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-[#FFFDF5] rounded-3xl p-5 border border-[#E5DFD1] shadow-sm">
          <div className="text-xs text-[#6B7870] font-semibold flex items-center justify-between">
            <span>নিবন্ধিত ব্যবহারকারী</span>
            <Users className="w-4 h-4 text-[#123C2D]" />
          </div>
          <div className="text-2xl font-black text-[#123C2D] mt-2">৪ জন</div>
          <div className="text-[11px] text-[#1E7E52] font-bold mt-1">৩ জন সম্মতিপ্রাপ্ত (Consented)</div>
        </div>

        <div className="bg-[#FFFDF5] rounded-3xl p-5 border border-[#E5DFD1] shadow-sm">
          <div className="text-xs text-[#6B7870] font-semibold flex items-center justify-between">
            <span>সক্রিয় তাহাজ্জুদ স্কোয়াড</span>
            <TrendingUp className="w-4 h-4 text-[#D4AF57]" />
          </div>
          <div className="text-2xl font-black text-[#123C2D] mt-2">২ টি</div>
          <div className="text-[11px] text-[#6B7870] mt-1">১০০% ইনভাইট কোড সুরক্ষিত</div>
        </div>

        <div className="bg-[#FFFDF5] rounded-3xl p-5 border border-[#E5DFD1] shadow-sm">
          <div className="text-xs text-[#6B7870] font-semibold flex items-center justify-between">
            <span>শিটস আউটবক্স অবস্থা</span>
            <FileSpreadsheet className="w-4 h-4 text-[#123C2D]" />
          </div>
          <div className="text-2xl font-black text-[#1E7E52] mt-2">০ পেন্ডিং</div>
          <div className="text-[11px] text-[#6B7870] mt-1">সব ডাটা সিঙ্ক সম্পন্ন</div>
        </div>

        <div className="bg-[#FFFDF5] rounded-3xl p-5 border border-[#E5DFD1] shadow-sm">
          <div className="text-xs text-[#6B7870] font-semibold flex items-center justify-between">
            <span>অডিট ও নিরাপত্তা লগ</span>
            <History className="w-4 h-4 text-[#D4AF57]" />
          </div>
          <div className="text-2xl font-black text-[#123C2D] mt-2">১০০%</div>
          <div className="text-[11px] text-[#1E7E52] font-bold mt-1">সকল কোয়েরি সংরক্ষিত</div>
        </div>
      </div>

      {/* User Monitoring Table with Consent Status in Warm White #FFFDF5 */}
      <div className="bg-[#FFFDF5] rounded-3xl border border-[#E5DFD1] shadow-sm overflow-hidden">
        <div className="p-5 border-b border-[#E5DFD1] flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center space-x-2">
            <Users className="w-4 h-4 text-[#123C2D]" />
            <h3 className="font-bold text-sm text-[#123C2D]">ব্যবহারকারীদের সম্মতি ও আমল পর্যবেক্ষণ</h3>
          </div>

          <div className="relative">
            <Search className="w-4 h-4 text-[#6B7870] absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="নাম বা ইমেইল দিয়ে খুঁজুন..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-9 pr-3 py-2.5 border border-[#E5DFD1] bg-white rounded-2xl text-xs text-[#252B27] outline-none focus:outline-none focus:ring-0 focus:border-[#252B27] transition-colors duration-150 w-64"
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-[#F7F5EC] border-b border-[#E5DFD1] text-[#434E47] font-bold">
                <th className="py-3 px-4">ব্যবহারকারী</th>
                <th className="py-3 px-3">শেয়ারিং সম্মতি (Scope)</th>
                <th className="py-3 px-3">তাহাজ্জুদ (৭ দিন)</th>
                <th className="py-3 px-3">সালাত হার</th>
                <th className="py-3 px-3">সর্বশেষ সক্রিয়তা</th>
                <th className="py-3 px-4 text-right">কার্যক্রম</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E5DFD1] text-[#252B27]">
              {filteredUsers.map((u) => (
                <tr key={u.uid} className="hover:bg-[#FBF6E9]/60 transition">
                  <td className="py-3.5 px-4">
                    <div className="font-bold text-[#123C2D]">{u.displayName}</div>
                    <div className="text-[11px] text-[#6B7870] font-mono">{u.email}</div>
                  </td>

                  <td className="py-3.5 px-3">
                    {u.consentScope === 'DETAILED' ? (
                      <span className="inline-flex items-center px-2 py-0.5 rounded-lg text-[11px] font-bold bg-[#EAF5EE] text-[#1E7E52] border border-[#1E7E52]/20">
                        বিস্তারিত অনুমোদন (Detailed)
                      </span>
                    ) : u.consentScope === 'AGGREGATE_ONLY' ? (
                      <span className="inline-flex items-center px-2 py-0.5 rounded-lg text-[11px] font-bold bg-[#EBF4F0] text-[#123C2D] border border-[#123C2D]/20">
                        গড় পরিসংখ্যান (Aggregate)
                      </span>
                    ) : (
                      <span className="inline-flex items-center px-2 py-0.5 rounded-lg text-[11px] font-bold bg-[#F2EFE4] text-[#434E47] border border-[#E5DFD1]">
                        🔒 কোনো সম্মতি নেই (Personal Only)
                      </span>
                    )}
                  </td>

                  <td className="py-3.5 px-3">
                    {u.consentGiven ? (
                      <span className="font-bold text-[#1E7E52]">{u.tahajjudCount7d} রাত</span>
                    ) : (
                      <span className="text-[#6B7870] italic">সীমাবদ্ধ (Gated)</span>
                    )}
                  </td>

                  <td className="py-3.5 px-3">
                    {u.consentGiven ? (
                      <span className="font-black text-[#123C2D]">{u.prayerScore7d}%</span>
                    ) : (
                      <span className="text-[#6B7870] italic">সীমাবদ্ধ (Gated)</span>
                    )}
                  </td>

                  <td className="py-3.5 px-3 text-[#6B7870] text-[11px]">
                    {u.lastActive}
                  </td>

                  <td className="py-3.5 px-4 text-right">
                    {u.consentGiven ? (
                      <button 
                        onClick={() => alert(`${u.displayName}-এর বিস্তারিত রিপোর্ট লোড করা হচ্ছে...`)}
                        className="px-3 py-1 bg-[#EBF4F0] hover:bg-[#123C2D] hover:text-[#FFFDF5] rounded-xl text-[11px] font-bold text-[#123C2D] border border-[#123C2D]/20 transition"
                      >
                        রিপোর্ট দেখুন
                      </button>
                    ) : (
                      <span className="text-[#6B7870] text-[10px]">অ্যাক্সেসযোগ্য নয়</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Sync Console & Audit Trail */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Apps Script Console Log in Dark Emerald Container */}
        <div className="bg-[#0C291E] text-[#D8E2DC] rounded-3xl p-5 border border-[#D4AF57]/30 font-mono text-xs space-y-2 shadow-sm">
          <div className="flex items-center justify-between text-[#E5C678] pb-2 border-b border-[#D4AF57]/30">
            <span className="font-bold">Apps Script Webhook Console</span>
            <span className="text-[10px] text-[#1E7E52] font-semibold bg-[#EAF5EE] px-2 py-0.5 rounded">STATUS: 200 OK</span>
          </div>
          <div className="h-40 overflow-y-auto space-y-1 text-[11px]">
            {syncLogs.map((log, i) => (
              <div key={i} className="text-[#D8E2DC] leading-relaxed">
                &gt; {log}
              </div>
            ))}
          </div>
        </div>

        {/* Administrative Audit Trail in Warm White #FFFDF5 */}
        <div className="bg-[#FFFDF5] rounded-3xl p-5 border border-[#E5DFD1] shadow-sm space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-xs text-[#123C2D] uppercase tracking-wider flex items-center space-x-1.5">
              <History className="w-3.5 h-3.5 text-[#123C2D]" />
              <span>অ্যাডমিন অডিট ট্রেইল (Audit Trail)</span>
            </h3>
            <span className="text-[10px] text-[#6B7870]">সর্বশেষ ৩টি ইভেন্ট</span>
          </div>

          <div className="space-y-2 text-xs">
            {auditLogs.map((log) => (
              <div key={log.id} className="p-3 rounded-2xl bg-[#F7F5EC] border border-[#E5DFD1] flex items-start justify-between">
                <div>
                  <span className="font-bold text-[11px] text-[#123C2D] block font-mono">{log.action}</span>
                  <span className="text-[#434E47] text-[11px]">{log.details}</span>
                </div>
                <span className="text-[10px] text-[#6B7870] whitespace-nowrap ml-2">{log.time}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
