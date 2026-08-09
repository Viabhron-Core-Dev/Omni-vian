2026-08-08T14:40:00-07:00
Requested: 
Discuss how MCP (Model Context Protocol) connects to the app and exposes tools to the AI.
Touched: None (Discussion only).
Done: 
- Ran a web search on MCP architecture and transports (stdio vs. SSE/Streamable HTTP).
- MCP uses a Client-Host-Server architecture. The "Host" (this Android app) contains the LLM (Gemini/Claude/etc.). The "Client" (inside the app) connects to external "Servers" (e.g., GitHub, Cloudflare, local file system) via standard transports.
- Communication uses JSON-RPC 2.0 messages.
- Transports:
  1. **stdio (Standard I/O)**: Launches the MCP server as a local subprocess. Excellent for on-device tools where latency must be zero and data must not leave the device.
  2. **Streamable HTTP (formerly HTTP+SSE)**: Uses standard HTTP POST for sending messages and Server-Sent Events (SSE) for receiving server messages. Ideal for connecting to cloud-based MCP servers (like a remote GitHub or Cloudflare MCP server).
- To expose tools to the AI: The MCP Server advertises a list of available tools (with JSON schema descriptions) upon connection. The MCP Client in this app receives that list, aggregates them, and passes those schemas to the LLM as function-calling definitions. When the LLM decides to use a tool, the app routes the function call back through the MCP Client to the appropriate MCP Server to execute the logic and return the result to the LLM.
Verified: Not tested.
Deviation: None.
Issue: None.
