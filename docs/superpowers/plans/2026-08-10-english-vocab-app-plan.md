# English Vocabulary Learning App — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking. Work directly in this repository (`C:\Users\ysomp\OneDrive\Documents\EnglishWords`) — do not create a separate git worktree, this is a fresh project with no other work to isolate from.

**Goal:** Build the Android app described in `docs/superpowers/specs/2026-08-10-english-vocab-app-design.md`: daily 5-word vocabulary lessons with pronunciation practice, daily and weekly quizzes, streak/star/badge gamification, and a monthly-reward indicator, backed by a 1000-word Hebrew-English content bank.

**Architecture:** Native Android (Kotlin), Room for local persistence, plain-Kotlin logic classes (no Android dependency) for all scoring/selection rules so they run under fast JVM unit tests, thin wrappers around `TextToSpeech`/`SpeechRecognizer`, `WorkManager` for the daily reminder. Views use XML layouts + ViewBinding (no Compose, to keep the toolchain lean on this machine).

**Tech Stack:** Kotlin, Gradle 8.7 (wrapper committed), AGP 8.4.2, JDK 17, Room 2.6.1, WorkManager 2.9.1, JUnit4, Robolectric 4.13 (for Room/asset-dependent tests), Truth assertions.

**Environment already verified working on this machine:** JDK 17 (`C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot`), Android SDK at `C:\Users\ysomp\AndroidSdk` (platform 34, build-tools 34.0.0, platform-tools), Gradle wrapper (`gradlew.bat`) — `assembleDebug` has been run successfully. Every task below assumes you run Gradle via `.\gradlew.bat <task>` from the repo root with `$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"` set first (PowerShell) — the harness does not persist this env var between tool calls in the current session, so set it at the start of every command block that invokes Gradle.

There is **no emulator or physical device configured**. Unit tests (JVM + Robolectric) can be run and verified after every task. Anything that only makes sense on-device (TTS actually speaking, SpeechRecognizer actually listening, real notification firing) is called out explicitly as **manual verification** in the final task — it cannot be automated here.

---

### Task 1: Project Scaffold — ALREADY COMPLETE

**Files (already created and committed):**
- `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `.gitignore`
- `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/ysompo/englishwords/MainActivity.kt`
- `app/src/main/res/layout/activity_main.xml`, `app/src/main/res/values/strings.xml`, `app/src/main/res/values/themes.xml`, `app/src/main/res/drawable/ic_launcher.xml`

- [x] **Step 1: Scaffold created, `assembleDebug` verified to succeed, committed** (commits `901447c`, `34f78fd` — the second commit also enabled core library desugaring for `java.time`, needed starting at Task 5).

No action needed — start at Task 2.

---

### Task 2: Word Entity, JSON Schema, and a 40-Word Starter Bank

**Files:**
- Create: `app/src/main/assets/words.json`
- Create: `app/src/main/java/com/ysompo/englishwords/data/WordEntity.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/data/WordJsonLoader.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/data/WordJsonLoaderTest.kt`

- [ ] **Step 1: Write the starter word bank (40 words, IDs 1-40, ordered by frequency)**

```json
[
  {"id": 1, "word": "the", "translation_he": "ה-", "part_of_speech": "determiner", "example_sentence": "I saw ___ dog in the park.", "order_index": 1},
  {"id": 2, "word": "and", "translation_he": "ו-", "part_of_speech": "conjunction", "example_sentence": "I like tea ___ coffee.", "order_index": 2},
  {"id": 3, "word": "you", "translation_he": "אתה / את", "part_of_speech": "pronoun", "example_sentence": "Can ___ help me, please?", "order_index": 3},
  {"id": 4, "word": "is", "translation_he": "הוא / היא (זמן הווה)", "part_of_speech": "verb", "example_sentence": "This ___ my house.", "order_index": 4},
  {"id": 5, "word": "it", "translation_he": "זה", "part_of_speech": "pronoun", "example_sentence": "___ is raining outside.", "order_index": 5},
  {"id": 6, "word": "not", "translation_he": "לא", "part_of_speech": "adverb", "example_sentence": "I do ___ like this food.", "order_index": 6},
  {"id": 7, "word": "he", "translation_he": "הוא", "part_of_speech": "pronoun", "example_sentence": "___ is my best friend.", "order_index": 7},
  {"id": 8, "word": "she", "translation_he": "היא", "part_of_speech": "pronoun", "example_sentence": "___ plays soccer every day.", "order_index": 8},
  {"id": 9, "word": "have", "translation_he": "יש לי / להחזיק", "part_of_speech": "verb", "example_sentence": "I ___ two brothers.", "order_index": 9},
  {"id": 10, "word": "good", "translation_he": "טוב", "part_of_speech": "adjective", "example_sentence": "This is a ___ book.", "order_index": 10},
  {"id": 11, "word": "go", "translation_he": "ללכת", "part_of_speech": "verb", "example_sentence": "Let's ___ to the beach.", "order_index": 11},
  {"id": 12, "word": "day", "translation_he": "יום", "part_of_speech": "noun", "example_sentence": "Today is a sunny ___.", "order_index": 12},
  {"id": 13, "word": "big", "translation_he": "גדול", "part_of_speech": "adjective", "example_sentence": "The elephant is very ___.", "order_index": 13},
  {"id": 14, "word": "small", "translation_he": "קטן", "part_of_speech": "adjective", "example_sentence": "The mouse is ___.", "order_index": 14},
  {"id": 15, "word": "eat", "translation_he": "לאכול", "part_of_speech": "verb", "example_sentence": "I ___ breakfast every morning.", "order_index": 15},
  {"id": 16, "word": "run", "translation_he": "לרוץ", "part_of_speech": "verb", "example_sentence": "The dog likes to ___.", "order_index": 16},
  {"id": 17, "word": "happy", "translation_he": "שמח", "part_of_speech": "adjective", "example_sentence": "She is very ___ today.", "order_index": 17},
  {"id": 18, "word": "sad", "translation_he": "עצוב", "part_of_speech": "adjective", "example_sentence": "He looked ___ after losing the game.", "order_index": 18},
  {"id": 19, "word": "friend", "translation_he": "חבר", "part_of_speech": "noun", "example_sentence": "She is my best ___.", "order_index": 19},
  {"id": 20, "word": "school", "translation_he": "בית ספר", "part_of_speech": "noun", "example_sentence": "I go to ___ every day.", "order_index": 20},
  {"id": 21, "word": "book", "translation_he": "ספר", "part_of_speech": "noun", "example_sentence": "I am reading a new ___.", "order_index": 21},
  {"id": 22, "word": "water", "translation_he": "מים", "part_of_speech": "noun", "example_sentence": "Please give me a glass of ___.", "order_index": 22},
  {"id": 23, "word": "food", "translation_he": "אוכל", "part_of_speech": "noun", "example_sentence": "This ___ tastes delicious.", "order_index": 23},
  {"id": 24, "word": "play", "translation_he": "לשחק", "part_of_speech": "verb", "example_sentence": "The kids ___ in the yard.", "order_index": 24},
  {"id": 25, "word": "read", "translation_he": "לקרוא", "part_of_speech": "verb", "example_sentence": "I like to ___ before bed.", "order_index": 25},
  {"id": 26, "word": "write", "translation_he": "לכתוב", "part_of_speech": "verb", "example_sentence": "Please ___ your name here.", "order_index": 26},
  {"id": 27, "word": "fast", "translation_he": "מהיר", "part_of_speech": "adjective", "example_sentence": "That car is very ___.", "order_index": 27},
  {"id": 28, "word": "slow", "translation_he": "איטי", "part_of_speech": "adjective", "example_sentence": "The turtle is ___.", "order_index": 28},
  {"id": 29, "word": "house", "translation_he": "בית", "part_of_speech": "noun", "example_sentence": "We live in a small ___.", "order_index": 29},
  {"id": 30, "word": "family", "translation_he": "משפחה", "part_of_speech": "noun", "example_sentence": "I love spending time with my ___.", "order_index": 30},
  {"id": 31, "word": "love", "translation_he": "לאהוב", "part_of_speech": "verb", "example_sentence": "I ___ playing video games.", "order_index": 31},
  {"id": 32, "word": "want", "translation_he": "לרצות", "part_of_speech": "verb", "example_sentence": "I ___ to go outside.", "order_index": 32},
  {"id": 33, "word": "know", "translation_he": "לדעת", "part_of_speech": "verb", "example_sentence": "I ___ the answer.", "order_index": 33},
  {"id": 34, "word": "think", "translation_he": "לחשוב", "part_of_speech": "verb", "example_sentence": "I ___ it will rain today.", "order_index": 34},
  {"id": 35, "word": "strong", "translation_he": "חזק", "part_of_speech": "adjective", "example_sentence": "He is very ___.", "order_index": 35},
  {"id": 36, "word": "weak", "translation_he": "חלש", "part_of_speech": "adjective", "example_sentence": "The signal here is ___.", "order_index": 36},
  {"id": 37, "word": "morning", "translation_he": "בוקר", "part_of_speech": "noun", "example_sentence": "I wake up early in the ___.", "order_index": 37},
  {"id": 38, "word": "night", "translation_he": "לילה", "part_of_speech": "noun", "example_sentence": "The stars come out at ___.", "order_index": 38},
  {"id": 39, "word": "animal", "translation_he": "חיה", "part_of_speech": "noun", "example_sentence": "A dog is my favorite ___.", "order_index": 39},
  {"id": 40, "word": "color", "translation_he": "צבע", "part_of_speech": "noun", "example_sentence": "Blue is my favorite ___.", "order_index": 40}
]
```

Save this exactly as `app/src/main/assets/words.json`.

- [ ] **Step 2: Write `WordEntity`**

```kotlin
package com.ysompo.englishwords.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val id: Int,
    val word: String,
    val translationHe: String,
    val partOfSpeech: String,
    val exampleSentence: String,
    val orderIndex: Int
)
```

- [ ] **Step 3: Write the failing test for `WordJsonLoader`**

```kotlin
package com.ysompo.englishwords.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WordJsonLoaderTest {

    private val sampleJson = """
        [
          {"id": 1, "word": "the", "translation_he": "ה-", "part_of_speech": "determiner", "example_sentence": "I saw ___ dog.", "order_index": 1},
          {"id": 2, "word": "and", "translation_he": "ו-", "part_of_speech": "conjunction", "example_sentence": "tea ___ coffee.", "order_index": 2}
        ]
    """.trimIndent()

    @Test
    fun `parses all fields correctly`() {
        val words = WordJsonLoader.parse(sampleJson)

        assertThat(words).hasSize(2)
        assertThat(words[0]).isEqualTo(
            WordEntity(id = 1, word = "the", translationHe = "ה-", partOfSpeech = "determiner", exampleSentence = "I saw ___ dog.", orderIndex = 1)
        )
        assertThat(words[1].word).isEqualTo("and")
    }

    @Test
    fun `parses empty array as empty list`() {
        assertThat(WordJsonLoader.parse("[]")).isEmpty()
    }
}
```

- [ ] **Step 4: Run test to verify it fails**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.data.WordJsonLoaderTest" --console=plain
```
Expected: FAIL — `WordJsonLoader` does not exist (compile error).

- [ ] **Step 5: Implement `WordJsonLoader`**

```kotlin
package com.ysompo.englishwords.data

import android.content.Context
import org.json.JSONArray

object WordJsonLoader {

    fun loadFromAssets(context: Context, assetName: String = "words.json"): List<WordEntity> {
        val jsonText = context.assets.open(assetName).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return parse(jsonText)
    }

    fun parse(jsonText: String): List<WordEntity> {
        val array = JSONArray(jsonText)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            WordEntity(
                id = obj.getInt("id"),
                word = obj.getString("word"),
                translationHe = obj.getString("translation_he"),
                partOfSpeech = obj.getString("part_of_speech"),
                exampleSentence = obj.getString("example_sentence"),
                orderIndex = obj.getInt("order_index")
            )
        }
    }
}
```

- [ ] **Step 6: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.data.WordJsonLoaderTest" --console=plain
```
Expected: PASS (2 tests).

- [ ] **Step 7: Commit**

```bash
git add app/src/main/assets/words.json app/src/main/java/com/ysompo/englishwords/data/WordEntity.kt app/src/main/java/com/ysompo/englishwords/data/WordJsonLoader.kt app/src/test/java/com/ysompo/englishwords/data/WordJsonLoaderTest.kt
git commit -m "Add WordEntity, JSON parsing, and 40-word starter content bank"
```

---

### Task 3: Room Database — WordDao, AppDatabase, WordSeeder

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/data/WordDao.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/data/AppDatabase.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/data/WordSeeder.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/data/WordDaoTest.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/data/WordSeederTest.kt`

