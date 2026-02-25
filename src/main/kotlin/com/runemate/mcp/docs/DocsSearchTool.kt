package com.runemate.mcp.docs

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.*

internal data class DocPage(val path: String, val title: String, val content: String)

internal data class DocSection(
    val pagePath: String,
    val pageTitle: String,
    val heading: String,
    val content: String,
)

internal val docs: List<DocPage> by lazy {
    val stream = DocPage::class.java.getResourceAsStream("/docs/runemate-docs.json")
        ?: error("Could not find bundled documentation resource")
    jacksonObjectMapper().readValue(stream)
}

internal val sections: List<DocSection> by lazy {
    docs.flatMap { page -> splitSections(page) }
}

internal fun splitSections(page: DocPage): List<DocSection> {
    val lines = page.content.lines()
    val sections = mutableListOf<DocSection>()
    var currentHeading = page.title
    val buffer = StringBuilder()

    for (line in lines) {
        if (line.startsWith("## ")) {
            if (buffer.isNotBlank()) {
                sections += DocSection(page.path, page.title, currentHeading, buffer.toString().trim())
            }
            currentHeading = line.removePrefix("## ").trim()
            buffer.clear()
        } else {
            buffer.appendLine(line)
        }
    }
    if (buffer.isNotBlank()) {
        sections += DocSection(page.path, page.title, currentHeading, buffer.toString().trim())
    }
    return sections
}

internal fun searchDocs(query: String, limit: Int = 5): List<DocSection> {
    val words = query.lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (words.isEmpty()) return emptyList()

    return sections
        .map { section -> section to scoreSection(section, words) }
        .filter { (_, score) -> score > 0 }
        .sortedByDescending { (_, score) -> score }
        .take(limit)
        .map { (section, _) -> section }
}

private fun scoreSection(section: DocSection, words: List<String>): Int {
    val headingLower = section.heading.lowercase()
    val contentLower = section.content.lowercase()
    return words.sumOf { word ->
        countOccurrences(headingLower, word) * 3 + countOccurrences(contentLower, word)
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

fun Server.registerDocsSearchTool() {
    addTool(
        name = "docs-search",
        description = "Search the RuneMate developer documentation by keyword. Returns the most relevant documentation sections.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("query", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Keyword(s) to search for in the documentation"))
                })
                put("limit", buildJsonObject {
                    put("type", JsonPrimitive("integer"))
                    put("description", JsonPrimitive("Maximum number of results to return (default 5)"))
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
        val limit = args["limit"]?.jsonPrimitive?.intOrNull ?: 5

        val results = searchDocs(query, limit)
        if (results.isEmpty()) {
            CallToolResult(content = listOf(TextContent("No documentation found matching: $query")))
        } else {
            val output = results.map { section ->
                mapOf(
                    "path" to section.pagePath,
                    "title" to section.pageTitle,
                    "section" to section.heading,
                    "content" to section.content,
                )
            }
            CallToolResult(content = listOf(TextContent(resultMapper.writeValueAsString(output))))
        }
    }
}
