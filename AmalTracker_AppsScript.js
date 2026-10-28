/**
 * AMAL TRACKER & TAHAJJUD SQUAD — Google Apps Script Centralized Reporting Backend
 * 
 * Deployment Instructions:
 * 1. Open Google Sheets (create a new private Google Sheet).
 * 2. Click Extensions > Apps Script.
 * 3. Replace all code in Code.gs with this file content.
 * 4. Set the AUTH_TOKEN in the Script Properties (Project Settings > Script Properties)
 *    or set it in the config object below.
 * 5. Click "Deploy" > "New Deployment" > Type: "Web app"
 *    - Execute as: "Me" (your Google account)
 *    - Who has access: "Anyone" (requests are authenticated via the secure bearer token/API key)
 * 6. Copy the Web App URL and paste it into the Super Admin settings in Amal Tracker app / web app.
 */

const CONFIG = {
  DEFAULT_TOKEN: "AMAL_SECURE_TOKEN_2026",
  TABS: {
    USERS: "Users",
    PRAYER_RECORDS: "Prayer Records",
    TAHAJJUD_RECORDS: "Tahajjud Records",
    DHIKR_SESSIONS: "Dhikr Sessions",
    AMAL_ENTRIES: "Amal Entries",
    SQUAD_SUMMARIES: "Squad Summaries",
    DAILY_SUMMARY: "Daily Summary",
    MONTHLY_SUMMARY: "Monthly Summary",
    SYNC_ERRORS: "Sync Errors",
    AUDIT_LOG: "Admin Audit Log"
  }
};

/**
 * Handle incoming POST requests from Amal Tracker Android and Web Clients
 */
function doPost(e) {
  try {
    if (!e || !e.postData || !e.postData.contents) {
      return jsonResponse({ status: "error", message: "Empty request payload" }, 400);
    }

    const payload = JSON.parse(e.postData.contents);
    const token = payload.token || (e.parameter && e.parameter.token);

    const expectedToken = PropertiesService.getScriptProperties().getProperty("AUTH_TOKEN") || CONFIG.DEFAULT_TOKEN;
    if (token !== expectedToken) {
      logSyncError("AUTHENTICATION_FAILED", "Invalid security token provided");
      return jsonResponse({ status: "error", message: "Unauthorized. Invalid token." }, 401);
    }

    const ss = SpreadsheetApp.getActiveSpreadsheet();
    ensureTabsExist(ss);

    const action = payload.action;
    let result = {};

    switch (action) {
      case "SYNC_BATCH":
        result = handleBatchSync(ss, payload.data);
        break;
      case "RECORD_AUDIT":
        result = handleAuditLog(ss, payload.audit);
        break;
      case "TEST_CONNECTION":
        result = { status: "success", message: "Connection verified with Amal Tracker & Tahajjud Squad backend." };
        break;
      default:
        return jsonResponse({ status: "error", message: "Unknown action: " + action }, 400);
    }

    return jsonResponse({ status: "success", result: result });

  } catch (err) {
    logSyncError("EXCEPTION", err.toString());
    return jsonResponse({ status: "error", message: err.toString() }, 500);
  }
}

/**
 * Handle GET requests for health check and status
 */
function doGet(e) {
  return jsonResponse({
    status: "active",
    system: "Amal Tracker & Tahajjud Squad Reporting Engine",
    version: "2.0",
    time: new Date().toISOString()
  });
}

function handleBatchSync(ss, data) {
  if (!data) return { synced: 0 };
  let count = 0;

  // 1. Sync User profile with granular consent scope
  if (data.user) {
    upsertRow(ss.getSheetByName(CONFIG.TABS.USERS), 0, data.user.uid, [
      data.user.uid,
      data.user.displayName || "Anonymous",
      data.user.email || "",
      data.user.timezone || "Asia/Dhaka",
      data.user.consentGiven ? "CONSENTED" : "WITHDRAWN",
      data.user.consentScope || "AGGREGATE_ONLY",
      data.user.activeSquadId || "None",
      new Date().toISOString()
    ]);
    count++;
  }

  // 2. Sync Prayer Records (Deduplicate by Record ID)
  if (data.prayers && Array.isArray(data.prayers)) {
    const prayerSheet = ss.getSheetByName(CONFIG.TABS.PRAYER_RECORDS);
    data.prayers.forEach(p => {
      upsertRow(prayerSheet, 0, p.id, [
        p.id,
        p.userId,
        p.localDate,
        p.prayerName,
        p.status,
        p.notes || "",
        new Date(p.createdAt || Date.now()).toISOString(),
        new Date(p.updatedAt || Date.now()).toISOString()
      ]);
      count++;
    });
  }

  // 3. Sync Tahajjud Records (Deduplicate by Record ID)
  if (data.tahajjud && Array.isArray(data.tahajjud)) {
    const tahajjudSheet = ss.getSheetByName(CONFIG.TABS.TAHAJJUD_RECORDS);
    data.tahajjud.forEach(t => {
      upsertRow(tahajjudSheet, 0, t.id, [
        t.id,
        t.userId,
        t.localDate,
        t.performed ? "YES" : "NO",
        t.rakahs || 0,
        t.startTime || "",
        t.endTime || "",
        t.notes || "",
        new Date(t.createdAt || Date.now()).toISOString(),
        new Date(t.updatedAt || Date.now()).toISOString()
      ]);
      count++;
    });
  }

  // 4. Sync Dhikr Sessions
  if (data.dhikr && Array.isArray(data.dhikr)) {
    const dhikrSheet = ss.getSheetByName(CONFIG.TABS.DHIKR_SESSIONS);
    data.dhikr.forEach(d => {
      upsertRow(dhikrSheet, 0, d.id, [
        d.id,
        d.userId,
        d.localDate,
        d.dhikrTitle,
        d.count,
        d.targetCount,
        new Date(d.createdAt || Date.now()).toISOString()
      ]);
      count++;
    });
  }

  // 5. Sync Extended Amal Entries
  if (data.amal && Array.isArray(data.amal)) {
    const amalSheet = ss.getSheetByName(CONFIG.TABS.AMAL_ENTRIES);
    data.amal.forEach(a => {
      upsertRow(amalSheet, 0, a.id, [
        a.id,
        a.userId,
        a.localDate,
        a.category || "CUSTOM",
        a.title || "",
        a.currentCount || 0,
        a.targetCount || 1,
        a.unit || "",
        a.status || "NOT_RECORDED",
        a.isSharedWithSquad ? "YES" : "NO",
        new Date(a.createdAt || Date.now()).toISOString()
      ]);
      count++;
    });
  }

  return { syncedCount: count, timestamp: new Date().toISOString() };
}

