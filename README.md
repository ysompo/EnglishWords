# מילים באנגלית

אפליקציית Android ללימוד אוצר מילים באנגלית לילדים בגיל בית ספר יסודי — 5 מילים חדשות ביום, תרגול הגייה עם זיהוי דיבור, בוחן יומי ושבועי, וגיימיפיקציה (כוכבים, רצפים, תגים).

מפרט מלא: [docs/superpowers/specs/2026-08-10-english-vocab-app-design.md](docs/superpowers/specs/2026-08-10-english-vocab-app-design.md)
תוכנית הפיתוח: [docs/superpowers/plans/2026-08-10-english-vocab-app-plan.md](docs/superpowers/plans/2026-08-10-english-vocab-app-plan.md)

## התקנה על הטלפון

קובץ ההתקנה (APK) בנוי כבר ונמצא כאן:

```
app\build\outputs\apk\debug\app-debug.apk
```

### אפשרות 1: העברת הקובץ ישירות לטלפון

1. העתק את `app-debug.apk` לטלפון (USB, מייל לעצמך, Google Drive וכו').
2. בטלפון, בהגדרות → אפליקציות → אפשר "התקנה ממקורות לא ידועים" עבור האפליקציה שממנה תפתח את הקובץ (למשל מנהל הקבצים או הדפדפן).
3. הקש על הקובץ בטלפון כדי להתקין.

### אפשרות 2: התקנה דרך USB עם adb

1. בטלפון: הגדרות → אודות הטלפון → הקש 7 פעמים על "מספר build" (מפעיל מצב מפתחים) → הגדרות → אפשרויות למפתחים → הפעל "ניפוי באגים ב-USB".
2. חבר את הטלפון למחשב ב-USB ואשר את הבקשה שתופיע על הטלפון.
3. הרץ:

```bash
"C:\Users\ysomp\AndroidSdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
```

## בנייה מחדש מהקוד (אם צריך)

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat assembleDebug
```

ה-APK החדש ייווצר באותו נתיב שלמעלה. להרצת כל בדיקות היחידה (47 בדיקות):

```bash
.\gradlew.bat testDebugUnitTest
```

## מבנה הפרויקט

- `app/src/main/java/com/ysompo/englishwords/data/` — מסד נתונים מקומי (Room): מילים, התקדמות, השלמות יומיות/שבועיות
- `app/src/main/java/com/ysompo/englishwords/logic/` — לוגיקה טהורה (בחירת מילים, מסיחים, בוחנים, רצפים, תגים, התאמת הגייה) — כל הקבצים כאן מכוסים בבדיקות יחידה
- `app/src/main/java/com/ysompo/englishwords/repo/` — שכבת Repository שמחברת בין ה-DB ללוגיקה
- `app/src/main/java/com/ysompo/englishwords/speech/` — עטיפות ל-TextToSpeech ו-SpeechRecognizer
- `app/src/main/java/com/ysompo/englishwords/ui/` — המסכים (בית, למידה, בוחן, התקדמות, הגדרות)
- `app/src/main/java/com/ysompo/englishwords/notification/` — תזכורת יומית (WorkManager)
- `app/src/main/assets/words.json` — מאגר 1000 המילים עם תרגום, חלק דיבר ומשפט לדוגמה
- `tools/validate_words.py` — סקריפט לבדיקת תקינות קובץ המילים

## דרישות סביבת פיתוח (מותקנות כבר על המחשב הזה)

- JDK 17: `C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot`
- Android SDK: `C:\Users\ysomp\AndroidSdk`
