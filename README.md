# FEDCBA Attendance V2 - Android App

## 📱 Features
- WebView wrapper ng iyong existing web app
- Offline mode (localStorage + IndexedDB)
- GPS / Geolocation support
- Daily attendance alarms (Time In, Break Out, Break In, Dinner Out, Time Out)
- Push notifications
- Vibration on alarms
- Back button support with exit confirmation
- Splash screen with FEDCBA logo animation

---

## ⚙️ Bago mag-build: I-update ang Website URL

Buksan ang file:
```
app/src/main/java/com/fedcba/attendance/MainActivity.java
```

Hanapin ang linyang ito (line ~20):
```java
private static final String WEBSITE_URL = "https://www.attendance.v2.fedcba.site/";
```

Palitan ng iyong actual URL kung nag-bago na.

---

## 🛠️ Paano mag-build ng APK

### Kinakailangan:
- **Android Studio** (libre) — i-download sa https://developer.android.com/studio
- **Java JDK 11 o mas bago**
- Hindi kailangan ng Play Store account para sa debug APK

---

### Hakbang 1 — I-install ang Android Studio
1. I-download ang Android Studio
2. I-install at buksan
3. I-install ang SDK tools kapag na-prompt

---

### Hakbang 2 — I-open ang Project
1. Buksan ang Android Studio
2. Click **"Open"**
3. Piliin ang folder na **FedcbaApp**
4. Hintayin ang Gradle sync (ilang minuto)

---

### Hakbang 3 — Mag-build ng Debug APK (para sa testing)
1. Sa menu bar: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Hintayin (1-3 minuto)
3. Click **"locate"** sa notification para makita ang APK
4. Makikita ang APK sa:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
5. I-copy sa phone at i-install! ✅

---

### Hakbang 4 — Mag-build ng Release APK (para sa Play Store)
1. **Build → Generate Signed Bundle / APK**
2. Piliin **APK**
3. Click **"Create new..."** para gumawa ng keystore
   - Itago ang keystore file at password — HINDI ito mababawi!
4. Fill in ang details, click **Next**
5. Piliin **release** build variant
6. Click **Finish**
7. APK makikita sa:
   ```
   app/build/outputs/apk/release/app-release.apk
   ```

---

## 📲 Paano mag-install sa phone (debug APK)

1. Sa phone: **Settings → Security → Unknown Sources** → I-enable
   (O: Settings → Apps → Special app access → Install unknown apps)
2. I-transfer ang APK sa phone (USB, email, o Google Drive)
3. I-tap ang APK file
4. Click **Install**
5. Tapos na! 🎉

---

## 🔔 Alarm Schedule
| Oras | Label |
|------|-------|
| 8:00 AM | ⏰ Time In |
| 12:00 PM | ☕ Break Out |
| 1:00 PM | ✅ Break In |
| 3:00 PM | 🍽️ Dinner Out |
| 5:25 PM | 🏁 Time Out |

Para baguhin ang oras, i-edit ang `AlarmScheduler.java`:
```java
private static final int[][] ALARM_TIMES = {
    {8,  0,  0},  // {hour, minute, id}
    {12, 0,  1},
    ...
};
```

---

## 🆘 Common Errors

**"Gradle sync failed"**
→ File → Invalidate Caches → Restart

**"SDK not found"**
→ Tools → SDK Manager → Install Android 12+ SDK

**"App not installed" sa phone**
→ I-uninstall ang lumang version muna

---

## 📦 Para sa Play Store
Kailangan mo ng:
1. Google Play Developer Account ($25 one-time)
2. Release APK (may keystore signature)
3. Screenshots ng app
4. Description at icon

---

*Ginawa para sa FEDCBA Attendance System*
