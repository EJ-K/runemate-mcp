package com.runemate.mcp.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.*

private val SUPPORTED_TYPES = CacheManager.loaders.keys.sorted()

internal val objectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

fun Server.registerCacheLookupTool() {
    addTool(
        name = "cache-lookup",
        description = "Look up OSRS game cache definitions (items, NPCs, objects, etc.) by ID or by filtering on field values.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("type", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("The definition type to look up"))
                    putJsonArray("enum") { SUPPORTED_TYPES.forEach { add(it) } }
                })
                put("id", buildJsonObject {
                    put("type", JsonPrimitive("integer"))
                    put("description", JsonPrimitive("Specific config ID to load"))
                })
                put("filter", buildJsonObject {
                    put("type", JsonPrimitive("object"))
                    put("description", JsonPrimitive("Field name to value map for filtering results (e.g. {\"name\": \"Guard\"})"))
                    putJsonObject("additionalProperties") {
                        put("type", JsonPrimitive("string"))
                    }
                })
                put("limit", buildJsonObject {
                    put("type", JsonPrimitive("integer"))
                    put("description", JsonPrimitive("Maximum number of results to return (default 25)"))
                })
            },
            required = listOf("type"),
        ),
    ) { request ->
        val args = request.arguments ?: buildJsonObject {}
        val type = args["type"]?.jsonPrimitive?.content
            ?: return@addTool errorResult("Missing required parameter: type")

        val loader = CacheManager.loaders[type]
            ?: return@addTool errorResult("Unknown type '$type'. Supported types: $SUPPORTED_TYPES")

        val id = args["id"]?.jsonPrimitive?.intOrNull
        val filter = args["filter"]?.jsonObject
        val limit = args["limit"]?.jsonPrimitive?.intOrNull ?: 25

        when {
            id != null -> {
                val config = loader.load(id)
                    ?: return@addTool errorResult("No $type definition found for id $id")
                CallToolResult(content = listOf(TextContent(objectMapper.writeValueAsString(config))))
            }
            filter != null -> {
                val filterMap = filter.entries.associate { (k, v) -> k to v.jsonPrimitive.content }
                val results = loader.loadAll { config ->
                    filterMap.all { (fieldName, expected) ->
                        matchesField(config, fieldName, expected)
                    }
                }.take(limit)
                CallToolResult(content = listOf(TextContent(objectMapper.writeValueAsString(results))))
            }
            else -> errorResult(
                "Provide either 'id' for a specific lookup or 'filter' to search. " +
                    "Example: {\"type\": \"npc\", \"id\": 1} or {\"type\": \"npc\", \"filter\": {\"name\": \"Guard\"}, \"limit\": 5}"
            )
        }
    }
}

private fun errorResult(message: String): CallToolResult =
    CallToolResult(content = listOf(TextContent(message)), isError = true)

internal fun matchesField(obj: Any, fieldName: String, expected: String): Boolean {
    val value = getFieldValue(obj, fieldName) ?: return false
    return when (value) {
        is String -> value.contains(expected, ignoreCase = true)
        is Array<*> -> value.any { it?.toString().equals(expected, ignoreCase = true) }
        else -> value.toString().equals(expected, ignoreCase = true)
    }
}

internal fun getFieldValue(obj: Any, fieldName: String): Any? {
    var clazz: Class<*>? = obj.javaClass
    while (clazz != null) {
        try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            return field.get(obj)
        } catch (_: NoSuchFieldException) {}
        clazz = clazz.superclass
    }
    return null
}
