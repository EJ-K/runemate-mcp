# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew compileKotlin    # Compile
./gradlew run              # Run the MCP server (stdio transport)
./gradlew shadowJar        # Build fat JAR for distribution (build/libs/runemate-mcp-*-all.jar)
./gradlew test             # Run tests (integration tests require an OSRS cache on disk)
```

## Architecture

This is an MCP (Model Context Protocol) server written in Kotlin, using the MCP Kotlin SDK (`io.modelcontextprotocol:kotlin-sdk-server`) over stdio transport. It is designed to expose RuneMate OSRS game cache data to LLM clients via MCP tools.

- **Main.kt** — Entry point. Creates the `Server`, registers tools, and starts the stdio transport. The server communicates over stdin/stdout using JSON-RPC, so all logging must go to stderr (configured in `log4j2.xml`).
- **Tools.kt** — `Server.registerTools()` extension function where all MCP tools are registered via `server.addTool(...)`. New tools/features go here.
- **cache/CacheManager.kt** — Initializes the JagexCache from disk and creates all config loaders eagerly on startup.
- **cache/CacheQueryTool.kt** — Implements the `cache-lookup` tool. Uses Jackson for serialization and reflection-based direct field access for filtering.

Key SDK types used: `Server`, `ServerOptions`, `ServerCapabilities`, `Implementation`, `CallToolRequest`, `CallToolResult`, `TextContent` — all under `io.modelcontextprotocol.kotlin.sdk.server` and `io.modelcontextprotocol.kotlin.sdk.types`.

## Adding a New Tool

Call `addTool` on the `Server` inside `registerTools()`:

```kotlin
addTool(name = "tool-name", description = "What it does") { request ->
    CallToolResult(content = listOf(TextContent("result")))
}
```

## Constraints

- JVM 17 toolchain (matches RuneMate platform)
- Never write to stdout outside the MCP protocol — use Log4j2 which routes to stderr via `log4j2.xml`
- Logging: Log4j2 only (aligned with RuneMate conventions). SLF4J from the MCP SDK is bridged via `log4j-slf4j2-impl`.

## Versioning and Releases

- Follow [Semantic Versioning](https://semver.org/) (e.g. `1.0.0`, `1.1.0`, `2.0.0`).
- The version is set in `build.gradle.kts` (`version = "x.y.z"`). Use a plain version for releases, not `-SNAPSHOT`.
- Releases are published on GitHub with a git tag matching the version (e.g. tag `v1.0.0` for version `1.0.0`).
- The release artifact is the shadow JAR (`runemate-mcp-<version>-all.jar`), attached to the GitHub release.
- To publish a release:
  1. Set `version` in `build.gradle.kts` to the release version.
  2. Commit and tag: `git tag -a v<version> -m "v<version>"`
  3. Push: `git push origin main v<version>`
  4. Build: `./gradlew shadowJar`
  5. Create release: `gh release create v<version> build/libs/runemate-mcp-<version>-all.jar --title "v<version>"`
