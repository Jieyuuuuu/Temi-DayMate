## DayMate Module Development Plan

### 1. My Daily Schedule
- Goal: Help users clearly grasp daily activities; optional caregiver remote management.
- Main Features:
  - Visual timeline of activities
  - Photos/icons for activities
  - Task states (Complete / Skip / Snooze)
  - Voice reminders
- Technical Focus:
  - Jetpack Compose timeline UI
  - In-app full-screen reminders (overlay)
  - Room local storage; optional cloud sync later
- UI/UX:
  - Large buttons, clear icons
  - One-tap actions
- Data Flow:
  - Local DB first; optional cloud later
- Extensibility:
  - Custom activity types
- Implementation:
  - UI: Compose timeline
  - Storage: Room
  - Reminder: In-app overlay host (no system notifications)
  - Optional cloud: Firestore/Functions in future

---

### 2. Medication Reminder
- Goal: Ensure on-time medication; record adherence.
- Main Features:
  - Medication list (photo, name, dosage)
  - Voice prompts
  - Feedback: Taken / Missed / Snooze
  - Escalation to caregiver on repeated misses (optional)
- Technical Focus:
  - In-app overlay reminders (no AlarmManager on temi)
  - Local storage
- UI/UX:
  - Clear list with actions
- Data Flow:
  - Local records; optional cloud later
- Extensibility:
  - Multiple medications and complex dosage
- Implementation:
  - UI: Compose list + dialog
  - Storage: Room
  - Reminder: In-app overlay host
  - Caregiver push: FCM (future)

---

### 3. Meal Time & Food Log
- Goal: Help form healthy habits via simple logging.
- Main Features:
  - Three-meal reminders
  - Photo suggestions
  - Photo/mood journaling
  - Water intake
- Technical Focus:
  - Camera/Picker integration (future)
  - Local storage
- UI/UX:
  - Simple image-first interface
- Data Flow:
  - Local DB
- Extensibility:
  - Nutrition analysis (future)
- Implementation:
  - UI: Compose + Coil
  - Storage: Room
  - Reminder: overlay host

---

### 4. Exercise & Wall
- Goal: Encourage gentle exercise with gamification.
- Main Features:
  - Demos (text/video)
  - Voice motivation
  - Timer, optional GPS
  - Points system
- Technical Focus:
  - Video playback
  - GPS/Step count
- UI/UX:
  - Playful visuals
- Data Flow:
  - Local + optional cloud
- Implementation:
  - Video: ExoPlayer
  - GPS: FusedLocationProvider
  - Score: Room

---

### 5. Socialize
- Goal: Easy contact with family/friends.
- Main Features:
  - Big contact buttons
  - Voice dialing
- Technical Focus:
  - Contacts management
  - Speech recognition
  - Telephony integration
- UI/UX:
  - Large icons, simple flows
- Data Flow:
  - Local contacts
- Implementation:
  - ContactsContract API
  - SpeechRecognizer API
  - ACTION_CALL intent

---

### 6. Memory Games
- Goal: Train memory and cognition.
- Main Features:
  - Matching, spot-the-difference, sound recognition
  - Difficulty adjustment/adaptive
- Technical Focus:
  - Game logic
  - Scoring and difficulty algorithms
- UI/UX:
  - Animations, sound
- Data Flow:
  - Local scores
- Implementation:
  - Compose canvas or classic views
  - Lottie/Animator
  - SoundPool/MediaPlayer
  - Room

---

### 7. My Memories / Photo Journal
- Goal: Help recall important moments.
- Main Features:
  - Photo wall
  - Narration (TTS)
  - Local add only (no remote upload)
- Technical Focus:
  - Image management
  - TTS
- UI/UX:
  - Warm design, easy browsing
- Data Flow:
  - Local + optional cloud later
- Implementation:
  - Grid: Compose + Coil
  - TTS: TextToSpeech
  - Storage: Room (URI-based)

---

### 8. Sleep Tracker
- Goal: Track sleep and support healthy routines.
- Main Features:
  - Sleep time logging
  - Bedtime prompts
  - Morning mood check
