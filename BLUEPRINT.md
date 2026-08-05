# Blueprint: OmniRoute / AI Studio Mobile IDE

## 1. Core Architecture & Infrastructure
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Dual-Server Isolation**: 
  - **AI Proxy Server**: A full Node.js server environment (similar to Termux execution) running OmniRoute strictly in the foreground. It is tied to the app's lifecycle, providing internal AI agents a resilient gateway to LLMs. Full Node.js support is mandatory due to the complexity and security requirements of the proxy.
  - **User App Server**: A separate local web server (NanoHTTPD) used exclusively for previewing the active thread's generated web apps. It spins up on demand via a top-bar "Play" button and shuts down when closed. 
  - *Network Binding*: Both servers bind exclusively to `localhost` to ensure strict isolation and prevent external network exposure unless explicitly authorized.
- **App Lifecycle & Execution**: Chat threads and the embedded proxy run strictly in the foreground. The currently opened chat thread always has execution priority.
- **Agentic Engine**: A multi-agent system (Architect, Coder, Reviewer) for building, debugging, and auditing code. Supports local AI models (via OmniRoute) alongside cloud providers.
- **Orchestration & State Management**: The Android app acts primarily as a UI frontend and orchestrator. It manages file I/O, UI state, and process lifecycles, delegating heavy API routing to OmniRoute and reasoning to the Agentic Engine.
- **Extensible Tool Infrastructure**: The core infrastructure includes a built-in tool system from the start. This allows tools (built-in, Firebase Python, Drive, GitHub MCPs) to be registered at the app level, ready to be utilized by the Agent logic once it is integrated.
  - **Tool Permissions & Security**: Includes explicit user-approval hooks for sensitive tool executions (e.g., file deletion, external network requests) to ensure safety prior to agent automation.
- **Memory/Context**: Modular memory architecture for persistent context retention.
- **Log Keeper**: An always-on, shell-level logging system built from the start. Accessible via a Floating Action Button (FAB) anchored to the bottom-left corner of the screen. It strictly captures error types, component failures, timestamps, stack traces, and crash states from the AI Agents and OmniRoute, but strictly filters out ALL passwords or credentials. Includes a master on/off switch and time filters. Instead of pruning older logs, it exports them to the device's Download folder before clearing the active log state.

## 2. Global UI Layout & Navigation
- **Global Sidebar (Left Drawer)**: Contains the list of multi-thread workspaces (each acting as an isolated repo) and access to Global Settings. Designed for single-user, local-first operation without complex user switching or external authentication mechanisms.
- **Global App Settings (The "Library")**: Accessed via the sidebar. Structured as a library with dedicated pages for:
  - **Skills**: Add/edit/delete specific agent capabilities.
  - **Tools & Plugins**: Manage standard tools and Plugins (a hybrid of skills, tools, and MCP).
  - **Memory & Artifacts**: Global memory configuration and saved artifacts.
  - **System Instructions**: Global guardrails and rules.
  - **Built Agents**: Library of custom-configured agents.
  - **Model Context Protocol (MCP)**: Configuration for MCP integrations (GitHub and Firebase Python included as default providers).
  - **Google Drive Archive**: Set up automatic or manual archiving of chat threads.
- **Fixed Bottom Navigation**: A pill-shaped bar fixed at the absolute bottom of the screen (not floating). It switches between **Chat** and **Code** views, accompanied by a 3-dot menu for workspace actions.
- **OmniRoute Dashboard**: An integrated WebView pointing to the embedded proxy's local web interface to manage API keys, routing, analytics, and connections to **local AI models** (e.g., Ollama, on-device models, LM Studio). OmniRoute natively handles on-device AI inference and routing.

## 3. Workspace Views (Thread Specific)
### A. Chat View (Google AI Studio Style)
- **Top Bar (Agent Switcher)**: Tap the active Agent to open an Agent Card and customize:
  - **Model Order**: Fallback chains (e.g., try Claude, then GPT-4).
  - **System Prompts**: Specific rules for the active agent.
  - **Schedules/Reactions**: Post-task automated actions (e.g., auto-lint after coding).
- **Chat Input**: A scrollable (not infinitely expandable) input box to ensure the bottom text is always visible. It features a "+" button for uploading attachments (`.txt`, `.zip`, `.pdf`, code files), and an **Agent/Model Selector Pill** built directly into the input bar to quickly switch agents or models before sending.
- **Chat Interface & History**:
  - Beneath the user's sent message, a detailed list of actions and files modified, added, or deleted is displayed.
  - **File Bottom Sheet**: Tapping any changed file in the chat history opens it in a bottom sheet view for quick inspection (similar to the Artifacts UI).
  - **Native File Revert**: The app's native file system records a history of file changes independently of the AI. After an AI reply, the UI provides a file diff view and a button to revert the workspace back to that exact state using the local app-level file history.