- [ ] **Step 1: Write `WordDao`**

```kotlin
package com.ysompo.englishwords.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<WordEntity>)

    @Query("SELECT COUNT(*) FROM words")
    suspend fun count(): Int

    @Query("SELECT * FROM words ORDER BY orderIndex ASC")
    suspend fun getAllOrdered(): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getById(id: Int): WordEntity?
}
```

- [ ] **Step 2: Write `AppDatabase`**

```kotlin
package com.ysompo.englishwords.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WordEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "english_words.db"
                ).build().also { INSTANCE = it }
            }
    }
}
```

- [ ] **Step 3: Write the failing test for `WordDao`**

```kotlin
package com.ysompo.englishwords.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WordDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WordDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.wordDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `insertAll then getAllOrdered returns words sorted by orderIndex`() = runBlocking {
        val w1 = WordEntity(1, "the", "ה-", "determiner", "I saw ___ dog.", orderIndex = 2)
        val w2 = WordEntity(2, "and", "ו-", "conjunction", "tea ___ coffee.", orderIndex = 1)
        dao.insertAll(listOf(w1, w2))

        val result = dao.getAllOrdered()

        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(2)
        assertThat(result[1].id).isEqualTo(1)
    }

    @Test
    fun `count reflects number of inserted rows`() = runBlocking {
        assertThat(dao.count()).isEqualTo(0)
        dao.insertAll(listOf(WordEntity(1, "the", "ה-", "determiner", "s", 1)))
        assertThat(dao.count()).isEqualTo(1)
    }
}
```

- [ ] **Step 4: Run test to verify it fails**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.data.WordDaoTest" --console=plain
```
Expected: FAIL — `WordDao`/`AppDatabase` not yet compiled against Room annotation processor, or class not found (write Step 1/2 first if not already present, then re-run to confirm a clean pass instead — if you followed the steps in order, run this once after Steps 1-2 to confirm PASS directly and treat that as your verification).

- [ ] **Step 5: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.data.WordDaoTest" --console=plain
```
Expected: PASS (2 tests).

- [ ] **Step 6: Write `WordSeeder`**

```kotlin
package com.ysompo.englishwords.data

import android.content.Context

class WordSeeder(private val context: Context, private val wordDao: WordDao) {
    suspend fun seedIfNeeded() {
        if (wordDao.count() == 0) {
            wordDao.insertAll(WordJsonLoader.loadFromAssets(context))
        }
    }
}
```

- [ ] **Step 7: Write the failing test for `WordSeeder`**

```kotlin
package com.ysompo.englishwords.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WordSeederTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `seedIfNeeded loads all words from assets on first run`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val seeder = WordSeeder(context, db.wordDao())

        seeder.seedIfNeeded()

        assertThat(db.wordDao().count()).isEqualTo(40)
    }

    @Test
    fun `seedIfNeeded does nothing if words already exist`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val seeder = WordSeeder(context, db.wordDao())
        seeder.seedIfNeeded()
        val countAfterFirst = db.wordDao().count()

        seeder.seedIfNeeded()

        assertThat(db.wordDao().count()).isEqualTo(countAfterFirst)
    }
}
```

- [ ] **Step 8: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.data.WordSeederTest" --console=plain
```
Expected: PASS (2 tests). Robolectric reads `app/src/main/assets/words.json` from the merged test resources automatically because `testOptions.unitTests.isIncludeAndroidResources = true` is already set in `app/build.gradle.kts`.

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/data/WordDao.kt app/src/main/java/com/ysompo/englishwords/data/AppDatabase.kt app/src/main/java/com/ysompo/englishwords/data/WordSeeder.kt app/src/test/java/com/ysompo/englishwords/data/WordDaoTest.kt app/src/test/java/com/ysompo/englishwords/data/WordSeederTest.kt
git commit -m "Add Room database, WordDao, and first-run word seeding"
```

---

### Task 4: Progress Persistence — LearningProgress, DailyCompletion, WeeklyStatus

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/data/LearningProgressEntity.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/data/LearningProgressDao.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/data/DailyCompletionEntity.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/data/DailyCompletionDao.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/data/WeeklyStatusEntity.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/data/WeeklyStatusDao.kt`
- Modify: `app/src/main/java/com/ysompo/englishwords/data/AppDatabase.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/data/ProgressDaosTest.kt`

- [ ] **Step 1: Write the three entities**

```kotlin
package com.ysompo.englishwords.data

import androidx.room.Entity

@Entity(tableName = "learning_progress", primaryKeys = ["wordId"])
data class LearningProgressEntity(
    val wordId: Int,
    val learnedDate: String
)
```

```kotlin
package com.ysompo.englishwords.data

import androidx.room.Entity

@Entity(tableName = "daily_completion", primaryKeys = ["date"])
data class DailyCompletionEntity(
    val date: String,
    val learningDone: Boolean,
    val quizDone: Boolean,
    val quizScore: Int
)
```

```kotlin
package com.ysompo.englishwords.data

import androidx.room.Entity

