import React, { useState } from 'react';
import { Calendar, ChevronLeft, ChevronRight, CheckCircle2, Clock } from 'lucide-react';
import { PrayerType, PrayerStatus } from '@/types';

export default function PrayerPage() {
  const [date, setDate] = useState('2026-10-28');
  const [prayers, setPrayers] = useState<Record<PrayerType, { status: PrayerStatus; notes: string }>>({
    FAJR: { status: 'PERFORMED', notes: 'জামাতে আদায় করেছি' },
    DHUHR: { status: 'PERFORMED', notes: '' },
    ASR: { status: 'NOT_RECORDED', notes: '' },
    MAGHRIB: { status: 'NOT_RECORDED', notes: '' },
    ISHA: { status: 'NOT_RECORDED', notes: '' },
  });

  const list: { type: PrayerType; bn: string; ar: string; time: string }[] = [
    { type: 'FAJR', bn: 'ফজর', ar: 'الفجر', time: '০৪:৪৫ AM' },
    { type: 'DHUHR', bn: 'যোহর', ar: 'الظهر', time: '১২:০৫ PM' },
    { type: 'ASR', bn: 'আসর', ar: 'العصر', time: '০৪:১৫ PM' },
    { type: 'MAGHRIB', bn: 'মাগরিব', ar: 'المغرب', time: '০৫:৪০ PM' },
    { type: 'ISHA', bn: 'ইশা', ar: 'العشاء', time: '০৭:০০ PM' },
  ];

  const setStatus = (p: PrayerType, s: PrayerStatus) => {
    setPrayers(prev => ({
      ...prev,
      [p]: { ...prev[p], status: s }
    }));
  };

  const doneCount = Object.values(prayers).filter(x => x.status === 'PERFORMED').length;
  const missedCount = Object.values(prayers).filter(x => x.status === 'MISSED').length;
  const qazaCount = Object.values(prayers).filter(x => x.status === 'QAZA').length;

  return (
    <div className="space-y-6">
      {/* Header in Emerald Green #123C2D with Royal Gold #D4AF57 Accents */}
      <div className="bg-gradient-to-r from-[#0C291E] via-[#123C2D] to-[#1A5641] rounded-3xl p-6 text-[#FFFDF5] border border-[#D4AF57]/40 shadow-xl flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 text-[#D4AF57]">
            <Clock className="w-5 h-5 text-[#D4AF57]" />
            <span className="text-xs font-semibold uppercase tracking-wider">দৈনিক সালাত ট্র্যাকার</span>
          </div>
          <h1 className="text-2xl font-black text-[#FFFDF5] mt-1">পাঁচ ওয়াক্ত ফরজ সালাত</h1>
          <p className="text-sm text-[#D8E2DC]">ওয়াক্তভিত্তিক নির্ভুল হিসাব ও পর্যবেক্ষণ</p>
        </div>

        {/* Date Navigator */}
        <div className="flex items-center space-x-2 bg-[#0C291E]/80 backdrop-blur-md px-4 py-2 rounded-2xl border border-[#D4AF57]/40">
          <button className="p-1 text-[#FFFDF5] hover:text-[#D4AF57] transition">
            <ChevronLeft className="w-4 h-4" />
          </button>
          <span className="text-sm font-bold text-[#FFFDF5] tracking-wide">{date}</span>
          <button className="p-1 text-[#FFFDF5] hover:text-[#D4AF57] transition">
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Summary Stat Cards in Warm White #FFFDF5 */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="bg-[#FFFDF5] border border-[#E5DFD1] rounded-2xl p-4 text-center shadow-sm">
          <span className="text-2xl font-black text-[#1E7E52]">{doneCount}</span>
          <span className="block text-xs text-[#252B27] font-semibold mt-0.5">আদায়কৃত ওয়াক্ত</span>
        </div>
        <div className="bg-[#FFFDF5] border border-[#E5DFD1] rounded-2xl p-4 text-center shadow-sm">
          <span className="text-2xl font-black text-[#B58428]">{qazaCount}</span>
          <span className="block text-xs text-[#252B27] font-semibold mt-0.5">কাযা আদায়</span>
        </div>
        <div className="bg-[#FFFDF5] border border-[#E5DFD1] rounded-2xl p-4 text-center shadow-sm">
          <span className="text-2xl font-black text-[#9E4040]">{missedCount}</span>
          <span className="block text-xs text-[#252B27] font-semibold mt-0.5">ছুটে গেছে</span>
        </div>
        <div className="bg-[#FFFDF5] border border-[#E5DFD1] rounded-2xl p-4 text-center shadow-sm">
          <span className="text-2xl font-black text-[#252B27]">{5 - (doneCount + missedCount + qazaCount)}</span>
          <span className="block text-xs text-[#6B7870] font-semibold mt-0.5">বাকি ওয়াক্ত</span>
        </div>
      </div>

      {/* 5 Prayers List in Warm White #FFFDF5 Cards */}
      <div className="space-y-3">
        {list.map(item => {
          const current = prayers[item.type];
          return (
            <div key={item.type} className="bg-[#FFFDF5] rounded-2xl p-5 border border-[#E5DFD1] shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div>
                <div className="flex items-center space-x-3">
                  <h3 className="text-lg font-bold text-[#123C2D]">{item.bn}</h3>
                  <span className="text-base font-serif font-bold text-[#D4AF57]">{item.ar}</span>
                  <span className="text-xs bg-[#F2EFE4] text-[#434E47] px-2.5 py-0.5 rounded-full font-medium border border-[#E5DFD1]">
                    {item.time}
                  </span>
                </div>
                {current.notes && (
                  <p className="text-xs text-[#6B7870] mt-1">নোট: {current.notes}</p>
                )}
              </div>

              {/* Status Buttons */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                {(['PERFORMED', 'QAZA', 'MISSED', 'NOT_RECORDED'] as PrayerStatus[]).map(status => {
                  const isSel = current.status === status;
                  const labelMap: Record<PrayerStatus, string> = {
                    PERFORMED: 'আদায়কৃত',
                    QAZA: 'কাযা আদায়',
                    MISSED: 'ছুটে গেছে',
                    NOT_RECORDED: 'অনির্ধারিত'
                  };
                  const colorMap: Record<PrayerStatus, string> = {
                    PERFORMED: isSel 
                      ? 'bg-[#123C2D] text-[#FFFDF5] border-[#123C2D] shadow font-bold' 
                      : 'bg-[#EAF5EE] text-[#1E7E52] border-[#1E7E52]/20 hover:bg-[#D4EADE]',
                    QAZA: isSel 
                      ? 'bg-[#D4AF57] text-[#123C2D] border-[#D4AF57] shadow font-bold' 
                      : 'bg-[#FAF2E2] text-[#B58428] border-[#B58428]/20 hover:bg-[#F5E6C7]',
                    MISSED: isSel 
                      ? 'bg-[#9E4040] text-[#FFFDF5] border-[#9E4040] shadow font-bold' 
                      : 'bg-[#FDF0F0] text-[#9E4040] border-[#9E4040]/20 hover:bg-[#FCE0E0]',
                    NOT_RECORDED: isSel 
                      ? 'bg-[#252B27] text-[#FFFDF5] border-[#252B27] shadow font-bold' 
                      : 'bg-[#F2EFE4] text-[#434E47] border-[#E5DFD1] hover:bg-[#E5DFD1]'
                  };

                  return (
                    <button
                      key={status}
                      onClick={() => setStatus(item.type, status)}
                      className={`px-3 py-2 text-xs font-semibold rounded-xl border transition ${colorMap[status]}`}
                    >
                      {labelMap[status]}
                    </button>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
