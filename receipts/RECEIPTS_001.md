# Receipts Log

2026-08-05T09:14:35-07:00
* Requested: Add OmniRoute's Node.js binary and dependency requirements to the blueprint phase where OmniRoute is built.
* Files touched: `/BLUEPRINT.md`
* Action: Added sub-bullets under Phase 4 detailing the requirement for a pre-compiled Node.js binary (v20.20.2+, arm64-v8a) and the fallback configuration needed for `better-sqlite3` to use a pure JavaScript engine (`node:sqlite` or `sql.js`) to avoid native build tool requirements. `bcryptjs` was noted as pure JS and safe.
* Verification: Not tested. (Documentation update only).
* Deviation: None.
* Known issue/Follow-up: Need to source the correct `arm64-v8a` Node.js binary during implementation.

* 2026-08-12
* Apply OmniRoot renaming, update settings list, and add descriptive model categorization
* Edited `GlobalSettingsScreen.kt`, `AppDatabase.kt`, `AiModelEntity.kt`, `AiManagerViewModel.kt`, `AiManagerPanelScreen.kt`
* Renamed OmniRoute to OmniRoot in settings and made it the first item in "Core Setup". Added `inputType` and `outputType` to `AiModelEntity`. Updated database version to 6. Updated `AiManagerViewModel` to infer input and output types from model IDs and save them to the database. Refactored `ModelsTab` in `AiManagerPanelScreen` to group models by provider, display type icons, and add sorting by name or type.
* Verified via local build compilation (`compile_applet`).

* 2026-08-12
* Implement Phase 9.12 Model Rater feature.
* Edited `ModelRatingDao.kt` (created), `AppDatabase.kt`, `AiManagerViewModel.kt`, `AiManagerPanelScreen.kt`, `ChatMessageEntity.kt`, `ChatMessageMapper.kt`, `ChatScreen.kt`
* Created `ModelRatingDao` to persist thumbs up/down model ratings. Plumbed ratings through `AiManagerViewModel.kt`. Updated `ChatScreen.kt` to capture the current `modelName` and `providerId` on AI response generation, stored persistently via `ChatMessageEntity.kt`. Added ThumbsUp/ThumbsDown action buttons on AI chat bubbles that record ratings. Added `ModelRaterTab` to `AiManagerPanelScreen` to visualize the model rating leaderboard.
* Verified via local build compilation (`compile_applet`).

* 2026-08-12
* Apply structural and logic fixes to OmniRoot implementation based on review.
* Edited `ModelRatingEntity.kt`, `AiManagerViewModel.kt`, `ChatScreen.kt`, `AppDatabase.kt`, `AndroidManifest.xml`, and renamed directories `omniroute` to `omniroot`.
* Replaced `fallbackToDestructiveMigration` with explicit Room `Migration` objects (v5 -> v6 -> v7 -> v8). Fixed the infinite rating UI exploit by binding the model rating insertion to the actual `message.id` as its primary key. Fixed the "Select Model" dropdown leak by providing fallback model IDs before inserting into the database. Cleaned up structural naming debt by moving directories and fixing all `omniroute` references to `omniroot` globally.
* Verified via local build compilation (`compile_applet`).

* 2026-08-12
* Implement OmniRoot proxy failover routing, payload translation, and token metrics tracking. 
* Edited `OmniRootProxyServer.kt`, `CompressionEngine.kt`, and `TranslatorTab.kt`. 
* Unified Phase 9.4, 9.5, 9.6, and 9.7 into a single logic pipeline. The `OmniRootProxyServer` now runs a `try-catch` fallback loop. When a user requests a Combo Route, it evaluates the fallback chain array from the Room database, maps the format for the provider via `TranslationEngine`, handles rate limits (HTTP 429 and 500 errors) by shifting to the next provider, and logs tokens via `CompressionEngine.estimateTokens()` to `MetricsDao` on success. Created the `TranslatorTab` UI for building Fallback Chains and testing payload format translations safely before making network calls.
* Verified via local compilation build (`compile_applet`).