@Entity(tableName = "weekly_status", primaryKeys = ["weekStartDate"])
data class WeeklyStatusEntity(
    val weekStartDate: String,
    val daysCompleted: Int,
    val starEarned: Boolean,
    val weeklyQuizScore: Int?
)
```

- [ ] **Step 2: Write the three DAOs**

```kotlin
package com.ysompo.englishwords.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LearningProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: LearningProgressEntity)

    @Query("SELECT wordId FROM learning_progress")
    suspend fun getLearnedWordIds(): List<Int>

    @Query("SELECT COUNT(*) FROM learning_progress")
    suspend fun countLearned(): Int
}
```

```kotlin
package com.ysompo.englishwords.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyCompletionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyCompletionEntity)

    @Query("SELECT * FROM daily_completion WHERE date = :date")
    suspend fun getByDate(date: String): DailyCompletionEntity?

    @Query("SELECT * FROM daily_completion WHERE date BETWEEN :start AND :end")
    suspend fun getBetween(start: String, end: String): List<DailyCompletionEntity>
}
```

```kotlin
package com.ysompo.englishwords.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeeklyStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WeeklyStatusEntity)

    @Query("SELECT * FROM weekly_status ORDER BY weekStartDate ASC")
    suspend fun getAll(): List<WeeklyStatusEntity>

    @Query("SELECT * FROM weekly_status WHERE weekStartDate = :weekStartDate")
    suspend fun getByWeekStart(weekStartDate: String): WeeklyStatusEntity?
}
```

- [ ] **Step 3: Register the new entities/DAOs on `AppDatabase`**

Replace the contents of `app/src/main/java/com/ysompo/englishwords/data/AppDatabase.kt` with:

```kotlin
package com.ysompo.englishwords.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WordEntity::class,
        LearningProgressEntity::class,
        DailyCompletionEntity::class,
        WeeklyStatusEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun learningProgressDao(): LearningProgressDao
    abstract fun dailyCompletionDao(): DailyCompletionDao
    abstract fun weeklyStatusDao(): WeeklyStatusDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "english_words.db"
                ).build().also { INSTANCE = it }
            }
    }
}
```

- [ ] **Step 4: Write the failing test covering all three DAOs**

```kotlin
package com.ysompo.englishwords.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProgressDaosTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `learning progress tracks learned word ids`() = runBlocking {
        db.learningProgressDao().insert(LearningProgressEntity(wordId = 1, learnedDate = "2026-08-10"))
        db.learningProgressDao().insert(LearningProgressEntity(wordId = 2, learnedDate = "2026-08-10"))

        assertThat(db.learningProgressDao().getLearnedWordIds()).containsExactly(1, 2)
        assertThat(db.learningProgressDao().countLearned()).isEqualTo(2)
    }

    @Test
    fun `daily completion upsert and range query`() = runBlocking {
        db.dailyCompletionDao().upsert(DailyCompletionEntity("2026-08-09", learningDone = true, quizDone = true, quizScore = 5))
        db.dailyCompletionDao().upsert(DailyCompletionEntity("2026-08-10", learningDone = true, quizDone = false, quizScore = 0))

        val range = db.dailyCompletionDao().getBetween("2026-08-09", "2026-08-13")
        assertThat(range).hasSize(2)

        val single = db.dailyCompletionDao().getByDate("2026-08-10")
        assertThat(single?.quizDone).isFalse()
    }

    @Test
    fun `weekly status upsert and lookup`() = runBlocking {
        db.weeklyStatusDao().upsert(WeeklyStatusEntity("2026-08-09", daysCompleted = 4, starEarned = true, weeklyQuizScore = 4))

        val result = db.weeklyStatusDao().getByWeekStart("2026-08-09")
        assertThat(result?.starEarned).isTrue()
        assertThat(db.weeklyStatusDao().getAll()).hasSize(1)
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.data.ProgressDaosTest" --console=plain
```
Expected: PASS (3 tests).

- [ ] **Step 6: Run the full existing test suite to confirm nothing broke**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --console=plain
```
Expected: PASS (all tests so far).

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/data/
git add app/src/test/java/com/ysompo/englishwords/data/ProgressDaosTest.kt
git commit -m "Add LearningProgress, DailyCompletion, and WeeklyStatus persistence"
```

---

### Task 5: WeekUtils (date/week boundary logic)

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/logic/WeekUtils.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/logic/WeekUtilsTest.kt`

The design fixes the "week" as **Sunday through Thursday** (`docs/superpowers/specs/2026-08-10-english-vocab-app-design.md`, section "בוחן שבועי"). This class computes the Sunday that starts any given date's week, and whether a date is a school day (Sunday-Thursday).

- [ ] **Step 1: Write the failing test**

```kotlin
package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class WeekUtilsTest {

    @Test
    fun `weekStartFor returns the Sunday of that week for every day Sun-Sat`() {
        // 2026-08-09 is a Sunday
        val sunday = LocalDate.of(2026, 8, 9)
        assertThat(WeekUtils.weekStartFor(sunday)).isEqualTo(sunday)
        assertThat(WeekUtils.weekStartFor(LocalDate.of(2026, 8, 10))).isEqualTo(sunday) // Monday
        assertThat(WeekUtils.weekStartFor(LocalDate.of(2026, 8, 13))).isEqualTo(sunday) // Thursday
        assertThat(WeekUtils.weekStartFor(LocalDate.of(2026, 8, 14))).isEqualTo(sunday) // Friday
        assertThat(WeekUtils.weekStartFor(LocalDate.of(2026, 8, 15))).isEqualTo(sunday) // Saturday
        assertThat(WeekUtils.weekStartFor(LocalDate.of(2026, 8, 16))).isEqualTo(LocalDate.of(2026, 8, 16)) // next Sunday
    }

    @Test
    fun `isSchoolDay is true Sunday through Thursday, false Friday and Saturday`() {
        assertThat(WeekUtils.isSchoolDay(LocalDate.of(2026, 8, 9))).isTrue()   // Sun
        assertThat(WeekUtils.isSchoolDay(LocalDate.of(2026, 8, 13))).isTrue()  // Thu
        assertThat(WeekUtils.isSchoolDay(LocalDate.of(2026, 8, 14))).isFalse() // Fri
        assertThat(WeekUtils.isSchoolDay(LocalDate.of(2026, 8, 15))).isFalse() // Sat
    }

    @Test
    fun `formatDate and parseDate round-trip as ISO yyyy-MM-dd`() {
        val date = LocalDate.of(2026, 8, 9)
        val text = WeekUtils.formatDate(date)
        assertThat(text).isEqualTo("2026-08-09")
        assertThat(WeekUtils.parseDate(text)).isEqualTo(date)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.WeekUtilsTest" --console=plain
```
Expected: FAIL — `WeekUtils` unresolved.

- [ ] **Step 3: Implement `WeekUtils`**

```kotlin
package com.ysompo.englishwords.logic

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object WeekUtils {
    private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun weekStartFor(date: LocalDate): LocalDate {
        // DayOfWeek.value: Monday=1 .. Sunday=7. Sunday%7=0 (no shift), Monday%7=1, ... Saturday%7=6.
        val daysSinceSunday = date.dayOfWeek.value % 7
        return date.minusDays(daysSinceSunday.toLong())
    }

    fun isSchoolDay(date: LocalDate): Boolean =
        date.dayOfWeek != DayOfWeek.FRIDAY && date.dayOfWeek != DayOfWeek.SATURDAY

    fun formatDate(date: LocalDate): String = date.format(FORMATTER)

    fun parseDate(text: String): LocalDate = LocalDate.parse(text, FORMATTER)
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.WeekUtilsTest" --console=plain
```
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/logic/WeekUtils.kt app/src/test/java/com/ysompo/englishwords/logic/WeekUtilsTest.kt
git commit -m "Add WeekUtils for Sunday-Thursday week boundary logic"
```

---

### Task 6: DailyLessonSelector

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/logic/DailyLessonSelector.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/logic/DailyLessonSelectorTest.kt`

Picks the next 5 not-yet-learned words, in `orderIndex` order.

- [ ] **Step 1: Write the failing test**

```kotlin
package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import com.ysompo.englishwords.data.WordEntity
import org.junit.Test

class DailyLessonSelectorTest {

    private fun word(id: Int, orderIndex: Int) =
        WordEntity(id, "word$id", "he$id", "noun", "sentence ___.", orderIndex)

    @Test
    fun `returns first 5 words in orderIndex order when none learned`() {
        val words = (1..10).map { word(it, orderIndex = it) }.shuffled()

        val result = DailyLessonSelector.nextWordsToLearn(words, learnedWordIds = emptySet())

        assertThat(result.map { it.id }).containsExactly(1, 2, 3, 4, 5).inOrder()
    }

    @Test
    fun `skips already-learned words`() {
        val words = (1..10).map { word(it, orderIndex = it) }

        val result = DailyLessonSelector.nextWordsToLearn(words, learnedWordIds = setOf(1, 2, 3, 4, 5))

        assertThat(result.map { it.id }).containsExactly(6, 7, 8, 9, 10).inOrder()
    }

    @Test
    fun `returns fewer than 5 when fewer words remain`() {
        val words = (1..3).map { word(it, orderIndex = it) }

        val result = DailyLessonSelector.nextWordsToLearn(words, learnedWordIds = emptySet())

        assertThat(result).hasSize(3)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.DailyLessonSelectorTest" --console=plain
```
Expected: FAIL — `DailyLessonSelector` unresolved.

- [ ] **Step 3: Implement `DailyLessonSelector`**

```kotlin
package com.ysompo.englishwords.logic

import com.ysompo.englishwords.data.WordEntity

object DailyLessonSelector {
    const val WORDS_PER_DAY = 5

    fun nextWordsToLearn(allWordsOrderedByIndex: List<WordEntity>, learnedWordIds: Set<Int>): List<WordEntity> {
        return allWordsOrderedByIndex
            .sortedBy { it.orderIndex }
            .filter { it.id !in learnedWordIds }
            .take(WORDS_PER_DAY)
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.DailyLessonSelectorTest" --console=plain
```
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/logic/DailyLessonSelector.kt app/src/test/java/com/ysompo/englishwords/logic/DailyLessonSelectorTest.kt
git commit -m "Add DailyLessonSelector for picking the next 5 words to learn"
```

---

### Task 7: DistractorSelector

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/logic/DistractorSelector.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/logic/DistractorSelectorTest.kt`

Per the design's "בחירת מסיחים" section: translation distractors prefer same part-of-speech; sentence-completion distractors must be same part-of-speech (so the sentence stays grammatical).

- [ ] **Step 1: Write the failing test**

```kotlin
package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import com.ysompo.englishwords.data.WordEntity
import org.junit.Test
import kotlin.random.Random

class DistractorSelectorTest {

    private val pool = listOf(
        WordEntity(1, "run", "לרוץ", "verb", "I ___ fast.", 1),
        WordEntity(2, "eat", "לאכול", "verb", "I ___ lunch.", 2),
        WordEntity(3, "jump", "לקפוץ", "verb", "I ___ high.", 3),
        WordEntity(4, "happy", "שמח", "adjective", "I am ___.", 4),
        WordEntity(5, "sad", "עצוב", "adjective", "I am ___.", 5)
    )

    @Test
    fun `translationDistractors excludes the correct word and returns requested count`() {
        val correct = pool[0] // "run"

        val distractors = DistractorSelector.translationDistractors(correct, pool, count = 2, random = Random(42))

        assertThat(distractors).hasSize(2)
        assertThat(distractors).doesNotContain(correct.translationHe)
    }

    @Test
    fun `translationDistractors prefers same part of speech when enough candidates exist`() {
        val correct = pool[0] // verb "run"

        val distractors = DistractorSelector.translationDistractors(correct, pool, count = 2, random = Random(1))

        // Only 2 other verbs exist ("eat", "jump") - both should be chosen before any adjective.
        assertThat(distractors).containsExactly("לאכול", "לקפוץ")
    }

    @Test
    fun `sentenceCompletionDistractors only returns words with matching part of speech`() {
        val correct = pool[3] // adjective "happy"

        val distractors = DistractorSelector.sentenceCompletionDistractors(correct, pool, count = 1, random = Random(7))

        assertThat(distractors).containsExactly("sad")
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.DistractorSelectorTest" --console=plain
```
Expected: FAIL — `DistractorSelector` unresolved.

- [ ] **Step 3: Implement `DistractorSelector`**

```kotlin
package com.ysompo.englishwords.logic

import com.ysompo.englishwords.data.WordEntity
import kotlin.random.Random

object DistractorSelector {

    fun translationDistractors(
        correct: WordEntity,
        pool: List<WordEntity>,
        count: Int = 3,
        random: Random = Random
    ): List<String> {
        val samePartOfSpeech = pool.filter {
            it.id != correct.id && it.partOfSpeech == correct.partOfSpeech && it.translationHe != correct.translationHe
        }.shuffled(random)

        val others = pool.filter {
            it.id != correct.id && it.partOfSpeech != correct.partOfSpeech && it.translationHe != correct.translationHe
        }.shuffled(random)

        return (samePartOfSpeech + others).distinctBy { it.translationHe }.take(count).map { it.translationHe }
    }

    fun sentenceCompletionDistractors(
        correct: WordEntity,
        pool: List<WordEntity>,
        count: Int = 3,
        random: Random = Random
    ): List<String> {
        val samePartOfSpeech = pool.filter {
            it.id != correct.id && it.partOfSpeech == correct.partOfSpeech && it.word != correct.word
        }.shuffled(random)

        return samePartOfSpeech.distinctBy { it.word }.take(count).map { it.word }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.DistractorSelectorTest" --console=plain
```
Expected: PASS (3 tests). If the "prefers same part of speech" test is flaky with a different seed, keep the seed values exactly as written — they were chosen so `shuffled(random)` produces a deterministic, verified order.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/logic/DistractorSelector.kt app/src/test/java/com/ysompo/englishwords/logic/DistractorSelectorTest.kt
git commit -m "Add DistractorSelector for quiz multiple-choice options"
```

---

### Task 8: QuizQuestionGenerator

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/logic/QuizQuestion.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/logic/QuizQuestionGenerator.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/logic/QuizQuestionGeneratorTest.kt`

Per design: daily quiz mixes sentence-completion and translation-choice; weekly quiz is translation-choice only, drawn from all words learned so far.

- [ ] **Step 1: Write `QuizQuestion`**

```kotlin
package com.ysompo.englishwords.logic

import com.ysompo.englishwords.data.WordEntity

enum class QuestionType { TRANSLATION_CHOICE, SENTENCE_COMPLETION }

data class QuizQuestion(
    val type: QuestionType,
    val promptWord: WordEntity,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)
```

- [ ] **Step 2: Write the failing test**

```kotlin
package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import com.ysompo.englishwords.data.WordEntity
import org.junit.Test
import kotlin.random.Random

class QuizQuestionGeneratorTest {

    private val pool = listOf(
        WordEntity(1, "run", "לרוץ", "verb", "I ___ fast.", 1),
        WordEntity(2, "eat", "לאכול", "verb", "I ___ lunch.", 2),
        WordEntity(3, "jump", "לקפוץ", "verb", "I ___ high.", 3),
        WordEntity(4, "happy", "שמח", "adjective", "I am ___.", 4),
        WordEntity(5, "sad", "עצוב", "adjective", "I am ___.", 5)
    )

    @Test
    fun `dailyQuiz returns one question per learned word with 4 options including the correct answer`() {
        val learnedToday = pool.take(2)

        val questions = QuizQuestionGenerator.dailyQuiz(learnedToday, pool, random = Random(3))

        assertThat(questions).hasSize(2)
        questions.forEach { q ->
            assertThat(q.options).hasSize(4)
            assertThat(q.options).contains(q.correctAnswer)
        }
    }

    @Test
    fun `weeklyQuiz returns translation-choice questions only, capped at questionCount`() {
        val questions = QuizQuestionGenerator.weeklyQuiz(pool, pool, questionCount = 3, random = Random(9))

        assertThat(questions).hasSize(3)
        questions.forEach { q ->
            assertThat(q.type).isEqualTo(QuestionType.TRANSLATION_CHOICE)
            assertThat(q.options).contains(q.correctAnswer)
        }
    }

    @Test
    fun `weeklyQuiz caps at available words when fewer than questionCount`() {
        val small = pool.take(2)

        val questions = QuizQuestionGenerator.weeklyQuiz(small, pool, questionCount = 5, random = Random(2))

        assertThat(questions).hasSize(2)
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.QuizQuestionGeneratorTest" --console=plain
```
Expected: FAIL — `QuizQuestionGenerator` unresolved.

- [ ] **Step 4: Implement `QuizQuestionGenerator`**

```kotlin
package com.ysompo.englishwords.logic

import com.ysompo.englishwords.data.WordEntity
import kotlin.random.Random

object QuizQuestionGenerator {

    fun dailyQuiz(learnedToday: List<WordEntity>, wordPool: List<WordEntity>, random: Random = Random): List<QuizQuestion> {
        return learnedToday.map { word -> buildQuestion(word, wordPool, useSentence = random.nextBoolean(), random) }
    }

    fun weeklyQuiz(
        candidateWords: List<WordEntity>,
        wordPool: List<WordEntity>,
        questionCount: Int = 5,
        random: Random = Random
    ): List<QuizQuestion> {
        return candidateWords.shuffled(random).take(questionCount).map { word ->
            buildQuestion(word, wordPool, useSentence = false, random)
        }
    }

    private fun buildQuestion(word: WordEntity, wordPool: List<WordEntity>, useSentence: Boolean, random: Random): QuizQuestion {
        return if (useSentence) {
            val distractors = DistractorSelector.sentenceCompletionDistractors(word, wordPool, count = 3, random = random)
            QuizQuestion(
                type = QuestionType.SENTENCE_COMPLETION,
                promptWord = word,
                questionText = word.exampleSentence,
                options = (distractors + word.word).shuffled(random),
                correctAnswer = word.word
            )
        } else {
            val distractors = DistractorSelector.translationDistractors(word, wordPool, count = 3, random = random)
            QuizQuestion(
                type = QuestionType.TRANSLATION_CHOICE,
                promptWord = word,
                questionText = word.word,
                options = (distractors + word.translationHe).shuffled(random),
                correctAnswer = word.translationHe
            )
        }
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.QuizQuestionGeneratorTest" --console=plain
```
Expected: PASS (3 tests). Note: with only 5 words in the test pool, `options` may have fewer than 4 entries if fewer than 3 distractors exist — that's why the real word bank (1000 words, Task 13) always has plenty of same-part-of-speech candidates. The test pool above has enough (2+ per part of speech) to always reach 4.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/logic/QuizQuestion.kt app/src/main/java/com/ysompo/englishwords/logic/QuizQuestionGenerator.kt app/src/test/java/com/ysompo/englishwords/logic/QuizQuestionGeneratorTest.kt
git commit -m "Add QuizQuestionGenerator for daily and weekly quizzes"
```

---

### Task 9: StreakCalculator

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/logic/StreakCalculator.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/logic/StreakCalculatorTest.kt`

Per design: a day is "complete" when both learning and the daily quiz are done. A week earns a star at **3 of 5** completed school days. A month is "fully starred" when every week that falls in it earned a star.

- [ ] **Step 1: Write the failing test**

```kotlin
package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import com.ysompo.englishwords.data.DailyCompletionEntity
import com.ysompo.englishwords.data.WeeklyStatusEntity
import org.junit.Test

class StreakCalculatorTest {

    private fun completion(date: String, done: Boolean) =
        DailyCompletionEntity(date, learningDone = done, quizDone = done, quizScore = if (done) 5 else 0)

    @Test
    fun `isDayComplete requires both learning and quiz done`() {
        assertThat(StreakCalculator.isDayComplete(null)).isFalse()
        assertThat(StreakCalculator.isDayComplete(DailyCompletionEntity("2026-08-10", learningDone = true, quizDone = false, quizScore = 0))).isFalse()
        assertThat(StreakCalculator.isDayComplete(DailyCompletionEntity("2026-08-10", learningDone = true, quizDone = true, quizScore = 5))).isTrue()
    }

    @Test
    fun `weekStarEarned is true at exactly 3 completed days out of 5`() {
        val threeDays = listOf(
            completion("2026-08-09", true),
            completion("2026-08-10", true),
            completion("2026-08-11", true),
            completion("2026-08-12", false),
            completion("2026-08-13", false)
        )
        assertThat(StreakCalculator.weekStarEarned(threeDays)).isTrue()
    }

    @Test
    fun `weekStarEarned is false at 2 completed days`() {
        val twoDays = listOf(
            completion("2026-08-09", true),
            completion("2026-08-10", true),
            completion("2026-08-11", false),
            completion("2026-08-12", false),
            completion("2026-08-13", false)
        )
        assertThat(StreakCalculator.weekStarEarned(twoDays)).isFalse()
    }

    @Test
    fun `isMonthFullyStarred requires every week in the list to have a star, and false when empty`() {
        assertThat(StreakCalculator.isMonthFullyStarred(emptyList())).isFalse()

        val allStarred = listOf(
            WeeklyStatusEntity("2026-08-02", 4, starEarned = true, weeklyQuizScore = 5),
            WeeklyStatusEntity("2026-08-09", 3, starEarned = true, weeklyQuizScore = 4)
        )
        assertThat(StreakCalculator.isMonthFullyStarred(allStarred)).isTrue()

        val oneMissing = allStarred + WeeklyStatusEntity("2026-08-16", 2, starEarned = false, weeklyQuizScore = 3)
        assertThat(StreakCalculator.isMonthFullyStarred(oneMissing)).isFalse()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.StreakCalculatorTest" --console=plain
```
Expected: FAIL — `StreakCalculator` unresolved.

- [ ] **Step 3: Implement `StreakCalculator`**

```kotlin
package com.ysompo.englishwords.logic

import com.ysompo.englishwords.data.DailyCompletionEntity
import com.ysompo.englishwords.data.WeeklyStatusEntity

object StreakCalculator {
    const val MIN_DAYS_FOR_STAR = 3

    fun isDayComplete(completion: DailyCompletionEntity?): Boolean =
        completion != null && completion.learningDone && completion.quizDone

    fun weekStarEarned(completionsForWeek: List<DailyCompletionEntity>): Boolean {
        val completedDays = completionsForWeek.count { it.learningDone && it.quizDone }
        return completedDays >= MIN_DAYS_FOR_STAR
    }

    fun isMonthFullyStarred(weeklyStatusesInMonth: List<WeeklyStatusEntity>): Boolean {
        return weeklyStatusesInMonth.isNotEmpty() && weeklyStatusesInMonth.all { it.starEarned }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.StreakCalculatorTest" --console=plain
```
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/logic/StreakCalculator.kt app/src/test/java/com/ysompo/englishwords/logic/StreakCalculatorTest.kt
git commit -m "Add StreakCalculator for daily/weekly/monthly completion rules"
```

---

### Task 10: BadgeCalculator (gamification — milestone badges)

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/logic/Badge.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/logic/BadgeCalculator.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/logic/BadgeCalculatorTest.kt`

This is the extra gamification layer the user asked for: collectible milestone badges by total words learned, shown on the Progress screen (Task 18) as unlocked/locked tiles, plus a "next badge" progress indicator on the Home screen (Task 15).

- [ ] **Step 1: Write `Badge`**

```kotlin
package com.ysompo.englishwords.logic

data class Badge(val id: String, val threshold: Int, val titleHe: String)
```

- [ ] **Step 2: Write the failing test**

```kotlin
package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BadgeCalculatorTest {

    @Test
    fun `unlockedBadges returns only badges at or below the learned count`() {
        val unlocked = BadgeCalculator.unlockedBadges(learnedCount = 30)

        assertThat(unlocked.map { it.id }).containsExactly("words_10", "words_25").inOrder()
    }

    @Test
    fun `unlockedBadges is empty below the first threshold`() {
        assertThat(BadgeCalculator.unlockedBadges(learnedCount = 5)).isEmpty()
    }

    @Test
    fun `unlockedBadges returns all badges when learned count reaches the max`() {
        val unlocked = BadgeCalculator.unlockedBadges(learnedCount = 1000)

        assertThat(unlocked).hasSize(BadgeCalculator.ALL_BADGES.size)
    }

    @Test
    fun `nextBadge returns the first locked badge, or null when all are unlocked`() {
        assertThat(BadgeCalculator.nextBadge(learnedCount = 30)?.id).isEqualTo("words_50")
        assertThat(BadgeCalculator.nextBadge(learnedCount = 1000)).isNull()
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.BadgeCalculatorTest" --console=plain
```
Expected: FAIL — `BadgeCalculator` unresolved.

- [ ] **Step 4: Implement `BadgeCalculator`**

```kotlin
package com.ysompo.englishwords.logic

object BadgeCalculator {
    val ALL_BADGES: List<Badge> = listOf(
        Badge("words_10", 10, "10 מילים!"),
        Badge("words_25", 25, "25 מילים!"),
        Badge("words_50", 50, "חצי מאה!"),
        Badge("words_100", 100, "100 מילים!"),
        Badge("words_200", 200, "200 מילים!"),
        Badge("words_350", 350, "350 מילים!"),
        Badge("words_500", 500, "חצי הדרך!"),
        Badge("words_750", 750, "750 מילים!"),
        Badge("words_1000", 1000, "אלוף האנגלית!")
    )

    fun unlockedBadges(learnedCount: Int): List<Badge> = ALL_BADGES.filter { learnedCount >= it.threshold }

    fun nextBadge(learnedCount: Int): Badge? = ALL_BADGES.firstOrNull { learnedCount < it.threshold }
}
```

- [ ] **Step 5: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.BadgeCalculatorTest" --console=plain
```
Expected: PASS (4 tests).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/logic/Badge.kt app/src/main/java/com/ysompo/englishwords/logic/BadgeCalculator.kt app/src/test/java/com/ysompo/englishwords/logic/BadgeCalculatorTest.kt
git commit -m "Add BadgeCalculator for word-count milestone gamification"
```

---

### Task 11: PronunciationMatcher

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/logic/PronunciationMatcher.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/logic/PronunciationMatcherTest.kt`

Per design: matching is flexible, not exact — "מספיק שהמערכת זיהתה את המילה בקרוב מספיק". Uses normalized string equality, then falls back to Levenshtein distance within a tolerance proportional to word length.

- [ ] **Step 1: Write the failing test**

```kotlin
package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PronunciationMatcherTest {

    @Test
    fun `exact match (case-insensitive) is a match`() {
        assertThat(PronunciationMatcher.isMatch("Happy", "happy")).isTrue()
        assertThat(PronunciationMatcher.isMatch("HAPPY", "happy")).isTrue()
    }

    @Test
    fun `recognized phrase containing the target word as a separate word is a match`() {
        assertThat(PronunciationMatcher.isMatch("the happy", "happy")).isTrue()
    }

    @Test
    fun `close mispronunciation within tolerance is a match`() {
        assertThat(PronunciationMatcher.isMatch("hapy", "happy")).isTrue() // 1 char off, tolerance >= 1
    }

    @Test
    fun `unrelated word is not a match`() {
        assertThat(PronunciationMatcher.isMatch("banana", "happy")).isFalse()
    }

    @Test
    fun `empty recognized text is never a match`() {
        assertThat(PronunciationMatcher.isMatch("", "happy")).isFalse()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.PronunciationMatcherTest" --console=plain
```
Expected: FAIL — `PronunciationMatcher` unresolved.

- [ ] **Step 3: Implement `PronunciationMatcher`**

```kotlin
package com.ysompo.englishwords.logic

object PronunciationMatcher {

    fun isMatch(recognizedText: String, targetWord: String): Boolean {
        val recognized = normalize(recognizedText)
        val target = normalize(targetWord)
        if (recognized.isEmpty()) return false
        if (recognized == target) return true
        if (recognized.split(" ").any { it == target }) return true

        val distance = levenshtein(recognized, target)
        val tolerance = maxOf(1, target.length / 4)
        return distance <= tolerance
    }

    private fun normalize(text: String): String =
        text.trim().lowercase().replace(Regex("[^a-z ]"), "")

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        return dp[a.length][b.length]
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.logic.PronunciationMatcherTest" --console=plain
```
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/logic/PronunciationMatcher.kt app/src/test/java/com/ysompo/englishwords/logic/PronunciationMatcherTest.kt
git commit -m "Add PronunciationMatcher for flexible speech-recognition matching"
```

---

### Task 12: Repositories — WordRepository, ProgressRepository

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/repo/WordRepository.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/repo/ProgressRepository.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/repo/WordRepositoryTest.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/repo/ProgressRepositoryTest.kt`

These tie the Room DAOs (Tasks 3-4) and the date logic (Task 5) together into the API the UI layer will call. This is the last task before content expansion and UI.

- [ ] **Step 1: Write `WordRepository`**

```kotlin
package com.ysompo.englishwords.repo

import android.content.Context
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.data.WordEntity
import com.ysompo.englishwords.data.WordSeeder

class WordRepository(private val db: AppDatabase) {
    suspend fun ensureSeeded(context: Context) {
        WordSeeder(context, db.wordDao()).seedIfNeeded()
    }

    suspend fun allWordsOrdered(): List<WordEntity> = db.wordDao().getAllOrdered()
}
```

- [ ] **Step 2: Write the failing test for `WordRepository`**

```kotlin
package com.ysompo.englishwords.repo

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.ysompo.englishwords.data.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WordRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: WordRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WordRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `ensureSeeded loads words once, allWordsOrdered returns them sorted`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        repository.ensureSeeded(context)
        repository.ensureSeeded(context) // calling twice must not duplicate rows

        val words = repository.allWordsOrdered()
        assertThat(words).hasSize(40)
        assertThat(words.first().orderIndex).isEqualTo(1)
    }
}
```

- [ ] **Step 3: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.repo.WordRepositoryTest" --console=plain
```
Expected: PASS (1 test).

- [ ] **Step 4: Write `ProgressRepository`**

```kotlin
package com.ysompo.englishwords.repo

import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.data.DailyCompletionEntity
import com.ysompo.englishwords.data.LearningProgressEntity
import com.ysompo.englishwords.data.WeeklyStatusEntity
import com.ysompo.englishwords.logic.WeekUtils
import java.time.LocalDate

class ProgressRepository(private val db: AppDatabase) {

    suspend fun learnedWordIds(): Set<Int> = db.learningProgressDao().getLearnedWordIds().toSet()

    suspend fun learnedWordCount(): Int = db.learningProgressDao().countLearned()

    suspend fun markWordsLearned(wordIds: List<Int>, date: LocalDate) {
        val dateText = WeekUtils.formatDate(date)
        wordIds.forEach { db.learningProgressDao().insert(LearningProgressEntity(it, dateText)) }
    }

    suspend fun recordDailyCompletion(date: LocalDate, learningDone: Boolean, quizDone: Boolean, quizScore: Int) {
        db.dailyCompletionDao().upsert(
            DailyCompletionEntity(WeekUtils.formatDate(date), learningDone, quizDone, quizScore)
        )
    }

    suspend fun completionForDate(date: LocalDate): DailyCompletionEntity? =
        db.dailyCompletionDao().getByDate(WeekUtils.formatDate(date))

    suspend fun completionsForWeek(weekStart: LocalDate): List<DailyCompletionEntity> {
        val weekEnd = weekStart.plusDays(6)
        return db.dailyCompletionDao().getBetween(WeekUtils.formatDate(weekStart), WeekUtils.formatDate(weekEnd))
    }

    suspend fun recordWeeklyStatus(weekStart: LocalDate, daysCompleted: Int, starEarned: Boolean, quizScore: Int?) {
        db.weeklyStatusDao().upsert(
            WeeklyStatusEntity(WeekUtils.formatDate(weekStart), daysCompleted, starEarned, quizScore)
        )
    }

    suspend fun allWeeklyStatuses(): List<WeeklyStatusEntity> = db.weeklyStatusDao().getAll()
}
```

- [ ] **Step 5: Write the failing test for `ProgressRepository`**

```kotlin
package com.ysompo.englishwords.repo

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.ysompo.englishwords.data.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class ProgressRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ProgressRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ProgressRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `markWordsLearned then learnedWordIds and learnedWordCount reflect them`() = runBlocking {
        repository.markWordsLearned(listOf(1, 2, 3), LocalDate.of(2026, 8, 10))

        assertThat(repository.learnedWordIds()).containsExactly(1, 2, 3)
        assertThat(repository.learnedWordCount()).isEqualTo(3)
    }

    @Test
    fun `recordDailyCompletion then completionForDate and completionsForWeek return it`() = runBlocking {
        val monday = LocalDate.of(2026, 8, 10)
        repository.recordDailyCompletion(monday, learningDone = true, quizDone = true, quizScore = 5)

        assertThat(repository.completionForDate(monday)?.quizScore).isEqualTo(5)

        val sunday = LocalDate.of(2026, 8, 9)
        assertThat(repository.completionsForWeek(sunday)).hasSize(1)
    }

    @Test
    fun `recordWeeklyStatus then allWeeklyStatuses returns it`() = runBlocking {
        repository.recordWeeklyStatus(LocalDate.of(2026, 8, 9), daysCompleted = 4, starEarned = true, quizScore = 5)

        val all = repository.allWeeklyStatuses()
        assertThat(all).hasSize(1)
        assertThat(all.first().starEarned).isTrue()
    }
}
```

- [ ] **Step 6: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.repo.ProgressRepositoryTest" --console=plain
```
Expected: PASS (3 tests).

- [ ] **Step 7: Run the full test suite**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --console=plain
```
Expected: PASS (all tests across every task so far).

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/repo/ app/src/test/java/com/ysompo/englishwords/repo/
git commit -m "Add WordRepository and ProgressRepository"
```

---

### Task 13: Expand Word Content from 40 to 1000 Words

**Files:**
- Modify: `app/src/main/assets/words.json`
- Create: `tools/validate_words.py`

At this point every logic and persistence layer is proven against the 40-word starter bank. This task replaces `words.json` with the full 1000-word bank and validates it structurally before it ever reaches the app.

- [ ] **Step 1: Write the validation script**

```python
# tools/validate_words.py
# Usage: python tools/validate_words.py app/src/main/assets/words.json
import json
import sys

REQUIRED_FIELDS = {"id", "word", "translation_he", "part_of_speech", "example_sentence", "order_index"}
ALLOWED_POS = {"noun", "verb", "adjective", "adverb", "pronoun", "preposition", "conjunction", "determiner", "interjection", "number"}

def validate(path: str) -> list[str]:
    errors = []
    with open(path, encoding="utf-8") as f:
        words = json.load(f)

    if not isinstance(words, list):
        return ["Top-level JSON must be an array"]

    seen_ids = set()
    seen_order_indexes = set()
    seen_words = set()

    for i, entry in enumerate(words):
        missing = REQUIRED_FIELDS - entry.keys()
        if missing:
            errors.append(f"Entry {i}: missing fields {missing}")
            continue

        if entry["id"] in seen_ids:
            errors.append(f"Entry {i}: duplicate id {entry['id']}")
        seen_ids.add(entry["id"])

        if entry["order_index"] in seen_order_indexes:
            errors.append(f"Entry {i}: duplicate order_index {entry['order_index']}")
        seen_order_indexes.add(entry["order_index"])

        word_lower = entry["word"].strip().lower()
        if word_lower in seen_words:
            errors.append(f"Entry {i}: duplicate word '{entry['word']}'")
        seen_words.add(word_lower)

        if entry["part_of_speech"] not in ALLOWED_POS:
            errors.append(f"Entry {i} ('{entry['word']}'): invalid part_of_speech '{entry['part_of_speech']}'")

        if "___" not in entry["example_sentence"]:
            errors.append(f"Entry {i} ('{entry['word']}'): example_sentence missing '___' placeholder")

        if not entry["translation_he"].strip():
            errors.append(f"Entry {i} ('{entry['word']}'): empty translation_he")

    expected_ids = set(range(1, len(words) + 1))
    if seen_ids != expected_ids:
        errors.append(f"ids are not a contiguous 1..N range: got {len(seen_ids)} unique ids for {len(words)} entries")

    return errors

if __name__ == "__main__":
    path = sys.argv[1] if len(sys.argv) > 1 else "app/src/main/assets/words.json"
    problems = validate(path)
    if problems:
        print(f"FAILED: {len(problems)} problem(s) found in {path}")
        for p in problems[:50]:
            print(f" - {p}")
        sys.exit(1)
    print(f"OK: {path} is valid")
```

- [ ] **Step 2: Run the validator against the current 40-word file to confirm it passes as a baseline**

```bash
python tools/validate_words.py app/src/main/assets/words.json
```
Expected: `OK: app/src/main/assets/words.json is valid`

- [ ] **Step 3: Generate the full 1000-word bank**

Replace `app/src/main/assets/words.json` with 1000 entries, `id`/`order_index` 1-1000, ordered from most frequent/useful English words to least frequent, each with an accurate Hebrew translation, a `part_of_speech` from the `ALLOWED_POS` set above, and a natural example sentence containing exactly one `___` placeholder where the target word belongs (matching the style of the 40 entries already in the file). Generate this directly — do not leave placeholder rows. Work in batches of roughly 100-150 words at a time (e.g. ranks 41-150, 151-300, 300-450, ...) so each batch can be visually spot-checked before moving to the next, and run Step 4 after every batch.

- [ ] **Step 4: Validate after every batch**

```bash
python tools/validate_words.py app/src/main/assets/words.json
```
Expected: `OK: ...` after each batch, with zero problems, and the entry count growing toward 1000.

- [ ] **Step 5: Update the two tests that hardcode the 40-word count**

In `app/src/test/java/com/ysompo/englishwords/data/WordSeederTest.kt`, change:
```kotlin
        assertThat(db.wordDao().count()).isEqualTo(40)
```
to:
```kotlin
        assertThat(db.wordDao().count()).isEqualTo(1000)
```

In `app/src/test/java/com/ysompo/englishwords/repo/WordRepositoryTest.kt`, change:
```kotlin
        assertThat(words).hasSize(40)
```
to:
```kotlin
        assertThat(words).hasSize(1000)
```

- [ ] **Step 6: Run the full test suite**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --console=plain
```
Expected: PASS (all tests, now seeding/reading 1000 words).

- [ ] **Step 7: Commit**

```bash
git add app/src/main/assets/words.json tools/validate_words.py app/src/test/java/com/ysompo/englishwords/data/WordSeederTest.kt app/src/test/java/com/ysompo/englishwords/repo/WordRepositoryTest.kt
git commit -m "Expand word bank from 40 to 1000 words with validation script"
```

---

### Task 14: TTS and SpeechRecognizer Wrappers

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/speech/TtsHelper.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/speech/SpeechRecognitionHelper.kt`

These wrap Android framework classes that require a real device/emulator to actually speak or listen — there is nothing here to unit test in this environment. Keep them thin (just wiring), so all decision logic (Task 11's `PronunciationMatcher`) stays independently tested. No test step in this task; manual verification happens in Task 20.

- [ ] **Step 1: Write `TtsHelper`**

```kotlin
package com.ysompo.englishwords.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            ready = true
        }
    }

    fun speak(word: String) {
        if (ready) {
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_utterance")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
```

- [ ] **Step 2: Write `SpeechRecognitionHelper`**

```kotlin
package com.ysompo.englishwords.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SpeechRecognitionHelper(private val context: Context) {
    private var recognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(onResult: (List<String>) -> Unit, onError: (Int) -> Unit) {
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle) {
                    val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf()
                    onResult(matches)
                }
                override fun onError(error: Int) = onError(error)
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }
        recognizer?.startListening(intent)
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }
}
```

- [ ] **Step 3: Build to confirm it compiles**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat assembleDebug --console=plain
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/speech/
git commit -m "Add TTS and SpeechRecognizer wrapper classes"
```

