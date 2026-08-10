* 2026-08-10T05:40:00-07:00
* Request: Phase 9.7: The Unkillable Local Proxy (Foreground Service)
* Touched: app/src/main/AndroidManifest.xml, app/src/main/java/com/example/engine/server/PreviewServerManager.kt, app/src/main/java/com/example/ui/chat/ArtifactsListBottomSheet.kt, app/src/main/java/com/example/engine/omniroute/service/OmniRouteProxyServer.kt, app/src/main/java/com/example/engine/omniroute/service/OmniRouteProxyService.kt, app/src/main/java/com/example/MainActivity.kt
* Action: Updated PreviewServer port to 8081 to free up 8080. Added Foreground Service permissions to Manifest. Created OmniRouteProxyServer (NanoHTTPD) running on port 8080. Created OmniRouteProxyService to host the proxy as a Foreground Service with a persistent notification. Launched the service on app boot in MainActivity.
* Verification: Compiling now.
* Build completed successfully. Verified the fix locally.
* 2026-08-10T06:02:00-07:00
* Request: Fix Cleartext issue, Log Keeper, Chat Input Model Selector, and Ai Settings Models placeholder
* Touched: app/src/main/AndroidManifest.xml, app/src/main/java/com/example/ui/chat/OmniRouteClient.kt, app/src/main/java/com/example/ui/chat/ChatScreen.kt, app/src/main/java/com/example/ui/settings/omniroute/AiManagerPanelScreen.kt
* Action: Added `usesCleartextTraffic="true"` to Manifest. Integrated LogKeeper calls in OmniRouteClient. Replaced dummy clickable in ChatScreen with a DropdownMenu for model selection. Implemented `ModelsTab()` in AiManagerPanelScreen to replace the CenterTextTab placeholder. Fixed minor compiler errors.
* Verification: Compiling now.
* Build completed successfully. Verified locally.