- **Thread Settings Page**: A dedicated 4-tab settings page for the active thread:
  1. **Universal**: Thread-level tools, skills, MCPs, plugins, and guardrails accessible to all agents.
  2. **Agents**: Manage the list of active agents in the thread.
  3. **Versions**: Access version history/snapshots for the workspace.
  4. **GitHub**: Configure GitHub integration specific to this thread.

### B. Code View (Acode Style)
- **File Tree (Right / End Drawer)**: Slide-out drawer on the right side showing repository structure, avoiding collision with the global left sidebar. Includes a search bar. The project root, folders, and files all have their own 3-dot context menus for file operations (create, delete, rename). Tapping a file opens it in the main editor *without* closing the drawer.
- **Main Editor**: A lightweight, standard text editor acting as the main view for inspecting or tweaking code.
  - **Live Generation View**: When an agent is writing code, the editor switches to a read-only live view (or displays a structured loading state) so the user can watch the code being generated in real-time or understand that a process is running.

## 4. Agent Capabilities & Export
- **Parallel Agent Execution (Antigravity Architecture)**: The IDE supports a parallel agentic model where a master orchestrator spawns and coordinates sub-agents to perform tasks concurrently (e.g., searching docs, generating code, running lint tests). This allows non-blocking background operations, streaming progress back to the main UI.
- **File Readers & Analysis**: Built-in tools for agents to process user uploads.
- **Code Sandboxing**: Secure execution environments for tools and scripts:
  - **On-Device JS Sandbox**: For local, lightweight JavaScript execution.
  - **Cloud Firebase Python Sandbox**: Temporary, secure Python environment.
- **Artifacts**: Support for generating standalone small web apps. Artifacts have the capability to embed AI logic directly within them.
  - **Design Studio (UI Map Artifact)**: A specialized visual artifact accessible from the main sidebar. Allows users to create clickable UI interface maps (e.g., "tap note card -> settings screen"). These preview web apps are highly customizable (adjustable button sizes, fonts, colors). The tool divides designs into screens/menus and exports them as well-structured JSON, code, or images (to prevent AI hallucination) to serve as exact UI blueprints for building native Kotlin shells in chat.
- **Export & Deployment**:
  - **GitHub Integration**: Export directly to repos using a PAT or OAuth App.
  - **ZIP Export**: Package the local workspace into a `.zip` for manual extraction.

## 6. CI/CD & Build Pipeline
- **GitHub Actions Integration**: The Android APK is strictly built using a GitHub Actions workflow upon pushing to `main` or manual dispatch.
- **Workflow configuration**: Uses `ubuntu-latest`, JDK 21, and Gradle setup. It automatically generates a transient debug keystore (`keytool -genkey`) during the workflow to bypass local keystore credential risks and builds the APK using `gradle assembleDebug --no-daemon --no-configuration-cache`.

## 5. Development Phases
- **Phase 1 (Completed)**: Setup project foundation, UI skeleton (Sidebar, Fixed Bottom Navigation, Dual-tab layout), Local File System tracking, and the shell-level **Log Keeper**.
- **Phase 2 (Completed)**: Implement the NanoHTTPD preview server and the Extensible Tool Infrastructure (building the hooks and empty spaces for tools, skills, and MCPs).
- **Phase 3**: Build the complete Chat Interface, File Explorer, and Code Editor, wiring them directly to the local file system.
- **Phase 4**: Implement Agent Logic, Diff Parsing (The Brain), and OmniRoute Integration, connecting them into the pre-built UI and tool infrastructure. Includes building the Parallel Agent Execution (Antigravity) orchestrator for handling concurrent sub-agent tasks.
  - **OmniRoute Environment Requirements**: Requires embedding a pre-compiled Node.js binary (v20.20.2+, ideally v24.x LTS; excluding v21/v23) compiled for `arm64-v8a` architecture.
  - **Dependency Fallbacks**: OmniRoute utilizes `bcryptjs` (which is pure JavaScript and safe) and `better-sqlite3` (which typically requires native C/C++ bindings). To run locally on a mobile environment without native build tools, OmniRoute must be configured to fall back to a pure JavaScript SQLite engine (e.g., `node:sqlite` for Node 22+ or `sql.js` WASM).
- **Phase 5**: Build Global Library settings and Thread Settings (4-tab structure). Implement the Three Layers of Tool Permissions for AI Agents (Always Ask, Use Freely, No Permission) configurable both globally and per-thread.
- **Phase 6**: Wire up advanced cloud capabilities (Firebase Python Sandbox, GitHub export, Drive Archive, Artifact embeddings).
- **Phase 7**: Build the Design Studio / UI Map Artifact tool for generating UI reference blueprints.