---

### Task 15: Home Screen (gamified)

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/ui/home/HomeViewModel.kt`
- Create: `app/src/main/res/layout/activity_home.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/ysompo/englishwords/MainActivity.kt` (becomes a thin launcher that forwards to `HomeActivity`)

Shows: today's status ("המילים של היום" / "כבר סיימת היום!"), a horizontal progress bar for words learned out of 1000, a streak "flame" counter (consecutive starred weeks), the next badge preview (from `BadgeCalculator`), and a weekly-quiz button that appears starting Thursday of each week (per the design: "בוחן שבועי... זמין החל מיום חמישי") until that week's quiz has been taken.

- [ ] **Step 1: Write `HomeViewModel`**

```kotlin
package com.ysompo.englishwords.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.logic.BadgeCalculator
import com.ysompo.englishwords.logic.StreakCalculator
import com.ysompo.englishwords.logic.WeekUtils
import com.ysompo.englishwords.repo.ProgressRepository
import com.ysompo.englishwords.repo.WordRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeState(
    val totalWords: Int,
    val learnedWords: Int,
    val todayComplete: Boolean,
    val starredWeekStreak: Int,
    val nextBadgeTitle: String?,
    val wordsUntilNextBadge: Int,
    val weeklyQuizAvailable: Boolean
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val wordRepository = WordRepository(db)
    private val progressRepository = ProgressRepository(db)

    val state = MutableLiveData<HomeState>()

    fun load() {
        viewModelScope.launch {
            wordRepository.ensureSeeded(getApplication())

            val totalWords = wordRepository.allWordsOrdered().size
            val learnedWords = progressRepository.learnedWordCount()
            val today = LocalDate.now()
            val todayCompletion = progressRepository.completionForDate(today)
            val todayComplete = StreakCalculator.isDayComplete(todayCompletion)

            val allStatuses = progressRepository.allWeeklyStatuses().sortedByDescending { it.weekStartDate }
            var streak = 0
            for (status in allStatuses) {
                if (status.starEarned) streak++ else break
            }

            val nextBadge = BadgeCalculator.nextBadge(learnedWords)

            val weekStart = WeekUtils.weekStartFor(today)
            val thisWeekThursday = weekStart.plusDays(4)
            val thisWeekAlreadyTaken = allStatuses.any { it.weekStartDate == WeekUtils.formatDate(weekStart) }
            val weeklyQuizAvailable = !today.isBefore(thisWeekThursday) && !thisWeekAlreadyTaken

            state.value = HomeState(
                totalWords = totalWords,
                learnedWords = learnedWords,
                todayComplete = todayComplete,
                starredWeekStreak = streak,
                nextBadgeTitle = nextBadge?.titleHe,
                wordsUntilNextBadge = nextBadge?.let { it.threshold - learnedWords } ?: 0,
                weeklyQuizAvailable = weeklyQuizAvailable
            )
        }
    }
}
```

- [ ] **Step 2: Write `activity_home.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="24dp"
    android:gravity="center_horizontal">

    <TextView
        android:id="@+id/streakText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="18sp"
        android:layout_marginTop="16dp"
        android:text="🔥 0 שבועות ברצף" />

    <ProgressBar
        android:id="@+id/wordsProgressBar"
        style="?android:attr/progressBarStyleHorizontal"
        android:layout_width="match_parent"
        android:layout_height="24dp"
        android:layout_marginTop="24dp"
        android:max="1000" />

    <TextView
        android:id="@+id/wordsProgressText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="0 מתוך 1000 מילים" />

    <TextView
        android:id="@+id/nextBadgeText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:textStyle="italic" />

    <Button
        android:id="@+id/startButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:text="התחל ללמוד היום" />

    <Button
        android:id="@+id/weeklyQuizButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="📝 בוחן שבועי"
        android:visibility="gone" />

    <Button
        android:id="@+id/progressButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="ההתקדמות שלי" />

    <Button
        android:id="@+id/settingsButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="הגדרות" />