function handleAuditLog(ss, audit) {
  if (!audit) return { recorded: false };
  const sheet = ss.getSheetByName(CONFIG.TABS.AUDIT_LOG);
  sheet.appendRow([
    audit.id || Utilities.getUuid(),
    audit.adminUid || "system",
    audit.action || "",
    audit.details || "",
    new Date(audit.timestamp || Date.now()).toISOString()
  ]);
  return { recorded: true };
}

function upsertRow(sheet, keyColumnIndex, keyValue, rowData) {
  if (!sheet) return;
  const data = sheet.getDataRange().getValues();
  for (let i = 1; i < data.length; i++) {
    if (data[i][keyColumnIndex] == keyValue) {
      sheet.getRange(i + 1, 1, 1, rowData.length).setValues([rowData]);
      return;
    }
  }
  sheet.appendRow(rowData);
}

function ensureTabsExist(ss) {
  const definitions = [
    { name: CONFIG.TABS.USERS, headers: ["User UID", "Display Name", "Email", "Timezone", "Consent Status", "Consent Scope", "Squad ID", "Last Activity"] },
    { name: CONFIG.TABS.PRAYER_RECORDS, headers: ["Record ID", "User UID", "Local Date", "Prayer Name", "Status", "Notes", "Created At", "Updated At"] },
    { name: CONFIG.TABS.TAHAJJUD_RECORDS, headers: ["Record ID", "User UID", "Local Date", "Performed", "Rak'ahs", "Start Time", "End Time", "Notes", "Created At", "Updated At"] },
    { name: CONFIG.TABS.DHIKR_SESSIONS, headers: ["Session ID", "User UID", "Local Date", "Dhikr Title", "Count", "Target Count", "Created At"] },
    { name: CONFIG.TABS.AMAL_ENTRIES, headers: ["Amal ID", "User UID", "Local Date", "Category", "Title", "Count", "Target", "Unit", "Status", "Shared with Squad", "Created At"] },
    { name: CONFIG.TABS.SQUAD_SUMMARIES, headers: ["Squad ID", "Squad Name", "Active Members", "Average Prayer %", "Tahajjud Nights", "Total Amal Done", "Updated At"] },
    { name: CONFIG.TABS.DAILY_SUMMARY, headers: ["Summary ID", "User UID", "Date", "Prayers Completed", "Tahajjud Done", "Dhikr Total", "Updated At"] },
    { name: CONFIG.TABS.MONTHLY_SUMMARY, headers: ["Summary ID", "User UID", "Month (YYYY-MM)", "Prayer Rate %", "Tahajjud Nights", "Total Dhikr"] },
    { name: CONFIG.TABS.SYNC_ERRORS, headers: ["Timestamp", "Error Type", "Details"] },
    { name: CONFIG.TABS.AUDIT_LOG, headers: ["Audit ID", "Admin UID", "Action", "Details", "Timestamp"] }
  ];

  definitions.forEach(def => {
    let sheet = ss.getSheetByName(def.name);
    if (!sheet) {
      sheet = ss.insertSheet(def.name);
      sheet.appendRow(def.headers);
      sheet.getRange(1, 1, 1, def.headers.length).setFontWeight("bold").setBackground("#0B3B24").setFontColor("#FFFFFF");
      sheet.setFrozenRows(1);
    }
  });
}

function logSyncError(type, details) {
  try {
    const ss = SpreadsheetApp.getActiveSpreadsheet();
    let sheet = ss.getSheetByName(CONFIG.TABS.SYNC_ERRORS);
    if (!sheet) {
      ensureTabsExist(ss);
      sheet = ss.getSheetByName(CONFIG.TABS.SYNC_ERRORS);
    }
    sheet.appendRow([new Date().toISOString(), type, details]);
  } catch (e) {
    Logger.log("Failed to log sync error: " + e.toString());
  }
}

function jsonResponse(data, code) {
  return ContentService.createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}
