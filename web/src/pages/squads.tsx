import React, { useState, useEffect } from 'react';
import { Squad, SquadMember, SharingConsentScope } from '../types';
import { 
  Users, 
  Plus, 
  Key, 
  ShieldCheck, 
  CheckCircle2, 
  Lock, 
  TrendingUp, 
  Award, 
  Info,
  LogOut,
  Sparkles
} from 'lucide-react';

export default function SquadsPage() {
  const [activeTab, setActiveTab] = useState<'my-squad' | 'join' | 'create'>('my-squad');
  const [squad, setSquad] = useState<Squad | null>(null);
  const [members, setMembers] = useState<SquadMember[]>([]);
  const [inviteCodeInput, setInviteCodeInput] = useState('');
  const [newSquadName, setNewSquadName] = useState('');
  const [newSquadDesc, setNewSquadDesc] = useState('');
  const [isPrivate, setIsPrivate] = useState(true);
  const [myShareMode, setMyShareMode] = useState<SharingConsentScope>('AGGREGATE_ONLY');
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    const savedSquad = localStorage.getItem('amal_active_squad');
    if (savedSquad) {
      try {
        const parsed = JSON.parse(savedSquad);
        setSquad(parsed);
      } catch (e) {
        console.error(e);
      }
    } else {
      const demoSquad: Squad = {
        id: 'squad-tahajjud-dhaka-1',
        name: 'তাহাজ্জুদ ও ফজর স্কোয়াড',
        description: 'নিয়মিত তাহাজ্জুদ ও ফজর নামাজ জামাতে আদায়ের পারস্পরিক উৎসাহ ও দুয়ার স্কোয়াড।',
        ownerUid: 'user-leader-1',
        inviteCode: 'TAHAJ7',
        isPrivate: true,
        memberCount: 4,
        createdAt: Date.now() - 86400000 * 14,
      };
      setSquad(demoSquad);
      localStorage.setItem('amal_active_squad', JSON.stringify(demoSquad));
    }

    const demoMembers: SquadMember[] = [
      {
        uid: 'user-leader-1',
        squadId: 'squad-tahajjud-dhaka-1',
        displayName: 'মুহাম্মদ আব্দুল্লাহ (আমীর)',
        role: 'SQUAD_LEADER',
        joinedAt: Date.now() - 86400000 * 14,
        shareMode: 'AGGREGATE_ONLY',
      },
      {
        uid: 'user-2',
        squadId: 'squad-tahajjud-dhaka-1',
        displayName: 'তানভীর হাসান',
        role: 'MEMBER',
        joinedAt: Date.now() - 86400000 * 10,
        shareMode: 'AGGREGATE_ONLY',
      },
      {
        uid: 'user-3',
        squadId: 'squad-tahajjud-dhaka-1',
        displayName: 'মাহমুদুল হক',
        role: 'MODERATOR',
        joinedAt: Date.now() - 86400000 * 8,
        shareMode: 'DETAILED',
      },
      {
        uid: 'user-me',
        displayName: 'আপনি (আমি)',
        squadId: 'squad-tahajjud-dhaka-1',
        role: 'MEMBER',
        joinedAt: Date.now() - 86400000 * 3,
        shareMode: 'AGGREGATE_ONLY',
      },
    ];
    setMembers(demoMembers);
  }, []);

  const handleJoinSquad = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inviteCodeInput.trim()) return;

    const joined: Squad = {
      id: `squad-${Date.now()}`,
      name: `স্কোয়াড (${inviteCodeInput.toUpperCase()})`,
      description: 'আমন্ত্রণের মাধ্যমে যুক্ত হওয়া ব্যক্তিগত ইবাদত স্কোয়াড।',
      ownerUid: 'other-user',
      inviteCode: inviteCodeInput.toUpperCase(),
      isPrivate: true,
      memberCount: 5,
      createdAt: Date.now(),
    };

    setSquad(joined);
    localStorage.setItem('amal_active_squad', JSON.stringify(joined));
    setMessage('সফলভাবে স্কোয়াডে যোগদান সম্পন্ন হয়েছে!');
    setActiveTab('my-squad');
    setTimeout(() => setMessage(null), 3000);
  };

  const handleCreateSquad = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newSquadName.trim()) return;

    const code = Math.random().toString(36).substring(2, 8).toUpperCase();
    const created: Squad = {
      id: `squad-${Date.now()}`,
      name: newSquadName,
      description: newSquadDesc || 'পরস্পরকে সৎকাজের আদেশ ও আমলের উৎসাহদানকারী স্কোয়াড।',
      ownerUid: 'user-me',
      inviteCode: code,
      isPrivate: isPrivate,
      memberCount: 1,
      createdAt: Date.now(),
    };

    setSquad(created);
    localStorage.setItem('amal_active_squad', JSON.stringify(created));
    setMessage(`নতুন স্কোয়াড তৈরি হয়েছে! ইনভাইট কোড: ${code}`);
    setActiveTab('my-squad');
    setNewSquadName('');
    setNewSquadDesc('');
    setTimeout(() => setMessage(null), 4000);
  };

  const handleLeaveSquad = () => {
    if (confirm('আপনি কি নিশ্চিত যে এই স্কোয়াড ত্যাগ করতে চান?')) {
      setSquad(null);
      localStorage.removeItem('amal_active_squad');
      setMessage('আপনি স্কোয়াড ত্যাগ করেছেন।');
      setTimeout(() => setMessage(null), 3000);
    }
  };

  return (
    <div className="space-y-6 text-[#252B27]">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-[#123C2D] flex items-center space-x-2">
            <Users className="w-6 h-6 text-[#123C2D]" />
            <span>তাহাজ্জুদ স্কোয়াড ও কমিউনিটি</span>
          </h1>
          <p className="text-sm text-[#434E47] mt-1">
            "তোমরা সৎকর্ম ও তাকওয়ায় একে অপরকে সাহায্য কর..." (সূরা আল-মায়েদা: ২)
          </p>
        </div>

        <div className="flex items-center space-x-2">
          <button
            onClick={() => setActiveTab('my-squad')}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition ${
              activeTab === 'my-squad'
                ? 'bg-[#123C2D] text-[#FFFDF5] shadow'
                : 'bg-[#FFFDF5] border border-[#E5DFD1] text-[#252B27] hover:bg-[#F2EFE4]'
            }`}
          >
            আমার স্কোয়াড
          </button>
          <button
            onClick={() => setActiveTab('join')}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition ${
              activeTab === 'join'
                ? 'bg-[#123C2D] text-[#FFFDF5] shadow'
                : 'bg-[#FFFDF5] border border-[#E5DFD1] text-[#252B27] hover:bg-[#F2EFE4]'
            }`}
          >
            কোড দিয়ে যুক্ত হোন
          </button>
          <button
            onClick={() => setActiveTab('create')}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition ${
              activeTab === 'create'
                ? 'bg-[#123C2D] text-[#FFFDF5] shadow'
                : 'bg-[#FFFDF5] border border-[#E5DFD1] text-[#252B27] hover:bg-[#F2EFE4]'
            }`}
          >
            নতুন স্কোয়াড খুলুন
          </button>
        </div>
      </div>

      {/* Message Alert */}
      {message && (
        <div className="p-3 bg-[#EAF5EE] border border-[#1E7E52]/30 text-[#123C2D] text-sm rounded-2xl flex items-center space-x-2">
          <CheckCircle2 className="w-4 h-4 text-[#1E7E52] flex-shrink-0" />
          <span>{message}</span>
        </div>
      )}

      {/* Tab 1: My Squad */}
      {activeTab === 'my-squad' && (
        <>
          {squad ? (
            <div className="space-y-6">
              {/* Squad Banner in Emerald Green #123C2D with Royal Gold #D4AF57 */}
              <div className="bg-gradient-to-r from-[#0C291E] via-[#123C2D] to-[#1A5641] text-[#FFFDF5] rounded-3xl p-6 md:p-8 border border-[#D4AF57]/40 shadow-xl relative overflow-hidden">
                <div className="relative z-10 flex flex-col md:flex-row md:items-start justify-between gap-4">
                  <div>
                    <div className="flex items-center space-x-2">
                      <span className="text-xs bg-white/10 border border-[#D4AF57]/40 text-[#D4AF57] px-3 py-0.5 rounded-full font-bold">
                        {squad.isPrivate ? 'প্রাইভেট স্কোয়াড' : 'পাবলিক স্কোয়াড'}
                      </span>
                      <span className="text-xs text-[#D8E2DC]">
                        {squad.memberCount} জন সদস্য
                      </span>
                    </div>
                    <h2 className="text-2xl font-black text-[#FFFDF5] mt-2">{squad.name}</h2>
                    <p className="text-sm text-[#D8E2DC] mt-1 max-w-xl">{squad.description}</p>
                  </div>

                  <div className="bg-[#0C291E]/90 backdrop-blur-md rounded-2xl p-4 border border-[#D4AF57]/40 text-center min-w-[140px]">
                    <span className="text-[11px] text-[#D4AF57] uppercase tracking-wider block font-bold">
                      ইনভাইট কোড
                    </span>
                    <span className="text-xl font-mono font-black tracking-widest text-[#FFFDF5] mt-1 block">
                      {squad.inviteCode}
                    </span>
                    <button 
                      onClick={() => {
                        navigator.clipboard?.writeText(squad.inviteCode);
                        setMessage('ইনভাইট কোড কপি করা হয়েছে!');
                        setTimeout(() => setMessage(null), 2500);
                      }}
                      className="text-xs text-[#E5C678] hover:underline mt-1.5 block w-full text-center font-semibold"
                    >
                      কপি করুন
                    </button>
                  </div>
                </div>

                {/* Aggregate Summary Metrics */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-6 pt-6 border-t border-white/15">
                  <div className="bg-[#0C291E]/70 rounded-2xl p-3.5 border border-[#D4AF57]/20">
                    <div className="text-xs text-[#D8E2DC] flex items-center space-x-1">
                      <TrendingUp className="w-3.5 h-3.5 text-[#D4AF57]" />
                      <span>স্কোয়াড গড় সালাত</span>
                    </div>
                    <div className="text-xl font-black text-[#FFFDF5] mt-1">৮৮%</div>
                    <div className="text-[10px] text-[#D8E2DC]">আজকের গড় আদায়</div>
                  </div>

                  <div className="bg-[#0C291E]/70 rounded-2xl p-3.5 border border-[#D4AF57]/20">
                    <div className="text-xs text-[#D8E2DC] flex items-center space-x-1">
                      <Award className="w-3.5 h-3.5 text-[#D4AF57]" />
                      <span>তাহাজ্জুদ উপস্থিতি</span>
                    </div>
                    <div className="text-xl font-black text-[#FFFDF5] mt-1">৩ / ৪ জন</div>
                    <div className="text-[10px] text-[#D8E2DC]">আজ রাতের তাহাজ্জুদ</div>
                  </div>

                  <div className="bg-[#0C291E]/70 rounded-2xl p-3.5 border border-[#D4AF57]/20">
                    <div className="text-xs text-[#D8E2DC] flex items-center space-x-1">
                      <Sparkles className="w-3.5 h-3.5 text-[#D4AF57]" />
                      <span>কুরআন তিলাওয়াত</span>
                    </div>
                    <div className="text-xl font-black text-[#FFFDF5] mt-1">৭৫%</div>
                    <div className="text-[10px] text-[#D8E2DC]">দৈনিক লক্ষ্য পূরণ</div>
                  </div>

                  <div className="bg-[#0C291E]/70 rounded-2xl p-3.5 border border-[#D4AF57]/20">
                    <div className="text-xs text-[#D8E2DC] flex items-center space-x-1">
                      <ShieldCheck className="w-3.5 h-3.5 text-[#D4AF57]" />
                      <span>ব্যক্তিগত গোপনীয়তা</span>
                    </div>
                    <div className="text-xl font-black text-[#FFFDF5] mt-1">সুরক্ষিত</div>
                    <div className="text-[10px] text-[#D8E2DC]">নোটস সম্পূর্ণ গোপন</div>
                  </div>
                </div>
              </div>

              {/* Privacy Selector Card in Warm White #FFFDF5 */}
              <div className="bg-[#FFFDF5] rounded-3xl p-6 border border-[#E5DFD1] shadow-sm space-y-4">
                <div className="flex items-center space-x-2 text-[#123C2D]">
                  <ShieldCheck className="w-5 h-5 text-[#123C2D]" />
                  <h3 className="font-bold text-base">এই স্কোয়াডে আপনার শেয়ারিং সেটিং</h3>
                </div>
                <p className="text-xs text-[#434E47]">
                  ইসলামিক নীতি ও ব্যবহারকারীর গোপনীয়তা রক্ষা করতে আপনি বেছে নিতে পারেন স্কোয়াডে কতটুকু তথ্য প্রদর্শিত হবে। কোনো অবস্থাতেই আপনার ব্যক্তিগত নোট বা সংবেদনশীল বিবরণ শেয়ার হবে না।
                </p>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-3 pt-1">
                  <label 
                    className={`p-4 rounded-2xl border cursor-pointer transition flex flex-col justify-between ${
                      myShareMode === 'PERSONAL_ONLY'
                        ? 'border-[#123C2D] bg-[#EBF4F0] ring-2 ring-[#123C2D]'
                        : 'border-[#E5DFD1] hover:border-[#123C2D]/40'
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <span className="font-bold text-xs text-[#123C2D]">ব্যক্তিগত (কোনো তথ্য শেয়ার নয়)</span>
                      <input
                        type="radio"
                        name="shareMode"
                        checked={myShareMode === 'PERSONAL_ONLY'}
                        onChange={() => setMyShareMode('PERSONAL_ONLY')}
                        className="mt-0.5 text-[#123C2D] focus:ring-[#123C2D]"
                      />
                    </div>
                    <span className="text-[11px] text-[#6B7870] mt-2 block">
                      স্কোয়াডে কেবল সদস্য হিসেবে নাম থাকবে, কোনো আমল তথ্য পৌঁছাবে না।
                    </span>
                  </label>

                  <label 
                    className={`p-4 rounded-2xl border cursor-pointer transition flex flex-col justify-between ${
                      myShareMode === 'AGGREGATE_ONLY'
                        ? 'border-[#123C2D] bg-[#EBF4F0] ring-2 ring-[#123C2D]'
                        : 'border-[#E5DFD1] hover:border-[#123C2D]/40'
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <span className="font-bold text-xs text-[#123C2D]">গড় পরিসংখ্যান (সুপারিশকৃত)</span>
                      <input
                        type="radio"
                        name="shareMode"
                        checked={myShareMode === 'AGGREGATE_ONLY'}
                        onChange={() => setMyShareMode('AGGREGATE_ONLY')}
                        className="mt-0.5 text-[#123C2D] focus:ring-[#123C2D]"
                      />
                    </div>
                    <span className="text-[11px] text-[#6B7870] mt-2 block">
                      কেবল স্কোয়াডের সামগ্রিক গড় শতাংশে গণনা হবে। ব্যক্তিগত স্ট্যাটাস কেউ দেখবে না।
                    </span>
                  </label>

                  <label 
                    className={`p-4 rounded-2xl border cursor-pointer transition flex flex-col justify-between ${
                      myShareMode === 'DETAILED'
                        ? 'border-[#123C2D] bg-[#EBF4F0] ring-2 ring-[#123C2D]'
                        : 'border-[#E5DFD1] hover:border-[#123C2D]/40'
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <span className="font-bold text-xs text-[#123C2D]">বিস্তারিত সালাত স্ট্যাটাস</span>
                      <input
                        type="radio"
                        name="shareMode"
                        checked={myShareMode === 'DETAILED'}
                        onChange={() => setMyShareMode('DETAILED')}
                        className="mt-0.5 text-[#123C2D] focus:ring-[#123C2D]"
                      />
                    </div>
                    <span className="text-[11px] text-[#6B7870] mt-2 block">
                      অনুমোদিত সদস্যরা কেবল সালাত সম্পন্ন হয়েছে কি না দেখতে পারবে (নোট ছাড়া)।
                    </span>
                  </label>
                </div>
              </div>

              {/* Member List in Warm White #FFFDF5 */}
              <div className="bg-[#FFFDF5] rounded-3xl p-6 border border-[#E5DFD1] shadow-sm space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="font-bold text-base text-[#123C2D]">স্কোয়াড সদস্যবৃন্দ ({members.length})</h3>
                  <span className="text-xs text-[#6B7870]">অনলাইন ও সক্রিয়</span>
                </div>

                <div className="divide-y divide-[#E5DFD1]">
                  {members.map((m) => (
                    <div key={m.uid} className="py-3.5 flex items-center justify-between">
                      <div className="flex items-center space-x-3">
                        <div className="w-10 h-10 rounded-2xl bg-[#EBF4F0] border border-[#123C2D]/20 flex items-center justify-center font-bold text-[#123C2D] text-sm shadow-sm">
                          {m.displayName.charAt(0)}
                        </div>
                        <div>
                          <div className="flex items-center space-x-2">
                            <span className="font-bold text-sm text-[#252B27]">{m.displayName}</span>
                            <span className={`text-[10px] px-2.5 py-0.5 rounded-full font-bold ${
                              m.role === 'SQUAD_LEADER'
                                ? 'bg-[#FBF6E9] text-[#A68532] border border-[#D4AF57]/40'
                                : m.role === 'MODERATOR'
                                ? 'bg-[#EBF4F0] text-[#123C2D] border border-[#123C2D]/20'
                                : 'bg-[#F2EFE4] text-[#434E47]'
                            }`}>
                              {m.role === 'SQUAD_LEADER' ? 'লিডার' : m.role === 'MODERATOR' ? 'মডারেটর' : 'মেম্বার'}
                            </span>
                          </div>
                          <span className="text-xs text-[#6B7870]">
                            শেয়ারিং মোড: {m.shareMode === 'PERSONAL_ONLY' ? 'ব্যক্তিগত' : m.shareMode === 'AGGREGATE_ONLY' ? 'গড় পরিসংখ্যান' : 'বিস্তারিত'}
                          </span>
                        </div>
                      </div>

                      <span className="text-xs font-semibold px-2.5 py-1 bg-[#EAF5EE] text-[#1E7E52] rounded-xl border border-[#1E7E52]/20">
                        সক্রিয়
                      </span>
                    </div>
                  ))}
                </div>

                <div className="pt-4 border-t border-[#E5DFD1] flex justify-end">
                  <button
                    onClick={handleLeaveSquad}
                    className="px-4 py-2 rounded-xl border border-[#9E4040]/30 text-[#9E4040] hover:bg-[#FDF0F0] text-xs font-semibold flex items-center space-x-1.5 transition"
                  >
                    <LogOut className="w-3.5 h-3.5" />
                    <span>স্কোয়াড ত্যাগ করুন</span>
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className="bg-[#FFFDF5] rounded-3xl p-8 border border-[#E5DFD1] text-center space-y-4 shadow-sm">
              <div className="w-16 h-16 bg-[#EBF4F0] border border-[#123C2D]/20 rounded-3xl flex items-center justify-center mx-auto text-[#123C2D]">
                <Users className="w-8 h-8" />
              </div>
              <h3 className="text-lg font-bold text-[#123C2D]">কোনো স্কোয়াডে যুক্ত নেই</h3>
              <p className="text-xs text-[#434E47] max-w-md mx-auto">
                তাহাজ্জুদ ও সালাতের জন্য বন্ধুদের সাথে একটি স্কোয়াডে যুক্ত হোন অথবা আপনার নিজস্ব পারিবারিক স্কোয়াড তৈরি করুন।
              </p>
              <div className="flex justify-center space-x-3 pt-2">
                <button
                  onClick={() => setActiveTab('join')}
                  className="px-5 py-2.5 bg-[#123C2D] text-[#FFFDF5] rounded-2xl text-xs font-bold hover:bg-[#0C291E] transition shadow"
                >
                  ইনভাইট কোড দিয়ে যুক্ত হোন
                </button>
                <button
                  onClick={() => setActiveTab('create')}
                  className="px-5 py-2.5 bg-[#FFFDF5] border border-[#E5DFD1] text-[#252B27] rounded-2xl text-xs font-bold hover:bg-[#F2EFE4] transition"
                >
                  নতুন স্কোয়াড খুলুন
                </button>
              </div>
            </div>
          )}
        </>
      )}

      {/* Tab 2: Join Squad */}
      {activeTab === 'join' && (
        <div className="max-w-lg mx-auto bg-[#FFFDF5] rounded-3xl p-6 md:p-8 border border-[#E5DFD1] shadow-sm space-y-5">
          <div className="flex items-center space-x-2 text-[#123C2D]">
            <Key className="w-5 h-5 text-[#123C2D]" />
            <h2 className="text-lg font-bold">ইনভাইট কোড দিয়ে স্কোয়াডে যোগ দিন</h2>
          </div>
          <p className="text-xs text-[#434E47]">
            স্কোয়াড লিডার বা সহপাঠীর কাছ থেকে প্রাপ্ত ৬ ডিজিটের ইনভাইট কোডটি নিচে দিন।
          </p>

          <form onSubmit={handleJoinSquad} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-[#434E47] mb-1">
                ইনভাইট কোড (Invite Code)
              </label>
              <input
                type="text"
                maxLength={10}
                placeholder="যেমন: TAHAJ7"
                value={inviteCodeInput}
                onChange={(e) => setInviteCodeInput(e.target.value.toUpperCase())}
                className="w-full px-4 py-3 border border-[#E5DFD1] bg-white rounded-2xl outline-none focus:outline-none focus:ring-0 focus:border-[#252B27] transition-colors duration-150 text-center text-xl font-mono tracking-widest uppercase font-black text-[#123C2D]"
                required
              />
            </div>

            <div className="p-3.5 bg-[#FBF6E9] border border-[#D4AF57]/40 rounded-2xl text-xs text-[#A68532] flex items-start space-x-2">
              <Info className="w-4 h-4 text-[#A68532] flex-shrink-0 mt-0.5" />
              <span>
                স্কোয়াডে যোগ দিলে আপনার নাম এবং বাছাইকৃত শেয়ারিং মোড অনুযায়ী গড় আমল সংরক্ষিত হবে। ব্যক্তিগত নোট সুরক্ষিত থাকবে।
              </span>
            </div>

            <button
              type="submit"
              className="w-full py-3 bg-[#123C2D] text-[#FFFDF5] rounded-2xl font-bold text-sm hover:bg-[#0C291E] transition shadow-md"
            >
              স্কোয়াডে যুক্ত হোন
            </button>
          </form>
        </div>
      )}

      {/* Tab 3: Create Squad */}
      {activeTab === 'create' && (
        <div className="max-w-lg mx-auto bg-[#FFFDF5] rounded-3xl p-6 md:p-8 border border-[#E5DFD1] shadow-sm space-y-5">
          <div className="flex items-center space-x-2 text-[#123C2D]">
            <Plus className="w-5 h-5 text-[#123C2D]" />
            <h2 className="text-lg font-bold">নতুন তাহাজ্জুদ স্কোয়াড তৈরি করুন</h2>
          </div>
          <p className="text-xs text-[#434E47]">
            একটি নতুন স্কোয়াড তৈরি করে বন্ধুদের ইনভাইট কোড দিন যাতে সবাই একসাথে তাহাজ্জুদ ও সালাতের আমল করতে পারেন।
          </p>

          <form onSubmit={handleCreateSquad} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-[#434E47] mb-1.5">
                স্কোয়াডের নাম *
              </label>
              <input
                type="text"
                placeholder="যেমন: ফজরের কাফেলা"
                value={newSquadName}
                onChange={(e) => setNewSquadName(e.target.value)}
                className="w-full px-4 py-3 border border-[#E5DFD1] bg-white rounded-2xl outline-none focus:outline-none focus:ring-0 focus:border-[#252B27] transition-colors duration-150 text-sm text-[#252B27]"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-[#434E47] mb-1.5">
                সংক্ষিপ্ত বিবরণ ও লক্ষ্য
              </label>
              <textarea
                rows={2}
                placeholder="নিয়মিত তাহাজ্জুদ ও ফজরের পারস্পরিক তাগিদ..."
                value={newSquadDesc}
                onChange={(e) => setNewSquadDesc(e.target.value)}
                className="w-full px-4 py-3 border border-[#E5DFD1] bg-white rounded-2xl outline-none focus:outline-none focus:ring-0 focus:border-[#252B27] transition-colors duration-150 text-sm text-[#252B27]"
              />
            </div>

            <div className="flex items-center space-x-3 p-3.5 bg-[#F7F5EC] border border-[#E5DFD1] rounded-2xl">
              <input
                type="checkbox"
                id="privateCheck"
                checked={isPrivate}
                onChange={(e) => setIsPrivate(e.target.checked)}
                className="rounded text-[#123C2D] focus:ring-[#123C2D]"
              />
              <label htmlFor="privateCheck" className="text-xs text-[#434E47] cursor-pointer">
                <span className="font-bold block text-[#123C2D]">শুধুমাত্র ইনভাইট কোড দিয়ে যোগদান</span>
                <span>অননুমোদিত কেউ এই স্কোয়াডে প্রবেশ করতে পারবে না।</span>
              </label>
            </div>

            <button
              type="submit"
              className="w-full py-3 bg-[#123C2D] text-[#FFFDF5] rounded-2xl font-bold text-sm hover:bg-[#0C291E] transition shadow-md"
            >
              স্কোয়াড তৈরি করুন ও ইনভাইট কোড নিন
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
