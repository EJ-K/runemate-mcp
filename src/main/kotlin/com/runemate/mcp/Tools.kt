package com.runemate.mcp

import com.runemate.mcp.cache.registerCacheLookupTool
import io.modelcontextprotocol.kotlin.sdk.server.Server

/**
 * Registers all MCP tools on the server.
 */
fun Server.registerTools() {
    registerCacheLookupTool()
}