- Technical Focus:
  - Time logging
  - Voice prompts
- UI/UX:
  - Soft colors, simple flow
- Data Flow:
  - Local DB
- Implementation:
  - Room + TTS
  - Optional: Google Fit (future)

---

### 9. Settings / Caregiver Mode
- Goal: Provide secure caregiver tools.
- Main Features:
  - Password protection
  - Edit schedules/medications
  - View logs
  - Track usage
- Technical Focus:
  - Permissions
  - Queries
  - Cloud sync (future)
- UI/UX:
  - Simple segments
- Implementation:
  - Auth: SharedPreferences / BiometricPrompt (optional)
  - Cloud: Firebase Auth/Firestore (future)
  - Logs: Room + queries

---

### Common Accessibility & Design
- TTS throughout
- High-contrast themes
- Multi-language via strings.xml and Locale
- Fixed SOS button at bottom
- Modular MVVM + Jetpack components

# DayMate 各模組開發規劃

## 1. 我的日程（My Daily Schedule）
- **目標**：協助患者清楚掌握每日活動，並可由照護者遠端管理。
- **主要功能**：
  - 視覺化時間軸顯示活動
  - 活動照片/圖示
  - 任務狀態（完成/跳過/稍後提醒）
  - 語音提醒
  - 照護者遠端編輯
- **技術重點**：
  - RecyclerView/Timeline UI
  - Notification/AlarmManager
  - Firebase/雲端同步
- **UI/UX**：
  - 大按鈕、清楚圖示
  - 完成/跳過一鍵操作
- **資料流**：
  - 本地資料庫 + 雲端同步
- **延展性**：
  - 支援自訂活動類型
- **開發方法**：
  - UI：RecyclerView + 自訂Adapter 實作時間軸
  - 資料儲存：Room（本地）+ Firebase Firestore（雲端同步）
  - 通知：AlarmManager + NotificationManager
  - 遠端同步：Firebase Cloud Functions

---

## 2. 藥物提醒（Medication Reminder）
- **目標**：確保患者準時服藥，並回報服藥狀態。
- **主要功能**：
  - 藥物清單（照片、名稱、劑量）
  - 語音提示
  - 服藥回饋（已服用/漏服/稍後）
  - 多次漏服自動通知照護者
- **技術重點**：
  - 定時提醒/鬧鐘
  - 通知推播
  - 資料同步
- **UI/UX**：
  - 圖片+文字清單
  - 回饋按鈕明顯
- **資料流**：
  - 本地紀錄 + 雲端同步
- **延展性**：
  - 支援多種藥物、複雜劑量
- **開發方法**：
  - UI：RecyclerView + Glide（圖片載入）
  - 資料儲存：Room + Firebase
  - 定時提醒：WorkManager 或 AlarmManager
  - 語音提示：TextToSpeech API
  - 通知照護者：Firebase Cloud Messaging

---

## 3. 用餐與飲食紀錄（Meal Time & Food Log）
- **目標**：協助患者記錄飲食，養成良好習慣。
- **主要功能**：
  - 定時三餐提醒
  - 圖片建議
  - 拍照/心情記錄
  - 喝水追蹤
- **技術重點**：
  - 相機/圖片選取
  - 本地資料儲存
- **UI/UX**：
  - 圖片選單、心情表情
- **資料流**：
  - 本地紀錄
- **延展性**：
  - 支援營養分析
- **開發方法**：
  - UI：ImageView + RecyclerView
  - 拍照：CameraX API
  - 圖片儲存：Room/SQLite
  - 心情記錄：自訂Dialog + Emoji表情
  - 定時提醒：AlarmManager

---

## 4. 運動與牆（Exercise & Wall）
- **目標**：鼓勵患者適度運動，並以遊戲化提升動機。
- **主要功能**：
  - 運動示範（圖文/影片）
  - 語音激勵
  - 計時、GPS追蹤
  - 遊戲化積分
- **技術重點**：
  - 影片播放
  - GPS/計步
  - 分數系統
- **UI/UX**：
  - 活潑色彩、積分顯示
