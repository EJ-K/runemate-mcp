# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew compileKotlin    # Compile
./gradlew run              # Run the MCP server (stdio transport)
./gradlew build            # Full build
```

## Architecture

This is an MCP (Model Context Protocol) server written in Kotlin, using the MCP Kotlin SDK (`io.modelcontextprotocol:kotlin-sdk-server`) over stdio transport. It is designed to expose RuneMate data to LLM clients via MCP tools.

- **Main.kt** — Entry point. Creates the `Server`, registers tools, and starts the stdio transport. The server communicates over stdin/stdout using JSON-RPC, so all logging must go to stderr (configured in `logback.xml`).
- **Tools.kt** — `Server.registerTools()` extension function where all MCP tools are registered via `server.addTool(...)`. New tools/features go here.

Key SDK types used: `Server`, `ServerOptions`, `ServerCapabilities`, `Implementation`, `CallToolRequest`, `CallToolResult`, `TextContent` — all under `io.modelcontextprotocol.kotlin.sdk.server` and `io.modelcontextprotocol.kotlin.sdk.types`.

## Adding a New Tool

Call `addTool` on the `Server` inside `registerTools()`:

```kotlin
addTool(name = "tool-name", description = "What it does") { request ->
    CallToolResult(content = listOf(TextContent("result")))
}
```

## Constraints

- JVM 21 toolchain
- Never write to stdout outside the MCP protocol — use `org.slf4j.LoggerFactory` which routes to stderr via logback
