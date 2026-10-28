export type PrayerType = 'FAJR' | 'DHUHR' | 'ASR' | 'MAGHRIB' | 'ISHA';

export type PrayerStatus = 'NOT_RECORDED' | 'PERFORMED' | 'MISSED' | 'QAZA';

export interface PrayerRecord {
  id: string;
  userId: string;
  localDate: string; // YYYY-MM-DD
  prayerName: PrayerType;
  status: PrayerStatus;
  notes?: string;
  createdAt: number;
  updatedAt: number;
}

export interface TahajjudRecord {
  id: string;
  userId: string;
  localDate: string;
  performed: boolean;
  rakahs: number;
  startTime?: string;
  endTime?: string;
  notes?: string;
  createdAt: number;
  updatedAt: number;
}

export interface DhikrPreset {
  id: string;
  arabicText: string;
  bengaliTransliteration: string;
  bengaliMeaning: string;
  englishMeaning: string;
  reference: string;
  defaultTarget: number;
}

export interface DhikrRecord {
  id: string;
  userId: string;
  localDate: string;
  dhikrId: string;
  dhikrTitle: string;
  count: number;
  targetCount: number;
  createdAt: number;
}

export type AmalCategory = 'QURAN' | 'MORNING_ADHKAR' | 'EVENING_ADHKAR' | 'SADAQAH' | 'ISLAMIC_STUDY' | 'CUSTOM';

export type AmalStatus = 'NOT_RECORDED' | 'COMPLETED' | 'PARTIAL' | 'MISSED';

export interface AmalEntry {
  id: string;
  userId: string;
  localDate: string;
  category: AmalCategory;
  title: string;
  description?: string;
  targetCount: number;
  currentCount: number;
  unit: string;
  status: AmalStatus;
  notes?: string;
  isSharedWithSquad: boolean;
  createdAt: number;
  updatedAt: number;
}

export type SharingConsentScope = 'PERSONAL_ONLY' | 'AGGREGATE_ONLY' | 'DETAILED';

export type SquadRole = 'MEMBER' | 'SQUAD_LEADER' | 'MODERATOR' | 'SUPER_ADMIN';

export interface Squad {
  id: string;
  name: string;
  description: string;
  ownerUid: string;
  inviteCode: string;
  isPrivate: boolean;
  memberCount: number;
  createdAt: number;
}

export interface SquadMember {
  uid: string;
  squadId: string;
  displayName: string;
  role: SquadRole;
  joinedAt: number;
  shareMode: SharingConsentScope;
}

export interface UserProfile {
  uid: string;
  displayName: string;
  email: string;
  photoUrl?: string;
  timezone: string;
  language: 'bn' | 'en';
  consentGiven: boolean;
  consentScope: SharingConsentScope;
  consentTimestamp: number;
  tahajjudAlarmEnabled: boolean;
  tahajjudAlarmOffsetMinutes: number;
  calculationCity: string;
  latitude: number;
  longitude: number;
  isSuperAdmin: boolean;
  activeSquadId?: string;
}

export interface AdminAuditLog {
  id: string;
  adminUid: string;
  action: string;
  details: string;
  timestamp: number;
}
