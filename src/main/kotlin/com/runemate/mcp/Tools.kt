package com.runemate.mcp

import com.runemate.mcp.api.registerApiLookupTool
import com.runemate.mcp.cache.registerCacheLookupTool
import com.runemate.mcp.docs.registerDocsSearchTool
import io.modelcontextprotocol.kotlin.sdk.server.Server

/**
 * Registers all MCP tools on the server.
 */
fun Server.registerTools() {
    registerCacheLookupTool()
    registerDocsSearchTool()
    registerApiLookupTool()
}
