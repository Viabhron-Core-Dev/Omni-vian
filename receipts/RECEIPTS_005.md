2026-08-05T11:23:00-07:00
* Requested: Add Global Sidebar placeholder replacements, PWA bottom sheet preview, and Artifacts to the unmarked phases / backlog in the blueprint. 
* Files touched: `BLUEPRINT.md`
* Action: Updated `BLUEPRINT.md` to include Global Sidebar UI completion, Artifacts implementation, and PWA Bottom Sheet Preview to the 'Unmarked Phases / Backlog' section as separate granular phases.
* Verification: Not tested (blueprint update only).
* Deviation: None.
* Known issue/Follow-up: Need to begin implementation phase 3 as discussed.
* 2026-08-14
* Dropped the "Just Discuss" lock and implemented deep C++ to Kotlin logging via `LogKeeper`.
* Implemented `JNI_OnLoad` in `llama_bridge.cpp` to cache the `JavaVM` and a global reference to the `LlamaEngine` class.
* Updated `LlamaEngine.kt` to expose a `@JvmStatic onNativeLog` method that routes direct to `LogKeeper.log`.
* Rewrote C++ `LOGI` and `LOGE` macros to push formatted logs across the JNI barrier using `CallStaticVoidMethod`.
* Hooked into `llama_log_set()` to intercept all internal `ggml` and `llama.cpp` neural network logs and route them straight into the Android LogKeeper interface.
