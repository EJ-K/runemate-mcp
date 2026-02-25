package com.runemate.mcp.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.*

internal data class ApiClass(
    val `package`: String,
    val name: String,
    val kind: String,
    val description: String?,
    val superclass: String?,
    val interfaces: List<String>,
    val members: List<ApiMember>,
)

internal data class ApiMember(
    val name: String,
    val kind: String,
    val signature: String,
    val description: String?,
)

internal val apiIndex: List<ApiClass> by lazy {
    val stream = ApiClass::class.java.getResourceAsStream("/api/api-index.json")
        ?: error("Could not find bundled API index resource")
    jacksonObjectMapper().readValue(stream)
}

internal fun searchApi(query: String, type: String? = null, limit: Int = 10): List<ApiClass> {
    val words = query.lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (words.isEmpty()) return emptyList()

    return apiIndex
        .filter { type == null || it.kind == type }
        .map { cls -> cls to scoreClass(cls, words) }
        .filter { (_, score) -> score > 0 }
        .sortedByDescending { (_, score) -> score }
        .take(limit)
        .map { (cls, _) -> cls }
}

private fun scoreClass(cls: ApiClass, words: List<String>): Int {
    val classNameLower = cls.name.lowercase()
    val descriptionLower = (cls.description ?: "").lowercase()
    val qualifiedLower = "${cls.`package`}.${cls.name}".lowercase()

    return words.sumOf { word ->
        var score = 0
        // Class name matches weighted heavily
        score += countOccurrences(classNameLower, word) * 5
        // Qualified name match (for package-scoped searches)
        score += countOccurrences(qualifiedLower, word)
        // Class description matches
        score += countOccurrences(descriptionLower, word)
        // Member name matches
        score += cls.members.sumOf { m -> countOccurrences(m.name.lowercase(), word) * 2 }
        // Member description matches
        score += cls.members.sumOf { m -> countOccurrences((m.description ?: "").lowercase(), word) }
        score
    }
}

private fun countOccurrences(text: String, word: String): Int {
    var count = 0
    var index = text.indexOf(word)
    while (index >= 0) {
        count++
        index = text.indexOf(word, index + word.length)
    }
    return count
}

private val resultMapper = jacksonObjectMapper()

fun Server.registerApiLookupTool() {
    addTool(
        name = "api-lookup",
        description = "Search the RuneMate game API for classes, methods, and fields by keyword. " +
            "Returns matching class definitions with their method signatures and Javadoc descriptions. " +
            "Use this to find exact class names, method signatures, and parameter types.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("query", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Class name, method name, or keyword to search for (e.g. \"Npcs\", \"newQuery\", \"prayer\")"))
                })
                put("type", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Filter by type: class, interface, or enum"))
                    put("enum", buildJsonArray {
                        add(JsonPrimitive("class"))
                        add(JsonPrimitive("interface"))
                        add(JsonPrimitive("enum"))
                    })
                })
                put("limit", buildJsonObject {
                    put("type", JsonPrimitive("integer"))
                    put("description", JsonPrimitive("Maximum number of results to return (default 10)"))
                })
            },
            required = listOf("query"),
        ),
    ) { request ->
        val args = request.arguments ?: buildJsonObject {}
        val query = args["query"]?.jsonPrimitive?.content
            ?: return@addTool CallToolResult(
                content = listOf(TextContent("Missing required parameter: query")),
                isError = true,
            )
        val type = args["type"]?.jsonPrimitive?.contentOrNull
        val limit = args["limit"]?.jsonPrimitive?.intOrNull ?: 10

        val results = searchApi(query, type, limit)
        if (results.isEmpty()) {
            CallToolResult(content = listOf(TextContent("No API classes found matching: $query")))
        } else {
            val output = results.map { cls ->
                mapOf(
                    "package" to cls.`package`,
                    "name" to cls.name,
                    "kind" to cls.kind,
                    "description" to cls.description,
                    "superclass" to cls.superclass,
                    "interfaces" to cls.interfaces,
                    "members" to cls.members.map { m ->
                        mapOf(
                            "name" to m.name,
                            "kind" to m.kind,
                            "signature" to m.signature,
                            "description" to m.description,
                        )
                    },
                )
            }
            CallToolResult(content = listOf(TextContent(resultMapper.writeValueAsString(output))))
        }
    }
}
