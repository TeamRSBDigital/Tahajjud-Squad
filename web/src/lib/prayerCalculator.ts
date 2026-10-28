export interface DayPrayerTimes {
  fajr: Date;
  sunrise: Date;
  dhuhr: Date;
  asr: Date;
  maghrib: Date;
  isha: Date;
  tahajjudReminder: Date;
}

export function calculatePrayerTimes(
  date: Date = new Date(),
  latitude: number = 23.8103, // Dhaka default
  longitude: number = 90.4125,
  fajrAngle: number = 18.0,
  ishaAngle: number = 18.0,
  tahajjudOffsetMinutes: number = 30
): DayPrayerTimes {
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const tz = -date.getTimezoneOffset() / 60;

  // Julian date
  let y = year;
  let m = month;
  if (m <= 2) {
    y -= 1;
    m += 12;
  }
  const a = Math.floor(y / 100);
  const b = 2 - a + Math.floor(a / 4);
  const jd = Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + day + b - 1524.5 - (longitude / (15.0 * 24.0));

  const d = jd - 2451545.0;
  const g = fixAngle(357.529 + 0.98560028 * d);
  const q = fixAngle(280.459 + 0.98564736 * d);
  const l = fixAngle(q + 1.915 * sinD(g) + 0.020 * sinD(2 * g));
  const e = 23.439 - 0.00000036 * d;
  const ra = fixAngle(toDeg(Math.atan2(cosD(e) * sinD(l), cosD(l)))) / 15.0;
  const declination = toDeg(Math.asin(sinD(e) * sinD(l)));
  const eqt = q / 15.0 - fixHour(ra);

  const noon = fixHour(12.0 + tz - longitude / 15.0 - eqt);
  const sunAlt = -0.8333;
  const sunriseHour = noon - sunAngleHour(sunAlt, latitude, declination);
  const sunsetHour = noon + sunAngleHour(sunAlt, latitude, declination);

  const fajrHour = noon - sunAngleHour(-fajrAngle, latitude, declination);
  const asrShadow = 1.0;
  const asrAlt = toDeg(Math.atan(1.0 / (asrShadow + Math.tan(toRad(Math.abs(latitude - declination))))));
  const asrHour = noon + sunAngleHour(asrAlt, latitude, declination);
  const maghribHour = sunsetHour;
  const ishaHour = noon + sunAngleHour(-ishaAngle, latitude, declination);

  const fajrDate = hourToDate(date, fajrHour);
  const sunriseDate = hourToDate(date, sunriseHour);
  const dhuhrDate = hourToDate(date, noon);
  const asrDate = hourToDate(date, asrHour);
  const maghribDate = hourToDate(date, maghribHour);
  const ishaDate = hourToDate(date, ishaHour);

  const tahajjudReminder = new Date(fajrDate.getTime() - tahajjudOffsetMinutes * 60 * 1000);

  return {
    fajr: fajrDate,
    sunrise: sunriseDate,
    dhuhr: dhuhrDate,
    asr: asrDate,
    maghrib: maghribDate,
    isha: ishaDate,
    tahajjudReminder
  };
}

export function getNextUpcomingTahajjud(
  latitude: number = 23.8103,
  longitude: number = 90.4125,
  offsetMinutes: number = 30
): { fajr: Date; tahajjud: Date } {
  const now = new Date();
  const todayTimes = calculatePrayerTimes(now, latitude, longitude, 18.0, 18.0, offsetMinutes);

  if (todayTimes.fajr.getTime() > now.getTime()) {
    return { fajr: todayTimes.fajr, tahajjud: todayTimes.tahajjudReminder };
  } else {
    const tomorrow = new Date(now.getTime() + 24 * 60 * 60 * 1000);
    const tomorrowTimes = calculatePrayerTimes(tomorrow, latitude, longitude, 18.0, 18.0, offsetMinutes);
    return { fajr: tomorrowTimes.fajr, tahajjud: tomorrowTimes.tahajjudReminder };
  }
}

export function formatTimeAmPm(date: Date): string {
  let hours = date.getHours();
  const minutes = date.getMinutes();
  const ampm = hours >= 12 ? 'PM' : 'AM';
  hours = hours % 12;
  hours = hours ? hours : 12;
  const strMinutes = minutes < 10 ? '0' + minutes : minutes;
  const strHours = hours < 10 ? '0' + hours : hours;
  return `${strHours}:${strMinutes} ${ampm}`;
}

function hourToDate(baseDate: Date, decimalHour: number): Date {
  const d = new Date(baseDate);
  const h = Math.floor(decimalHour);
  const mDec = (decimalHour - h) * 60;
  const m = Math.floor(mDec);
  const s = Math.floor((mDec - m) * 60);
  d.setHours((h % 24 + 24) % 24, m, s, 0);
  return d;
}

function sunAngleHour(angle: number, lat: number, dec: number): number {
  const cosH = (sinD(angle) - sinD(lat) * sinD(dec)) / (cosD(lat) * cosD(dec));
  if (cosH > 1.0) return 0.0;
  if (cosH < -1.0) return 12.0;
  return toDeg(Math.acos(cosH)) / 15.0;
}

function fixAngle(a: number): number {
  let ang = a - 360.0 * Math.floor(a / 360.0);
  if (ang < 0) ang += 360.0;
  return ang;
}

function fixHour(h: number): number {
  let hour = h - 24.0 * Math.floor(h / 24.0);
  if (hour < 0) hour += 24.0;
  return hour;
}

function toRad(deg: number): number { return deg * Math.PI / 180.0; }
function toDeg(rad: number): number { return rad * 180.0 / Math.PI; }
function sinD(deg: number): number { return Math.sin(toRad(deg)); }
function cosD(deg: number): number { return Math.cos(toRad(deg)); }