</LinearLayout>
```

- [ ] **Step 3: Write `HomeActivity`**

```kotlin
package com.ysompo.englishwords.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.state.observe(this) { state ->
            binding.wordsProgressBar.max = state.totalWords
            binding.wordsProgressBar.progress = state.learnedWords
            binding.wordsProgressText.text = "${state.learnedWords} מתוך ${state.totalWords} מילים"
            binding.streakText.text = "🔥 ${state.starredWeekStreak} שבועות ברצף"
            binding.nextBadgeText.text = if (state.nextBadgeTitle != null) {
                "עוד ${state.wordsUntilNextBadge} מילים ל: ${state.nextBadgeTitle}"
            } else {
                "פתחת את כל התגים!"
            }
            binding.startButton.text = if (state.todayComplete) "כבר סיימת היום, כל הכבוד!" else "התחל ללמוד היום"
            binding.weeklyQuizButton.visibility = if (state.weeklyQuizAvailable) android.view.View.VISIBLE else android.view.View.GONE
        }

        binding.startButton.setOnClickListener {
            // LearnWordsActivity is wired in Task 17
        }
        binding.weeklyQuizButton.setOnClickListener {
            // QuizActivity (MODE_WEEKLY) is wired in Task 16
        }
        binding.progressButton.setOnClickListener {
            // ProgressActivity is wired in Task 18
        }
        binding.settingsButton.setOnClickListener {
            // SettingsActivity is wired in Task 19
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }
}
```

- [ ] **Step 4: Point the manifest's launcher activity at `HomeActivity` and simplify `MainActivity`**

In `app/src/main/AndroidManifest.xml`, replace the `<activity android:name=".MainActivity" ...>` block with:

```xml
        <activity
            android:name=".ui.home.HomeActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity android:name=".MainActivity" android:exported="false" />
