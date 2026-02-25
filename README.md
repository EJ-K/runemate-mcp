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

### `cache-schema`

List the available field names and types for a cache definition type, so you know which fields can be used with `cache-lookup` filters. Call with no arguments to list all supported types.

**Parameters:**

| Parameter | Type   | Required | Description                                          |
|-----------|--------|----------|------------------------------------------------------|
| `type`    | string | No       | The definition type to inspect. Omit to list all types. |

**Examples:**

```jsonc
// List all supported cache types
{}

// Get fields for item definitions
{ "type": "item" }

// Get fields for NPC definitions
{ "type": "npc" }
```

Returns each field's name and type (e.g. `String`, `int`, `int[]`, `Map<Integer, Object>`), making it easy to construct accurate `cache-lookup` filters.

### `api-lookup`

Search the RuneMate game API for classes, methods, and fields by keyword. Returns matching class definitions with their method signatures and Javadoc descriptions.

**Parameters:**

| Parameter | Type    | Required | Description                                      |
|-----------|---------|----------|--------------------------------------------------|
| `query`   | string  | Yes      | Class name, method name, or keyword to search    |
| `type`    | string  | No       | Filter by `class`, `interface`, or `enum`        |
| `limit`   | integer | No       | Maximum results to return (default: 10)          |

**Examples:**

```jsonc
// Find the Npcs utility class
{ "query": "Npcs" }

// Search for query builder classes
{ "query": "newQuery", "limit": 5 }

// Find prayer-related enums
{ "query": "prayer", "type": "enum" }

// Look up coordinate/location classes
{ "query": "Coordinate" }
```

Results include class name, package, kind (class/interface/enum), Javadoc description, superclass, interfaces, and all public/protected members with their signatures. ID constant classes (e.g., `ItemID`, `NpcID`) include the class entry but not individual constants — use `cache-lookup` for those.

### `docs-search`

Search the RuneMate developer documentation by keyword. Returns the most relevant documentation sections ranked by term frequency.

**Parameters:**

| Parameter | Type    | Required | Description                                      |
|-----------|---------|----------|--------------------------------------------------|
| `query`   | string  | Yes      | Keyword(s) to search for                         |
| `limit`   | integer | No       | Maximum results to return (default: 5)           |

**Examples:**

```jsonc
// Search for querying documentation
{ "query": "query builder" }

// Find pathfinding-related docs
{ "query": "pathfinding", "limit": 3 }

// Look up settings/configuration
{ "query": "settings gradle plugin" }
```

Results include `path`, `title`, `section`, and `content` fields for each matching documentation section. Sections are split by `##` headings and scored by keyword occurrences (heading matches weighted 3x over content).

## Building

```bash
./gradlew assemble      # Compile the project
./gradlew shadowJar     # Build the fat JAR (build/libs/runemate-mcp-*-all.jar)
./gradlew test          # Run tests (integration tests require an OSRS cache on disk)
```

## License

[MIT](LICENSE)