- **資料流**：
  - 本地紀錄 + 雲端同步
- **延展性**：
  - 支援更多運動類型
- **開發方法**：
  - 影片播放：ExoPlayer
  - GPS：FusedLocationProviderClient (Google Play Services)
  - 計步：SensorManager
  - 分數儲存：Room + Firebase
  - 語音激勵：TextToSpeech API

---

## 5. 社交（Socialize）
- **目標**：方便患者與親友聯繫，降低孤獨感。
- **主要功能**：
  - 聯絡人照片大按鈕
  - 語音指令撥號
- **技術重點**：
  - 聯絡人管理
  - 語音辨識
  - 通話整合
- **UI/UX**：
  - 大圖示、簡單操作
- **資料流**：
  - 本地聯絡人
- **延展性**：
  - 支援視訊、訊息
- **開發方法**：
  - 聯絡人：ContactsContract API
  - 語音指令：SpeechRecognizer API
  - 撥號：Intent.ACTION_CALL
  - 視訊：WebRTC（如需）

---

## 6. 記憶遊戲（Memory Games）
- **目標**：訓練記憶力，提升認知。
- **主要功能**：
  - 配對遊戲、找不同、聲音辨識
  - 難度調整/自適應
- **技術重點**：
  - 遊戲邏輯
  - 分數與難度演算法
- **UI/UX**：
  - 動畫、音效
- **資料流**：
  - 本地紀錄
- **延展性**：
  - 新增遊戲類型
- **開發方法**：
  - 遊戲UI：Canvas 或 Jetpack Compose
  - 動畫：Lottie/Animator
  - 音效：SoundPool/MediaPlayer
  - 分數儲存：Room

---

## 7. 我的回憶（My Memories / Photo Journal）
- **目標**：協助患者回憶重要時刻，增進情感連結。
- **主要功能**：
  - 照片牆
  - 語音說明
  - 照護者遠端新增
- **技術重點**：
  - 圖片管理
  - 語音播放
  - 雲端同步
- **UI/UX**：
  - 溫馨風格、簡單瀏覽
- **資料流**：
  - 本地+雲端
- **延展性**：
  - 支援影片、故事
- **開發方法**：
  - 圖片牆：RecyclerView + Glide
  - 語音說明：TextToSpeech API
  - 雲端同步：Firebase Storage/Firestore

---

## 8. 睡眠追蹤（Sleep Tracker）
- **目標**：追蹤睡眠品質，協助健康管理。
- **主要功能**：
  - 睡眠時間紀錄
  - 睡前語音提示
  - 晨間心情追蹤
- **技術重點**：
  - 時間紀錄
  - 語音播放
- **UI/UX**：
  - 柔和色彩、簡單操作
- **資料流**：
  - 本地紀錄
- **延展性**：
  - 與穿戴裝置整合
- **開發方法**：
  - 睡眠紀錄：Room
  - 語音提示：TextToSpeech API
  - 穿戴裝置：Google Fit API（如需）

---

## 9. 設定 / 照護者專區（Settings / Caregiver Mode）
- **目標**：提供安全管理與照護者專屬功能。
- **主要功能**：
  - 密碼保護
  - 編輯日程/藥物
  - 查詢日誌
  - 追蹤互動狀態
  - 新增回憶
- **技術重點**：
  - 權限管理
  - 資料查詢
  - 雲端同步
- **UI/UX**：
  - 分區明確、操作簡單
- **資料流**：
  - 本地+雲端
- **延展性**：
  - 支援多照護者、多層權限
- **開發方法**：
  - 密碼保護：SharedPreferences + BiometricPrompt（如需）
  - 雲端同步：Firebase Auth/Firestore
  - 日誌查詢：Room + 查詢介面

---

## 無障礙與設計特點（共通）
- 全程語音旁白（TTS）：TextToSpeech API
- 高對比模式：自訂主題/Style
- 多語支援：strings.xml + Locale 設定
- 緊急 SOS 鈕：固定於底部，Intent 呼叫緊急聯絡人
- 模組化設計，方便擴充：MVVM 架構 + Jetpack 元件 