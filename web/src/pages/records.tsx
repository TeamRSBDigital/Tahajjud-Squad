import React, { useState } from 'react';
import { 
  BarChart2, 
  Calendar, 
  Clock, 
  Moon, 
  BookOpen, 
  Award, 
  Download
} from 'lucide-react';

export default function RecordsPage() {
  const [timeRange, setTimeRange] = useState<'7days' | '30days' | 'all'>('7days');

  const pastDays = [
    {
      date: 'আজ (Today)',
      dateStr: '2026-10-28',
      fajr: 'PERFORMED',
      dhuhr: 'PERFORMED',
      asr: 'PERFORMED',
      maghrib: 'PERFORMED',
      isha: 'PENDING',
      tahajjud: true,
      tahajjudRakahs: 4,
      quranPages: 6,
      adhkarDone: true,
      sadaqahDone: true,
      score: 90,
    },
    {
      date: 'গতকাল (Yesterday)',
      dateStr: '2026-10-27',
      fajr: 'PERFORMED',
      dhuhr: 'PERFORMED',
      asr: 'PERFORMED',
      maghrib: 'PERFORMED',
      isha: 'PERFORMED',
      tahajjud: true,
      tahajjudRakahs: 8,
      quranPages: 10,
      adhkarDone: true,
      sadaqahDone: false,
      score: 100,
    },
    {
      date: '২৬ অক্টোবর',
      dateStr: '2026-10-26',
      fajr: 'PERFORMED',
      dhuhr: 'QAZA',
      asr: 'PERFORMED',
      maghrib: 'PERFORMED',
      isha: 'PERFORMED',
      tahajjud: false,
      tahajjudRakahs: 0,
      quranPages: 4,
      adhkarDone: true,
      sadaqahDone: true,
      score: 75,
    },
    {
      date: '২৫ অক্টোবর',
      dateStr: '2026-10-25',
      fajr: 'PERFORMED',
      dhuhr: 'PERFORMED',
      asr: 'PERFORMED',
      maghrib: 'PERFORMED',
      isha: 'PERFORMED',
      tahajjud: true,
      tahajjudRakahs: 4,
      quranPages: 5,
      adhkarDone: true,
      sadaqahDone: false,
      score: 95,
    },
    {
      date: '২৪ অক্টোবর',
      dateStr: '2026-10-24',
      fajr: 'MISSED',
      dhuhr: 'PERFORMED',
      asr: 'PERFORMED',
      maghrib: 'PERFORMED',
      isha: 'PERFORMED',
      tahajjud: false,
      tahajjudRakahs: 0,
      quranPages: 2,
      adhkarDone: false,
      sadaqahDone: false,
      score: 60,
    },
    {
      date: '২৩ অক্টোবর',
      dateStr: '2026-10-23',
      fajr: 'PERFORMED',
      dhuhr: 'PERFORMED',
      asr: 'PERFORMED',
      maghrib: 'PERFORMED',
      isha: 'PERFORMED',
      tahajjud: true,
      tahajjudRakahs: 6,
      quranPages: 8,
      adhkarDone: true,
      sadaqahDone: true,
      score: 100,
    },
    {
      date: '২২ অক্টোবর',
      dateStr: '2026-10-22',
      fajr: 'PERFORMED',
      dhuhr: 'PERFORMED',
      asr: 'PERFORMED',
      maghrib: 'PERFORMED',
      isha: 'PERFORMED',
      tahajjud: true,
      tahajjudRakahs: 4,
      quranPages: 7,
      adhkarDone: true,
      sadaqahDone: false,
      score: 95,
    },
  ];

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PERFORMED':
        return <span className="inline-flex items-center px-2 py-0.5 rounded-lg text-[11px] font-bold bg-[#EAF5EE] text-[#1E7E52] border border-[#1E7E52]/20">আদায়</span>;
      case 'QAZA':
        return <span className="inline-flex items-center px-2 py-0.5 rounded-lg text-[11px] font-bold bg-[#FAF2E2] text-[#B58428] border border-[#B58428]/20">কাযা</span>;
      case 'MISSED':
        return <span className="inline-flex items-center px-2 py-0.5 rounded-lg text-[11px] font-bold bg-[#FDF0F0] text-[#9E4040] border border-[#9E4040]/20">ছুটেছে</span>;
      default:
        return <span className="inline-flex items-center px-2 py-0.5 rounded-lg text-[11px] font-bold bg-[#F2EFE4] text-[#434E47] border border-[#E5DFD1]">বাকি</span>;
    }
  };

  return (
    <div className="space-y-6 text-[#252B27]">
      {/* Header in Emerald Green & Royal Gold */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-black text-[#123C2D] flex items-center space-x-2">
            <BarChart2 className="w-6 h-6 text-[#123C2D]" />
            <span>আমল ও সালাত রেকর্ডস</span>
          </h1>
          <p className="text-sm text-[#434E47] mt-1">
            আপনার দৈনন্দিন সালাত, তাহাজ্জুদ ও নেক আমলের সার্বিক অগ্রগতি
          </p>
        </div>

        {/* Time range toggle */}
        <div className="flex items-center space-x-1.5 bg-[#FFFDF5] border border-[#E5DFD1] rounded-2xl p-1 shadow-sm">
          <button
            onClick={() => setTimeRange('7days')}
            className={`px-3.5 py-1.5 text-xs font-bold rounded-xl transition ${
              timeRange === '7days' ? 'bg-[#123C2D] text-[#FFFDF5] shadow-sm' : 'text-[#434E47] hover:text-[#252B27]'
            }`}
          >
            গত ৭ দিন
          </button>
          <button
            onClick={() => setTimeRange('30days')}
            className={`px-3.5 py-1.5 text-xs font-bold rounded-xl transition ${
              timeRange === '30days' ? 'bg-[#123C2D] text-[#FFFDF5] shadow-sm' : 'text-[#434E47] hover:text-[#252B27]'
            }`}
          >
            গত ৩০ দিন
          </button>
          <button
            onClick={() => setTimeRange('all')}
            className={`px-3.5 py-1.5 text-xs font-bold rounded-xl transition ${
              timeRange === 'all' ? 'bg-[#123C2D] text-[#FFFDF5] shadow-sm' : 'text-[#434E47] hover:text-[#252B27]'
            }`}
          >
            সব রেকর্ড
          </button>
        </div>
      </div>

      {/* Aggregate KPI Summary Cards in Warm White #FFFDF5 */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-[#FFFDF5] rounded-3xl p-5 border border-[#E5DFD1] shadow-sm">
          <div className="flex items-center justify-between text-[#6B7870] text-xs">
            <span className="font-semibold">সালাত ধারাবাহিকতা</span>
            <Clock className="w-4 h-4 text-[#123C2D]" />
          </div>
          <div className="text-2xl font-black text-[#123C2D] mt-2">৯১%</div>
          <div className="text-[11px] text-[#123C2D] font-bold mt-1">৩৩ / ৩৫ ওয়াক্ত আদায়</div>
        </div>

        <div className="bg-[#FFFDF5] rounded-3xl p-5 border border-[#E5DFD1] shadow-sm">
          <div className="flex items-center justify-between text-[#6B7870] text-xs">
            <span className="font-semibold">তাহাজ্জুদ স্ট্রিক</span>
            <Moon className="w-4 h-4 text-[#D4AF57]" />
          </div>
          <div className="text-2xl font-black text-[#123C2D] mt-2">৫ রাত</div>
          <div className="text-[11px] text-[#434E47] mt-1">মোট ২৬ রাকাত আদায়</div>
        </div>

        <div className="bg-[#FFFDF5] rounded-3xl p-5 border border-[#E5DFD1] shadow-sm">
          <div className="flex items-center justify-between text-[#6B7870] text-xs">
            <span className="font-semibold">কুরআন তিলাওয়াত</span>
            <BookOpen className="w-4 h-4 text-[#123C2D]" />
          </div>
          <div className="text-2xl font-black text-[#123C2D] mt-2">৪২ পৃষ্ঠা</div>
          <div className="text-[11px] text-[#123C2D] font-bold mt-1">দৈনিক গড় ৬ পৃষ্ঠা</div>
        </div>

        <div className="bg-[#FFFDF5] rounded-3xl p-5 border border-[#E5DFD1] shadow-sm">
          <div className="flex items-center justify-between text-[#6B7870] text-xs">
            <span className="font-semibold">আমল স্কোর</span>
            <Award className="w-4 h-4 text-[#D4AF57]" />
          </div>
          <div className="text-2xl font-black text-[#123C2D] mt-2">৮৮ / ১০০</div>
          <div className="text-[11px] text-[#1E7E52] font-bold mt-1">মাশাআল্লাহ ممتاز</div>
        </div>
      </div>

      {/* Daily Breakdown Table in Warm White #FFFDF5 */}
      <div className="bg-[#FFFDF5] rounded-3xl border border-[#E5DFD1] shadow-sm overflow-hidden">
        <div className="p-5 border-b border-[#E5DFD1] flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center space-x-2">
            <Calendar className="w-4 h-4 text-[#123C2D]" />
            <h3 className="font-bold text-sm text-[#123C2D]">তারিখ অনুযায়ী বিস্তারিত আমল তালিকা</h3>
          </div>

          <button
            onClick={() => {
              const header = 'তারিখ,ফজর,যোহর,আসর,মাগরিব,ইশা,তাহাজ্জুদ রাকাত,কুরআন পৃষ্ঠা,আমল স্কোর\n';
              const rows = pastDays.map(d => `${d.dateStr},${d.fajr},${d.dhuhr},${d.asr},${d.maghrib},${d.isha},${d.tahajjudRakahs},${d.quranPages},${d.score}%`).join('\n');
              const blob = new Blob([header + rows], { type: 'text/csv;charset=utf-8;' });
              const url = URL.createObjectURL(blob);
              const a = document.createElement('a');
              a.href = url;
              a.download = `amal_records_${Date.now()}.csv`;
              a.click();
            }}
            className="inline-flex items-center space-x-1.5 px-3.5 py-1.5 text-xs font-bold rounded-xl bg-[#EBF4F0] text-[#123C2D] border border-[#123C2D]/30 hover:bg-[#D4EADE] transition shadow-sm"
          >
            <Download className="w-3.5 h-3.5 text-[#123C2D]" />
            <span>CSV ডাউনলোড</span>
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-[#F7F5EC] border-b border-[#E5DFD1] text-[#434E47] font-bold">
                <th className="py-3 px-4">তারিখ</th>
                <th className="py-3 px-2">ফজর</th>
                <th className="py-3 px-2">যোহর</th>
                <th className="py-3 px-2">আসর</th>
                <th className="py-3 px-2">মাগরিব</th>
                <th className="py-3 px-2">ইশা</th>
                <th className="py-3 px-3">তাহাজ্জুদ</th>
                <th className="py-3 px-3">কুরআন</th>
                <th className="py-3 px-4 text-right">দৈনিক স্কোর</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E5DFD1] text-[#252B27]">
              {pastDays.map((row, idx) => (
                <tr key={idx} className="hover:bg-[#FBF6E9]/60 transition">
                  <td className="py-3.5 px-4 font-bold text-[#123C2D]">
                    {row.date}
                  </td>
                  <td className="py-3.5 px-2">{getStatusBadge(row.fajr)}</td>
                  <td className="py-3.5 px-2">{getStatusBadge(row.dhuhr)}</td>
                  <td className="py-3.5 px-2">{getStatusBadge(row.asr)}</td>
                  <td className="py-3.5 px-2">{getStatusBadge(row.maghrib)}</td>
                  <td className="py-3.5 px-2">{getStatusBadge(row.isha)}</td>
                  <td className="py-3.5 px-3">
                    {row.tahajjud ? (
                      <span className="text-[#1E7E52] font-bold">{row.tahajjudRakahs} রাকাত</span>
                    ) : (
                      <span className="text-[#6B7870]">-</span>
                    )}
                  </td>
                  <td className="py-3.5 px-3 font-semibold">
                    {row.quranPages > 0 ? `${row.quranPages} পৃষ্ঠা` : '-'}
                  </td>
                  <td className="py-3.5 px-4 text-right">
                    <span className={`font-black ${
                      row.score >= 90 ? 'text-[#1E7E52]' : row.score >= 75 ? 'text-[#B58428]' : 'text-[#9E4040]'
                    }`}>
                      {row.score}%
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
