import React, { useState } from 'react';
import { BookOpen, Plus, Sparkles } from 'lucide-react';
import { AmalEntry, AmalStatus } from '@/types';

export default function AmalTrackerPage() {
  const [entries, setEntries] = useState<AmalEntry[]>([
    {
      id: 'amal_1',
      userId: 'usr_web',
      localDate: '2026-10-28',
      category: 'QURAN',
      title: 'কুরআন তিলাওয়াত (Quran Recitation)',
      description: 'অর্থসহ অনুধাবন ও ৫ পৃষ্ঠা তিলাওয়াত',
      targetCount: 5,
      currentCount: 5,
      unit: 'পৃষ্ঠা',
      status: 'COMPLETED',
      isSharedWithSquad: true,
      createdAt: Date.now(),
      updatedAt: Date.now()
    },
    {
      id: 'amal_2',
      userId: 'usr_web',
      localDate: '2026-10-28',
      category: 'MORNING_ADHKAR',
      title: 'সকালের মাসনূন আযকার (Morning Adhkar)',
      description: 'সহীহ হাদীস বর্ণিত সকালের হেফাজত ও যিকিরসমূহ',
      targetCount: 1,
      currentCount: 1,
      unit: 'সেশন',
      status: 'COMPLETED',
      isSharedWithSquad: true,
      createdAt: Date.now(),
      updatedAt: Date.now()
    },
    {
      id: 'amal_3',
      userId: 'usr_web',
      localDate: '2026-10-28',
      category: 'EVENING_ADHKAR',
      title: 'সন্ধ্যার মাসনূন আযকার (Evening Adhkar)',
      description: 'মাগরিবের পূর্ববর্তী ও পরবর্তী দুআ ও সুরক্ষামূলক আযকার',
      targetCount: 1,
      currentCount: 0,
      unit: 'সেশন',
      status: 'NOT_RECORDED',
      isSharedWithSquad: false,
      createdAt: Date.now(),
      updatedAt: Date.now()
    },
    {
      id: 'amal_4',
      userId: 'usr_web',
      localDate: '2026-10-28',
      category: 'SADAQAH',
      title: 'দৈনিক সাদাকাহ (Daily Charity)',
      description: 'আল্লাহর সন্তুষ্টিতে কাউকে সাহায্য বা গোপনে দান',
      targetCount: 1,
      currentCount: 1,
      unit: 'বার',
      status: 'COMPLETED',
      isSharedWithSquad: false,
      createdAt: Date.now(),
      updatedAt: Date.now()
    },
    {
      id: 'amal_5',
      userId: 'usr_web',
      localDate: '2026-10-28',
      category: 'ISLAMIC_STUDY',
      title: 'দ্বীনি ইলম অধ্যয়ন (Islamic Knowledge)',
      description: 'হাদীস বা তাফসীর গ্রন্থ অধ্যয়ন (২০ মিনিট)',
      targetCount: 20,
      currentCount: 20,
      unit: 'মিনিট',
      status: 'COMPLETED',
      isSharedWithSquad: true,
      createdAt: Date.now(),
      updatedAt: Date.now()
    }
  ]);

  const [showAddModal, setShowAddModal] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newTarget, setNewTarget] = useState(1);
  const [newUnit, setNewUnit] = useState('বার');

  const updateStatus = (id: string, s: AmalStatus) => {
    setEntries(prev => prev.map(e => e.id === id ? { ...e, status: s } : e));
  };

  const handleAdd = () => {
    if (!newTitle) return;
    const newEntry: AmalEntry = {
      id: 'amal_' + Date.now(),
      userId: 'usr_web',
      localDate: '2026-10-28',
      category: 'CUSTOM',
      title: newTitle,
      targetCount: newTarget,
      currentCount: 0,
      unit: newUnit,
      status: 'NOT_RECORDED',
      isSharedWithSquad: false,
      createdAt: Date.now(),
      updatedAt: Date.now()
    };
    setEntries([...entries, newEntry]);
    setNewTitle('');
    setShowAddModal(false);
  };

  const completed = entries.filter(e => e.status === 'COMPLETED').length;

  return (
    <div className="space-y-6">
      {/* Header in Emerald Green & Royal Gold */}
      <div className="bg-gradient-to-r from-[#0C291E] via-[#123C2D] to-[#1A5641] rounded-3xl p-6 md:p-8 text-[#FFFDF5] border border-[#D4AF57]/40 shadow-xl flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 text-[#D4AF57]">
            <Sparkles className="w-5 h-5 text-[#D4AF57]" />
            <span className="text-xs font-semibold uppercase tracking-wider">দৈনন্দিন নেক আমল</span>
          </div>
          <h1 className="text-2xl font-black text-[#FFFDF5] mt-1">দৈনন্দিন আমল ট্র্যাকার</h1>
          <p className="text-sm text-[#D8E2DC]">কুরআন, আযকার, সাদাকাহ ও নেক আমলের ধারাবাহিকতা</p>
        </div>
        <button
          onClick={() => setShowAddModal(true)}
          className="px-4 py-2.5 bg-[#D4AF57] hover:bg-[#E5C678] text-[#123C2D] font-bold rounded-2xl flex items-center space-x-2 transition shadow-md"
        >
          <Plus className="w-4 h-4 text-[#123C2D]" />
          <span>নতুন আমল যোগ করুন</span>
        </button>
      </div>

      {/* Progress pill in Warm White #FFFDF5 */}
      <div className="bg-[#FFFDF5] rounded-2xl p-5 border border-[#E5DFD1] shadow-sm flex items-center justify-between">
        <div>
          <span className="text-sm font-bold text-[#123C2D]">আজকের অগ্রগতি: {completed} / {entries.length} সম্পন্ন</span>
          <p className="text-xs text-[#6B7870] mt-0.5">ছোট ছোট ভালো কাজও নিয়মিত আদায় করুন</p>
        </div>
        <div className="w-36 bg-[#F2EFE4] h-3 rounded-full overflow-hidden border border-[#E5DFD1]">
          <div
            className="bg-[#123C2D] h-full rounded-full transition-all duration-300"
            style={{ width: `${(completed / entries.length) * 100}%` }}
          />
        </div>
      </div>

      {/* Entries List */}
      <div className="space-y-3">
        {entries.map(entry => {
          const isDone = entry.status === 'COMPLETED';
          return (
            <div key={entry.id} className="bg-[#FFFDF5] rounded-2xl p-5 border border-[#E5DFD1] shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <h3 className="font-bold text-[#123C2D] text-base">{entry.title}</h3>
                {entry.description && <p className="text-xs text-[#6B7870] mt-0.5">{entry.description}</p>}
                <span className="text-xs text-[#A68532] font-bold block mt-1">
                  লক্ষ্য: {entry.targetCount} {entry.unit}
                </span>
              </div>

              <div className="flex items-center space-x-2">
                <button
                  onClick={() => updateStatus(entry.id, 'COMPLETED')}
                  className={`px-3.5 py-1.5 text-xs font-semibold rounded-xl border transition ${
                    isDone 
                      ? 'bg-[#123C2D] text-[#FFFDF5] border-[#123C2D] shadow font-bold' 
                      : 'bg-[#EAF5EE] text-[#1E7E52] border-[#1E7E52]/20 hover:bg-[#D4EADE]'
                  }`}
                >
                  সম্পন্ন
                </button>
                <button
                  onClick={() => updateStatus(entry.id, 'PARTIAL')}
                  className={`px-3.5 py-1.5 text-xs font-semibold rounded-xl border transition ${
                    entry.status === 'PARTIAL' 
                      ? 'bg-[#D4AF57] text-[#123C2D] border-[#D4AF57] shadow font-bold' 
                      : 'bg-[#FAF2E2] text-[#B58428] border-[#B58428]/20 hover:bg-[#F5E6C7]'
                  }`}
                >
                  আংশিক
                </button>
                <button
                  onClick={() => updateStatus(entry.id, 'MISSED')}
                  className={`px-3.5 py-1.5 text-xs font-semibold rounded-xl border transition ${
                    entry.status === 'MISSED' 
                      ? 'bg-[#9E4040] text-[#FFFDF5] border-[#9E4040] shadow font-bold' 
                      : 'bg-[#FDF0F0] text-[#9E4040] border-[#9E4040]/20 hover:bg-[#FCE0E0]'
                  }`}
                >
                  ছুটেছে
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* Add Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#FFFDF5] rounded-3xl max-w-md w-full p-6 shadow-2xl border border-[#E5DFD1] space-y-4 text-[#252B27]">
            <h3 className="text-lg font-bold text-[#123C2D]">নতুন আমল লক্ষ্য যোগ করুন</h3>
            <div>
              <label className="block text-xs font-bold text-[#434E47] mb-1.5">আমলের নাম:</label>
              <input
                type="text"
                value={newTitle}
                onChange={e => setNewTitle(e.target.value)}
                placeholder="যেমন: সূরা হাশরের শেষ ৩ আয়াত"
                className="w-full px-4 py-3 border border-[#E5DFD1] bg-white rounded-2xl text-sm text-[#252B27] outline-none focus:outline-none focus:ring-0 focus:border-[#252B27] transition-colors duration-150"
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-bold text-[#434E47] mb-1.5">দৈনিক লক্ষ্য:</label>
                <input
                  type="number"
                  value={newTarget}
                  onChange={e => setNewTarget(parseInt(e.target.value) || 1)}
                  className="w-full px-4 py-3 border border-[#E5DFD1] bg-white rounded-2xl text-sm text-[#252B27] outline-none focus:outline-none focus:ring-0 focus:border-[#252B27] transition-colors duration-150"
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-[#434E47] mb-1.5">একক:</label>
                <input
                  type="text"
                  value={newUnit}
                  onChange={e => setNewUnit(e.target.value)}
                  className="w-full px-4 py-3 border border-[#E5DFD1] bg-white rounded-2xl text-sm text-[#252B27] outline-none focus:outline-none focus:ring-0 focus:border-[#252B27] transition-colors duration-150"
                />
              </div>
            </div>
            <div className="flex justify-end space-x-2 pt-2">
              <button onClick={() => setShowAddModal(false)} className="px-4 py-2 text-sm text-[#6B7870] hover:bg-[#F2EFE4] rounded-2xl transition">বাতিল</button>
              <button onClick={handleAdd} className="px-5 py-2 bg-[#123C2D] hover:bg-[#0C291E] text-[#FFFDF5] font-bold rounded-2xl transition shadow">সংরক্ষণ</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