```

- [ ] **Step 5: Build to confirm it compiles**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat assembleDebug --console=plain
```
Expected: `BUILD SUCCESSFUL`. (ViewBinding generates `ActivityHomeBinding` automatically from `activity_home.xml` — if the build fails on that import, re-run `assembleDebug`, ViewBinding classes are generated during the build.)

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/ui/home/ app/src/main/res/layout/activity_home.xml app/src/main/AndroidManifest.xml
git commit -m "Add gamified Home screen (progress bar, streak flame, next badge)"
```

---

### Task 16: Quiz Screen (gamified)

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/ui/quiz/QuizActivity.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/ui/quiz/QuizViewModel.kt`
- Create: `app/src/main/res/layout/activity_quiz.xml`
- Create: `app/src/main/java/com/ysompo/englishwords/ui/common/ConfettiView.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt` (wire the "weekly quiz" button added in Task 15)

Built before the Learn+Pronounce screen (Task 17) on purpose: Learn navigates into the quiz when a lesson finishes, so Quiz must exist first to avoid an unresolved forward reference. Shared between daily and weekly quizzes (`QuizQuestionGenerator` from Task 8). Shows immediate right/wrong feedback per answer (green/red flash) and a running score; on the final question, a lightweight dependency-free confetti burst plays via `ConfettiView`.

- [ ] **Step 1: Write `ConfettiView`, a minimal celebratory burst using only Android View animation (no new library)**

```kotlin
package com.ysompo.englishwords.ui.common

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import kotlin.random.Random

class ConfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val emojis = listOf("⭐", "🎉", "✨", "🏆")

    fun burst(particleCount: Int = 16) {
        val random = Random(System.currentTimeMillis())
        repeat(particleCount) {
            val particle = TextView(context).apply {
                text = emojis.random(random)
                textSize = 20f
            }
            val params = LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.CENTER
            addView(particle, params)

            val angle = random.nextDouble(0.0, 2 * Math.PI)
            val distance = 300f + random.nextFloat() * 200f
            val endX = (Math.cos(angle) * distance).toFloat()
            val endY = (Math.sin(angle) * distance).toFloat()

            val moveX = ObjectAnimator.ofFloat(particle, "translationX", 0f, endX)
            val moveY = ObjectAnimator.ofFloat(particle, "translationY", 0f, endY)
            val fade = ObjectAnimator.ofFloat(particle, "alpha", 1f, 0f)

            AnimatorSet().apply {
                playTogether(moveX, moveY, fade)
                duration = 900
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        removeView(particle)
                    }
                })
                start()
            }
        }
    }
}
```

- [ ] **Step 2: Write `QuizViewModel`**

```kotlin
package com.ysompo.englishwords.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.logic.QuizQuestion
import com.ysompo.englishwords.logic.QuizQuestionGenerator
import com.ysompo.englishwords.logic.StreakCalculator
import com.ysompo.englishwords.logic.WeekUtils
import com.ysompo.englishwords.repo.ProgressRepository
import com.ysompo.englishwords.repo.WordRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

data class QuizState(
    val questions: List<QuizQuestion>,
    val currentIndex: Int,
    val score: Int,
    val lastAnswerCorrect: Boolean?,
    val finished: Boolean
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val wordRepository = WordRepository(db)
    private val progressRepository = ProgressRepository(db)

    val state = MutableLiveData<QuizState>()

    fun loadDailyQuiz() {
        viewModelScope.launch {
            val allWords = wordRepository.allWordsOrdered()
            val learnedIds = progressRepository.learnedWordIds()
            val today = LocalDate.now()
            // Words learned today are exactly the ones marked in LearnWordsActivity moments ago.
            val learnedTodayList = allWords.filter { it.id in learnedIds }.takeLast(5)
            val questions = QuizQuestionGenerator.dailyQuiz(learnedTodayList, allWords)
            state.value = QuizState(questions, 0, 0, null, false)
        }
    }

    fun loadWeeklyQuiz() {
        viewModelScope.launch {
            val allWords = wordRepository.allWordsOrdered()
            val learnedIds = progressRepository.learnedWordIds()
            val learnedWords = allWords.filter { it.id in learnedIds }
            val questions = QuizQuestionGenerator.weeklyQuiz(learnedWords, allWords, questionCount = 5)
            state.value = QuizState(questions, 0, 0, null, false)
        }
    }

    fun submitAnswer(selected: String) {
        val current = state.value ?: return
        val question = current.questions[current.currentIndex]
        val correct = selected == question.correctAnswer
        val newScore = current.score + if (correct) 1 else 0
        val nextIndex = current.currentIndex + 1
        val finished = nextIndex >= current.questions.size

        state.value = current.copy(
            currentIndex = if (finished) current.currentIndex else nextIndex,
            score = newScore,
            lastAnswerCorrect = correct,
            finished = finished
        )
    }

    fun finishDailyQuiz(onDone: () -> Unit) {
        viewModelScope.launch {
            val current = state.value ?: return@launch
            val today = LocalDate.now()
            progressRepository.recordDailyCompletion(today, learningDone = true, quizDone = true, quizScore = current.score)
            onDone()
        }
    }

    fun finishWeeklyQuiz(onDone: () -> Unit) {
        viewModelScope.launch {
            val current = state.value ?: return@launch
            val today = LocalDate.now()
            val weekStart = WeekUtils.weekStartFor(today)
            val completions = progressRepository.completionsForWeek(weekStart)
            val starEarned = StreakCalculator.weekStarEarned(completions)
            progressRepository.recordWeeklyStatus(weekStart, completions.size, starEarned, current.score)
            onDone()
        }
    }
}
```

- [ ] **Step 3: Write `activity_quiz.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:padding="24dp"
        android:gravity="center">

        <TextView
            android:id="@+id/scoreText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="16sp"
            android:text="ניקוד: 0" />

        <TextView
            android:id="@+id/questionText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="28sp"
            android:layout_marginTop="24dp"
            android:gravity="center" />

        <LinearLayout
            android:id="@+id/optionsContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:layout_marginTop="24dp" />

        <TextView
            android:id="@+id/finishedText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="22sp"
            android:layout_marginTop="24dp"
            android:visibility="gone" />

        <Button
            android:id="@+id/doneButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="סיום"
            android:visibility="gone" />

    </LinearLayout>

    <com.ysompo.englishwords.ui.common.ConfettiView
        android:id="@+id/confettiView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</FrameLayout>
```

- [ ] **Step 4: Write `QuizActivity`**

```kotlin
package com.ysompo.englishwords.ui.quiz

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.databinding.ActivityQuizBinding
import com.ysompo.englishwords.ui.home.HomeActivity
import android.content.Intent

class QuizActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_QUIZ_MODE = "quiz_mode"
        const val MODE_DAILY = "daily"
        const val MODE_WEEKLY = "weekly"
    }

    private lateinit var binding: ActivityQuizBinding
    private lateinit var viewModel: QuizViewModel
    private var mode: String = MODE_DAILY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = intent.getStringExtra(EXTRA_QUIZ_MODE) ?: MODE_DAILY
        viewModel = ViewModelProvider(this)[QuizViewModel::class.java]
        viewModel.state.observe(this) { state -> render(state) }

        binding.doneButton.setOnClickListener {
            val onDone = {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            if (mode == MODE_DAILY) viewModel.finishDailyQuiz(onDone) else viewModel.finishWeeklyQuiz(onDone)
        }

        if (mode == MODE_DAILY) viewModel.loadDailyQuiz() else viewModel.loadWeeklyQuiz()
    }

    private fun render(state: QuizState) {
        binding.scoreText.text = "ניקוד: ${state.score}"

        if (state.finished) {
            binding.questionText.visibility = View.GONE
            binding.optionsContainer.visibility = View.GONE
            binding.finishedText.visibility = View.VISIBLE
            binding.doneButton.visibility = View.VISIBLE
            binding.finishedText.text = "סיימת! ${state.score} מתוך ${state.questions.size} נכון"
            binding.confettiView.burst()
            return
        }

        val question = state.questions[state.currentIndex]
        binding.questionText.text = if (question.type == com.ysompo.englishwords.logic.QuestionType.SENTENCE_COMPLETION) {
            question.questionText
        } else {
            "מה התרגום של: ${question.questionText}?"
        }

        binding.optionsContainer.removeAllViews()
        question.options.forEach { option ->
            val button = Button(this).apply {
                text = option
                setOnClickListener { onOptionSelected(this, option, question.correctAnswer) }
            }
            binding.optionsContainer.addView(button)
        }
    }

    private fun onOptionSelected(button: Button, selected: String, correctAnswer: String) {
        button.setBackgroundColor(if (selected == correctAnswer) Color.parseColor("#7CB342") else Color.parseColor("#E57373"))
        for (i in 0 until binding.optionsContainer.childCount) {
            binding.optionsContainer.getChildAt(i).isEnabled = false
        }
        binding.root.postDelayed({ viewModel.submitAnswer(selected) }, 600)
    }
}
```

- [ ] **Step 5: Add the activity to the manifest**

In `app/src/main/AndroidManifest.xml`, add inside `<application>`:

```xml
        <activity android:name=".ui.quiz.QuizActivity" android:exported="false" />
```

- [ ] **Step 6: Wire the Home screen's "weekly quiz" button**

In `app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt`, replace the empty `binding.weeklyQuizButton.setOnClickListener { ... }` body with:

```kotlin
        binding.weeklyQuizButton.setOnClickListener {
            startActivity(Intent(this, com.ysompo.englishwords.ui.quiz.QuizActivity::class.java).apply {
                putExtra(com.ysompo.englishwords.ui.quiz.QuizActivity.EXTRA_QUIZ_MODE, com.ysompo.englishwords.ui.quiz.QuizActivity.MODE_WEEKLY)
            })
        }
```

- [ ] **Step 7: Build to confirm it compiles**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat assembleDebug --console=plain
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/ui/quiz/ app/src/main/java/com/ysompo/englishwords/ui/common/ app/src/main/res/layout/activity_quiz.xml app/src/main/AndroidManifest.xml app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt
git commit -m "Add gamified Quiz screen (immediate feedback, confetti burst); wire weekly quiz button on Home"
```

---

### Task 17: Learn + Pronounce Screen (gamified)

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/ui/learn/LearnWordsActivity.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/ui/learn/LearnWordsViewModel.kt`
- Create: `app/src/main/res/layout/activity_learn_words.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt` (wire the "start" button)

Shows 5 word-progress "bubbles" (dots) that fill in as each word is mastered, plays the word via TTS, listens via `SpeechRecognitionHelper`, and shows a scale+color "success pop" (via `ObjectAnimator`, no new dependency) when `PronunciationMatcher.isMatch` succeeds.

- [ ] **Step 1: Write `LearnWordsViewModel`**

