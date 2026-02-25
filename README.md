# RuneMate MCP Server

An [MCP](https://modelcontextprotocol.io/) (Model Context Protocol) server that gives AI coding agents access to the Old School RuneScape game cache. Query item stats, NPC definitions, object configs, and more — directly from your editor.

## Prerequisites

- **Java 17+**
- **OSRS game cache** on disk (typically installed by [RuneLite](https://runelite.net/))

The server looks for the cache at `~/.runelite/jagexcache/oldschool/LIVE` by default. To use a different location, set the `OSRS_CACHE_DIR` environment variable:

```bash
export OSRS_CACHE_DIR=/path/to/jagexcache/oldschool/LIVE
```

## Setup

Download the latest `runemate-mcp-*-all.jar` from [Releases](https://github.com/EJ-K/runemate-mcp/releases), then add it to your MCP client config.

### Claude Code

Add to your Claude Code MCP settings (`~/.claude/claude_code_config.json`):

```json
{
  "mcpServers": {
    "runemate": {
      "command": "java",
      "args": ["-jar", "/path/to/runemate-mcp-1.0.0-all.jar"]
    }
  }
}
```

### Claude Desktop

Add to your Claude Desktop config (`claude_desktop_config.json`):

```json
{
  "mcpServers": {
    "runemate": {
      "command": "java",
      "args": ["-jar", "/path/to/runemate-mcp-1.0.0-all.jar"]
    }
  }
}
```

### From source

If you prefer to run from source instead of using the JAR:

```json
{
  "mcpServers": {
    "runemate": {
      "command": "./gradlew",
      "args": ["-q", "--console=plain", "run"],
      "cwd": "/path/to/runemate-mcp"
    }
  }
}
```

## Tools

### `cache-lookup`

Look up OSRS game cache definitions by ID or by filtering on field values.

**Parameters:**

| Parameter | Type    | Required | Description                                      |
|-----------|---------|----------|--------------------------------------------------|
| `type`    | string  | Yes      | Definition type (see supported types below)       |
| `id`      | integer | No       | Specific definition ID to load                    |
| `filter`  | object  | No       | Field name/value pairs to filter by               |
| `limit`   | integer | No       | Maximum results to return (default: 25)           |

Provide either `id` for a direct lookup or `filter` to search.

**Supported types:** `combatgauge`, `dbrow`, `dbtable`, `enum`, `inventory`, `item`, `npc`, `object`, `spotanim`, `struct`, `varbit`, `worldentity`

**Examples:**

```jsonc
// Load a specific NPC by ID
{ "type": "npc", "id": 239 }

// Search for NPCs by name (case-insensitive substring match)
{ "type": "npc", "filter": { "name": "Guard" }, "limit": 5 }

// Look up an item
{ "type": "item", "id": 4151 }

// Find objects by name
{ "type": "object", "filter": { "name": "Oak" }, "limit": 10 }

// Find NPCs with a specific action
{ "type": "npc", "filter": { "actions": "Pickpocket" }, "limit": 5 }
```

**Filter matching rules:**
- **Strings** — case-insensitive substring match (e.g. `"guard"` matches `"Guard captain"`)
- **Arrays** — matches if any element equals the value (case-insensitive)
- **Numbers/other** — exact match via string comparison

## Building

```bash
./gradlew assemble      # Compile the project
./gradlew shadowJar     # Build the fat JAR (build/libs/runemate-mcp-*-all.jar)
./gradlew test          # Run tests (integration tests require an OSRS cache on disk)
```

## License

[MIT](LICENSE)