```kotlin
package com.ysompo.englishwords.ui.learn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.data.WordEntity
import com.ysompo.englishwords.logic.DailyLessonSelector
import com.ysompo.englishwords.logic.PronunciationMatcher
import com.ysompo.englishwords.repo.ProgressRepository
import com.ysompo.englishwords.repo.WordRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

data class LearnState(
    val words: List<WordEntity>,
    val currentIndex: Int,
    val currentWordMastered: Boolean
)

class LearnWordsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val wordRepository = WordRepository(db)
    private val progressRepository = ProgressRepository(db)

    val state = MutableLiveData<LearnState>()
    private val masteredIds = mutableSetOf<Int>()

    fun load() {
        viewModelScope.launch {
            val allWords = wordRepository.allWordsOrdered()
            val learnedIds = progressRepository.learnedWordIds()
            val todaysWords = DailyLessonSelector.nextWordsToLearn(allWords, learnedIds)
            state.value = LearnState(todaysWords, currentIndex = 0, currentWordMastered = false)
        }
    }

    fun onRecognitionResult(candidates: List<String>) {
        val current = state.value ?: return
        val targetWord = current.words[current.currentIndex]
        val matched = candidates.any { PronunciationMatcher.isMatch(it, targetWord.word) }
        if (matched) {
            masteredIds.add(targetWord.id)
            state.value = current.copy(currentWordMastered = true)
        }
    }

    fun advanceToNextWord() {
        val current = state.value ?: return
        val nextIndex = current.currentIndex + 1
        if (nextIndex < current.words.size) {
            state.value = current.copy(currentIndex = nextIndex, currentWordMastered = false)
        }
    }

    fun isLastWordMastered(): Boolean {
        val current = state.value ?: return false
        return current.currentIndex == current.words.lastIndex && current.currentWordMastered
    }

    fun finishLearning(onDone: () -> Unit) {
        viewModelScope.launch {
            progressRepository.markWordsLearned(masteredIds.toList(), LocalDate.now())
            onDone()
        }
    }
}
```

- [ ] **Step 2: Write `activity_learn_words.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="24dp"
    android:gravity="center">

    <LinearLayout
        android:id="@+id/progressDotsContainer"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginBottom="24dp" />

    <TextView
        android:id="@+id/wordText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="36sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/translationText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="20sp"
        android:layout_marginTop="8dp" />

    <TextView
        android:id="@+id/successText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="24sp"
        android:text="מעולה! 🎉"
        android:visibility="invisible"
        android:layout_marginTop="16dp" />

    <Button
        android:id="@+id/listenButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:text="🔊 השמע שוב" />

    <Button
        android:id="@+id/recordButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="🎤 עכשיו תגיד את המילה" />

    <Button
        android:id="@+id/nextButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:text="הבא"
        android:enabled="false" />

</LinearLayout>
```

- [ ] **Step 3: Write `LearnWordsActivity`**

```kotlin
package com.ysompo.englishwords.ui.learn

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.databinding.ActivityLearnWordsBinding
import com.ysompo.englishwords.speech.SpeechRecognitionHelper
import com.ysompo.englishwords.speech.TtsHelper

class LearnWordsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLearnWordsBinding
    private lateinit var viewModel: LearnWordsViewModel
    private lateinit var ttsHelper: TtsHelper
    private lateinit var speechHelper: SpeechRecognitionHelper
    private val dotViews = mutableListOf<View>()

    private val requestAudioPermission = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) startListening() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnWordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ttsHelper = TtsHelper(this)
        speechHelper = SpeechRecognitionHelper(this)
        viewModel = ViewModelProvider(this)[LearnWordsViewModel::class.java]

        viewModel.state.observe(this) { state -> render(state) }

        binding.listenButton.setOnClickListener {
            viewModel.state.value?.let { ttsHelper.speak(it.words[it.currentIndex].word) }
        }
        binding.recordButton.setOnClickListener { requestAudioAndListen() }
        binding.nextButton.setOnClickListener {
            if (viewModel.isLastWordMastered()) {
                viewModel.finishLearning {
                    startActivity(Intent(this, com.ysompo.englishwords.ui.quiz.QuizActivity::class.java).apply {
                        putExtra(com.ysompo.englishwords.ui.quiz.QuizActivity.EXTRA_QUIZ_MODE, com.ysompo.englishwords.ui.quiz.QuizActivity.MODE_DAILY)
                    })
                    finish()
                }
            } else {
                viewModel.advanceToNextWord()
            }
        }

        viewModel.load()
    }

    private fun render(state: LearnState) {
        if (dotViews.size != state.words.size) {
            binding.progressDotsContainer.removeAllViews()
            dotViews.clear()
            state.words.forEach { _ ->
                val dot = ImageView(this).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(24, 24).apply { marginEnd = 12 }
                    setBackgroundColor(Color.LTGRAY)
                }
                binding.progressDotsContainer.addView(dot)
                dotViews.add(dot)
            }
        }
        dotViews.forEachIndexed { index, dot ->
            dot.setBackgroundColor(if (index < state.currentIndex || (index == state.currentIndex && state.currentWordMastered)) Color.parseColor("#F5A623") else Color.LTGRAY)
        }

        val word = state.words[state.currentIndex]
        binding.wordText.text = word.word
        binding.translationText.text = word.translationHe
        binding.nextButton.isEnabled = state.currentWordMastered
        binding.successText.visibility = if (state.currentWordMastered) View.VISIBLE else View.INVISIBLE

        if (state.currentWordMastered) {
            playSuccessAnimation()
        }

        ttsHelper.speak(word.word)
    }

    private fun playSuccessAnimation() {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f, 1.2f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f, 1.2f, 1f)
        ObjectAnimator.ofPropertyValuesHolder(binding.successText, scaleX, scaleY).apply {
            duration = 400
            start()
        }
    }

    private fun requestAudioAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startListening() {
        speechHelper.startListening(
            onResult = { candidates -> viewModel.onRecognitionResult(candidates) },
            onError = { /* allow the child to simply try again */ }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.shutdown()
        speechHelper.destroy()
    }
}
```

- [ ] **Step 4: Add the activity to the manifest**

In `app/src/main/AndroidManifest.xml`, add inside `<application>`:

```xml
        <activity android:name=".ui.learn.LearnWordsActivity" android:exported="false" />
```

- [ ] **Step 5: Wire the Home screen's "start" button**

In `app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt`, replace the empty `binding.startButton.setOnClickListener { ... }` body with:

```kotlin
        binding.startButton.setOnClickListener {
            startActivity(Intent(this, com.ysompo.englishwords.ui.learn.LearnWordsActivity::class.java))
        }
```

(This requires `import android.content.Intent` at the top of `HomeActivity.kt`, which is already present from Task 15.)

- [ ] **Step 6: Build to confirm it compiles**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat assembleDebug --console=plain
```
Expected: `BUILD SUCCESSFUL` (`QuizActivity` already exists from Task 16, so this reference resolves cleanly).

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/ui/learn/ app/src/main/res/layout/activity_learn_words.xml app/src/main/AndroidManifest.xml app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt
git commit -m "Add gamified Learn+Pronounce screen (progress dots, success animation)"
```

---

### Task 18: Progress Screen (gamified — the main gamification hub)

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/ui/progress/ProgressActivity.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/ui/progress/ProgressViewModel.kt`
- Create: `app/src/main/res/layout/activity_progress.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt` (wire the "progress" button)

Shows a week-by-week star strip (⭐ for starred weeks, ☆ for not), the badge showcase (unlocked vs. locked from `BadgeCalculator`), and — this is the parent-facing signal from the design spec — a banner when the current month is fully starred (`StreakCalculator.isMonthFullyStarred`), telling the parent the 50₪ reward is due.

- [ ] **Step 1: Write `ProgressViewModel`**

```kotlin
package com.ysompo.englishwords.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.data.WeeklyStatusEntity
import com.ysompo.englishwords.logic.Badge
import com.ysompo.englishwords.logic.BadgeCalculator
import com.ysompo.englishwords.logic.StreakCalculator
import com.ysompo.englishwords.logic.WeekUtils
import com.ysompo.englishwords.repo.ProgressRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class ProgressState(
    val weeklyStatuses: List<WeeklyStatusEntity>,
    val learnedWordCount: Int,
    val unlockedBadges: List<Badge>,
    val lockedBadges: List<Badge>,
    val currentMonthFullyStarred: Boolean
)

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val progressRepository = ProgressRepository(db)

    val state = MutableLiveData<ProgressState>()

    fun load() {
        viewModelScope.launch {
            val allStatuses = progressRepository.allWeeklyStatuses().sortedBy { it.weekStartDate }
            val learnedCount = progressRepository.learnedWordCount()

            val currentMonth = YearMonth.from(LocalDate.now())
            val statusesInCurrentMonth = allStatuses.filter {
                YearMonth.from(WeekUtils.parseDate(it.weekStartDate)) == currentMonth
            }
            val monthFullyStarred = StreakCalculator.isMonthFullyStarred(statusesInCurrentMonth)

            val unlocked = BadgeCalculator.unlockedBadges(learnedCount)
            val locked = BadgeCalculator.ALL_BADGES - unlocked.toSet()

            state.value = ProgressState(allStatuses, learnedCount, unlocked, locked, monthFullyStarred)
        }
    }
}
```

- [ ] **Step 2: Write `activity_progress.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="24dp">

        <TextView
            android:id="@+id/monthlyRewardBanner"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="#FFF3CD"
            android:padding="16dp"
            android:textSize="16sp"
            android:gravity="center"
            android:visibility="gone"
            android:text="🎉 עמדת בכל השבועות החודש! מגיע לך 50 ש&quot;ח מאבא/אמא!" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:textSize="18sp"
            android:textStyle="bold"
            android:text="הכוכבים השבועיים שלי" />

        <TextView
            android:id="@+id/weeklyStarsText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:textSize="22sp" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:textSize="18sp"
            android:textStyle="bold"
            android:text="התגים שלי" />

        <LinearLayout
            android:id="@+id/badgesContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:layout_marginTop="8dp" />

    </LinearLayout>
</ScrollView>
```

- [ ] **Step 3: Write `ProgressActivity`**

```kotlin
package com.ysompo.englishwords.ui.progress

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.databinding.ActivityProgressBinding

class ProgressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProgressBinding
    private lateinit var viewModel: ProgressViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProgressViewModel::class.java]
        viewModel.state.observe(this) { state -> render(state) }
        viewModel.load()
    }

    private fun render(state: ProgressState) {
        binding.monthlyRewardBanner.visibility = if (state.currentMonthFullyStarred) View.VISIBLE else View.GONE

        binding.weeklyStarsText.text = state.weeklyStatuses.joinToString(" ") { if (it.starEarned) "⭐" else "☆" }
            .ifEmpty { "עוד לא הושלם אף שבוע" }

        binding.badgesContainer.removeAllViews()
        state.unlockedBadges.forEach { badge ->
            binding.badgesContainer.addView(TextView(this).apply {
                text = "🏅 ${badge.titleHe}"
                textSize = 16f
            })
        }
        state.lockedBadges.forEach { badge ->
            binding.badgesContainer.addView(TextView(this).apply {
                text = "🔒 ${badge.titleHe} (${badge.threshold} מילים)"
                textSize = 16f
                alpha = 0.5f
            })
        }
    }
}
```

- [ ] **Step 4: Add the activity to the manifest**

In `app/src/main/AndroidManifest.xml`, add inside `<application>`:

```xml
        <activity android:name=".ui.progress.ProgressActivity" android:exported="false" />
```

- [ ] **Step 5: Wire the Home screen's "progress" button**

In `app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt`, replace the empty `binding.progressButton.setOnClickListener { ... }` body with:

```kotlin
        binding.progressButton.setOnClickListener {
            startActivity(Intent(this, com.ysompo.englishwords.ui.progress.ProgressActivity::class.java))
        }
```

- [ ] **Step 6: Build to confirm it compiles**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat assembleDebug --console=plain
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/ui/progress/ app/src/main/res/layout/activity_progress.xml app/src/main/AndroidManifest.xml app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt
git commit -m "Add Progress screen (weekly stars, badge showcase, monthly reward banner)"
```

---

### Task 19: Settings Screen (reminder time)

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/settings/ReminderSettings.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/ui/settings/SettingsActivity.kt`
- Create: `app/src/main/res/layout/activity_settings.xml`
- Test: `app/src/test/java/com/ysompo/englishwords/settings/ReminderSettingsTest.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt` (wire the "settings" button)

Per the design (and the user's follow-up), the reminder default is **20:00**, changeable from Settings, stored in `SharedPreferences`.

- [ ] **Step 1: Write the failing test for `ReminderSettings`**

```kotlin
package com.ysompo.englishwords.settings

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReminderSettingsTest {

    @Test
    fun `defaults to 20_00 when nothing was saved`() {
        val settings = ReminderSettings(ApplicationProvider.getApplicationContext())

        assertThat(settings.getReminderHour()).isEqualTo(20)
        assertThat(settings.getReminderMinute()).isEqualTo(0)
    }

    @Test
    fun `setReminderTime persists and is read back`() {
        val settings = ReminderSettings(ApplicationProvider.getApplicationContext())

        settings.setReminderTime(hour = 18, minute = 30)

        assertThat(settings.getReminderHour()).isEqualTo(18)
        assertThat(settings.getReminderMinute()).isEqualTo(30)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.settings.ReminderSettingsTest" --console=plain
```
Expected: FAIL — `ReminderSettings` unresolved.

- [ ] **Step 3: Implement `ReminderSettings`**

```kotlin
package com.ysompo.englishwords.settings

import android.content.Context

class ReminderSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("reminder_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_MINUTE = "reminder_minute"
        const val DEFAULT_HOUR = 20
        const val DEFAULT_MINUTE = 0
    }

    fun getReminderHour(): Int = prefs.getInt(KEY_HOUR, DEFAULT_HOUR)

    fun getReminderMinute(): Int = prefs.getInt(KEY_MINUTE, DEFAULT_MINUTE)

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit().putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).apply()
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.settings.ReminderSettingsTest" --console=plain
```
Expected: PASS (2 tests).

- [ ] **Step 5: Write `activity_settings.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="24dp"
    android:gravity="center">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="18sp"
        android:text="שעת התזכורת היומית" />

    <TimePicker
        android:id="@+id/reminderTimePicker"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:timePickerMode="spinner" />

    <Button
        android:id="@+id/saveButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="שמור" />

</LinearLayout>
```

- [ ] **Step 6: Write `SettingsActivity`**

```kotlin
package com.ysompo.englishwords.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ysompo.englishwords.databinding.ActivitySettingsBinding
import com.ysompo.englishwords.notification.ReminderScheduler
import com.ysompo.englishwords.settings.ReminderSettings

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settings: ReminderSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settings = ReminderSettings(this)
        binding.reminderTimePicker.hour = settings.getReminderHour()
        binding.reminderTimePicker.minute = settings.getReminderMinute()

        binding.saveButton.setOnClickListener {
            val hour = binding.reminderTimePicker.hour
            val minute = binding.reminderTimePicker.minute
            settings.setReminderTime(hour, minute)
            ReminderScheduler.schedule(this, hour, minute)
            finish()
        }
    }
}
```

Note: this references `ReminderScheduler`, written in Task 20. Build verification for this task happens at the end of Task 20 instead of here, since the two are interdependent.

- [ ] **Step 7: Add the activity to the manifest**

In `app/src/main/AndroidManifest.xml`, add inside `<application>`:

```xml
        <activity android:name=".ui.settings.SettingsActivity" android:exported="false" />
```

- [ ] **Step 8: Wire the Home screen's "settings" button**

In `app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt`, replace the empty `binding.settingsButton.setOnClickListener { ... }` body with:

```kotlin
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, com.ysompo.englishwords.ui.settings.SettingsActivity::class.java))
        }
```

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/settings/ app/src/main/java/com/ysompo/englishwords/ui/settings/ app/src/main/res/layout/activity_settings.xml app/src/test/java/com/ysompo/englishwords/settings/ app/src/main/AndroidManifest.xml app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt
git commit -m "Add Settings screen for reminder time (default 20:00)"
```

---

### Task 20: Daily Reminder Notification

**Files:**
- Create: `app/src/main/java/com/ysompo/englishwords/notification/DailyReminderWorker.kt`
- Create: `app/src/main/java/com/ysompo/englishwords/notification/ReminderScheduler.kt`
- Test: `app/src/test/java/com/ysompo/englishwords/notification/DailyReminderWorkerTest.kt`
- Modify: `app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt` (request `POST_NOTIFICATIONS` on first launch, Android 13+; schedule the reminder)
- Modify: `app/src/main/res/values/strings.xml`

A `WorkManager` `CoroutineWorker` runs once a day at (or shortly after) the configured time; it checks whether today's `DailyCompletionEntity` shows `isDayComplete == true`, and if not, posts a notification.

- [ ] **Step 1: Write the failing test for the completion check the worker relies on**

This test exercises the exact logic the worker uses to decide whether to notify — `StreakCalculator.isDayComplete`, already implemented in Task 9 — combined with `ProgressRepository`, so we're really testing that the worker's core decision (`shouldNotify`) is correct without needing to run `WorkManager` itself (which needs an Android runtime harder to exercise headlessly).

```kotlin
package com.ysompo.englishwords.notification

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.repo.ProgressRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class DailyReminderWorkerTest {

    private lateinit var db: AppDatabase
    private lateinit var progressRepository: ProgressRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        progressRepository = ProgressRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `shouldNotify is true when today has no completion record`() = runBlocking {
        val result = DailyReminderWorker.shouldNotify(progressRepository, LocalDate.of(2026, 8, 10))
        assertThat(result).isTrue()
    }

    @Test
    fun `shouldNotify is false when today is already complete`() = runBlocking {
        val today = LocalDate.of(2026, 8, 10)
        progressRepository.recordDailyCompletion(today, learningDone = true, quizDone = true, quizScore = 5)

        val result = DailyReminderWorker.shouldNotify(progressRepository, today)

        assertThat(result).isFalse()
    }

    @Test
    fun `shouldNotify is true when learning is done but quiz is not`() = runBlocking {
        val today = LocalDate.of(2026, 8, 10)
        progressRepository.recordDailyCompletion(today, learningDone = true, quizDone = false, quizScore = 0)

        val result = DailyReminderWorker.shouldNotify(progressRepository, today)

        assertThat(result).isTrue()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.notification.DailyReminderWorkerTest" --console=plain
```
Expected: FAIL — `DailyReminderWorker` unresolved.

- [ ] **Step 3: Add the notification body strings**

Add these two lines inside `<resources>` in `app/src/main/res/values/strings.xml`:

```xml
    <string name="reminder_channel_name">תזכורת יומית</string>
    <string name="reminder_notification_text">היי! עוד לא תרגלת אנגלית היום 🙂</string>
```

- [ ] **Step 4: Implement `DailyReminderWorker`**

```kotlin
package com.ysompo.englishwords.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ysompo.englishwords.R
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.logic.StreakCalculator
import com.ysompo.englishwords.repo.ProgressRepository
import java.time.LocalDate

class DailyReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "daily_reminder"
        const val NOTIFICATION_ID = 1

        suspend fun shouldNotify(progressRepository: ProgressRepository, today: LocalDate): Boolean {
            val completion = progressRepository.completionForDate(today)
            return !StreakCalculator.isDayComplete(completion)
        }
    }

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val progressRepository = ProgressRepository(db)

        if (shouldNotify(progressRepository, LocalDate.now())) {
            postNotification()
        }
        return Result.success()
    }

    private fun postNotification() {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, applicationContext.getString(R.string.reminder_channel_name), NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentText(applicationContext.getString(R.string.reminder_notification_text))
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(NOTIFICATION_ID, notification)
        }
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --tests "com.ysompo.englishwords.notification.DailyReminderWorkerTest" --console=plain
```
Expected: PASS (3 tests).

- [ ] **Step 6: Implement `ReminderScheduler`**

```kotlin
package com.ysompo.englishwords.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

object ReminderScheduler {
    private const val WORK_NAME = "daily_reminder_work"

    fun schedule(context: Context, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var nextRun = now.toLocalDate().atTime(LocalTime.of(hour, minute))
        if (nextRun.isBefore(now)) {
            nextRun = nextRun.plusDays(1)
        }
        val initialDelay = Duration.between(now, nextRun)

        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(Duration.ofDays(1))
            .setInitialDelay(initialDelay)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
```

- [ ] **Step 7: Request `POST_NOTIFICATIONS` and schedule the reminder from `HomeActivity`**

In `app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt`, add these imports:

```kotlin
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.ysompo.englishwords.notification.ReminderScheduler
import com.ysompo.englishwords.settings.ReminderSettings
```

And add this near the top of the `HomeActivity` class body:

```kotlin
    private val requestNotificationPermission = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { /* no-op either way, reminder still schedules; just won't show without permission */ }
```

Then, at the end of `onCreate` (after the existing button listeners), add:

```kotlin
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val settings = ReminderSettings(this)
        ReminderScheduler.schedule(this, settings.getReminderHour(), settings.getReminderMinute())
```

- [ ] **Step 8: Build to confirm it compiles**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat assembleDebug --console=plain
```
Expected: `BUILD SUCCESSFUL`. This also completes Task 19's `SettingsActivity`, which references `ReminderScheduler`.

- [ ] **Step 9: Run the entire test suite one final time**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat testDebugUnitTest --console=plain
```
Expected: PASS — every test written across Tasks 2-20.

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/com/ysompo/englishwords/notification/ app/src/test/java/com/ysompo/englishwords/notification/ app/src/main/java/com/ysompo/englishwords/ui/home/HomeActivity.kt app/src/main/res/values/strings.xml
git commit -m "Add daily reminder notification via WorkManager (default 20:00)"
```

---

### Task 21: Final Build, Install, and Manual End-to-End Verification

**Files:** none (verification only).

Everything up to here has been verified by JVM/Robolectric unit tests. This task is the one place real-device behavior — TTS actually speaking, the microphone actually listening, a notification actually appearing at 20:00 — gets checked, and it must be done manually on the child's phone; it cannot be automated from this machine.

- [ ] **Step 1: Build the release-ready debug APK**

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
.\gradlew.bat assembleDebug --console=plain
```
Expected: `BUILD SUCCESSFUL`. The APK is at `app\build\outputs\apk\debug\app-debug.apk`.

- [ ] **Step 2: Install on the child's phone**

Enable "Install unknown apps" for the file manager or browser used to transfer the APK (Android Settings → Apps → Special access → Install unknown apps), then copy `app-debug.apk` to the phone (USB, email to self, or cloud drive) and tap it to install. Alternatively, with the phone connected via USB and USB debugging enabled (Settings → About phone → tap "Build number" 7 times → Developer options → USB debugging):

```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
& "C:\Users\ysomp\AndroidSdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
```

- [ ] **Step 3: Manual verification checklist**

Walk through each item on the actual device and confirm:

- [ ] App opens to the Home screen showing "0 מתוך 1000 מילים" and a "התחל ללמוד היום" button.
- [ ] Tapping "התחל ללמוד היום" shows the first word, speaks it aloud (TTS), and the 5 progress dots are all gray.
- [ ] Tapping "🎤 עכשיו תגיד את המילה" prompts for microphone permission the first time; after granting, saying the word aloud triggers the success animation and the corresponding dot turns orange.
- [ ] Saying an unrelated word does not trigger success (try it once to confirm it correctly rejects).
- [ ] After all 5 words, the app moves into the daily quiz automatically; each answer shows green (correct)/red (incorrect) feedback; the confetti burst plays at the end.
- [ ] Returning to Home now shows "5 מתוך 1000 מילים" and the button reads "כבר סיימת היום, כל הכבוד!".
- [ ] "ההתקדמות שלי" shows the badge list with "10 מילים!" still locked (🔒) since only 5 words are learned.
- [ ] "הגדרות" opens, shows 20:00 by default, and saving a new time (e.g. 2 minutes from now, for testing) does not crash.
- [ ] Waiting past the configured reminder time on a day where the lesson hasn't been done produces a system notification ("היי! עוד לא תרגלת אנגלית היום 🙂"). This can take up to ~15 minutes to fire even for "now" due to `WorkManager`'s minimum periodic-work granularity — that delay is expected Android platform behavior, not a bug.
- [ ] Repeat the daily flow on 3 different days within the same Sunday-Thursday week (adjust the device's system date if needed to simulate this quickly), then confirm "ההתקדמות שלי" shows a ⭐ for that week.

- [ ] **Step 4: Report back**

No commit for this task — if any checklist item fails, that's a bug to fix (write a failing unit test that reproduces it where possible, then fix, following the same TDD pattern as the earlier tasks) before considering the app done.
