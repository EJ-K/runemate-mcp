package com.runemate.mcp.gradle

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.net.URI

abstract class RegenerateDocsTask : DefaultTask() {

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun regenerate() {
        val sitemapUrl = "https://runemate.gitbook.io/runemate-documentation/sitemap-pages.xml"
        val sitemap = URI(sitemapUrl).toURL().readText()

        val urlPattern = Regex("<loc>([^<]+)</loc>")
        val urls = urlPattern.findAll(sitemap).map { it.groupValues[1] }.toList()
        logger.lifecycle("Found ${urls.size} pages in sitemap")

        val baseUrl = "https://runemate.gitbook.io/runemate-documentation"
        val pages = mutableListOf<Map<String, String>>()

        for (url in urls) {
            val path = url.removePrefix(baseUrl).removePrefix("/")
            val label = path.ifEmpty { "(root)" }

            try {
                val doc = Jsoup.connect(url).get()
                val title = doc.selectFirst("h1")?.text()?.trim()
                    ?: path.substringAfterLast("/").ifEmpty { "Welcome" }
                val content = extractMarkdown(doc)

                pages.add(mapOf("path" to path, "title" to title, "content" to content))
                logger.lifecycle("  $label — ${content.length} chars")
            } catch (e: Exception) {
                logger.warn("  $label — FAILED: ${e.message}")
            }
        }

        val file = outputFile.get().asFile
        file.parentFile.mkdirs()

        val mapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
        mapper.writeValue(file, pages)
        logger.lifecycle("Wrote ${pages.size} pages to ${file.relativeTo(project.projectDir)}")
    }

    private fun extractMarkdown(doc: Document): String {
        // Target the main content area; fall back to body
        val root = doc.selectFirst("main") ?: doc.body()

        val sb = StringBuilder()
        convertNode(root, sb)

        return sb.toString()
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    private fun convertNode(element: Element, sb: StringBuilder) {
        for (node in element.childNodes()) {
            when (node) {
                is TextNode -> {
                    val text = node.wholeText
                    if (text.isNotBlank()) sb.append(text)
                }
                is Element -> convertElement(node, sb)
            }
        }
    }

    private fun convertElement(el: Element, sb: StringBuilder) {
        when (el.tagName()) {
            "h1" -> { sb.append("\n# "); sb.append(el.text()); sb.append("\n\n") }
            "h2" -> { sb.append("\n## "); sb.append(el.text()); sb.append("\n\n") }
            "h3" -> { sb.append("\n### "); sb.append(el.text()); sb.append("\n\n") }
            "h4" -> { sb.append("\n#### "); sb.append(el.text()); sb.append("\n\n") }
            "p" -> { convertNode(el, sb); sb.append("\n\n") }
            "br" -> sb.append("\n")
            "strong", "b" -> { sb.append("**"); convertNode(el, sb); sb.append("**") }
            "em", "i" -> { sb.append("*"); convertNode(el, sb); sb.append("*") }
            "code" -> {
                if (el.parent()?.tagName() == "pre") {
                    // Handled by the pre block
                    sb.append(el.wholeText())
                } else {
                    sb.append("`"); sb.append(el.text()); sb.append("`")
                }
            }
            "pre" -> {
                val code = el.selectFirst("code")
                val lang = code?.className()
                    ?.split(" ")
                    ?.firstOrNull { it.startsWith("language-") }
                    ?.removePrefix("language-")
                    ?: ""
                sb.append("\n```$lang\n")
                sb.append(code?.wholeText() ?: el.wholeText())
                sb.append("\n```\n\n")
            }
            "ul" -> { sb.append("\n"); convertListItems(el, sb, ordered = false); sb.append("\n") }
            "ol" -> { sb.append("\n"); convertListItems(el, sb, ordered = true); sb.append("\n") }
            "li" -> { convertNode(el, sb) }
            "a" -> {
                val href = el.attr("href")
                val text = el.text()
                if (href.isNotBlank() && text.isNotBlank()) {
                    sb.append("[$text]($href)")
                } else {
                    convertNode(el, sb)
                }
            }
            "table" -> convertTable(el, sb)
            "blockquote" -> {
                val text = el.text()
                text.lines().forEach { sb.append("> $it\n") }
                sb.append("\n")
            }
            "hr" -> sb.append("\n---\n\n")
            "img" -> {} // skip images
            "script", "style", "nav", "header", "footer" -> {} // skip non-content
            else -> convertNode(el, sb)
        }
    }

    private fun convertListItems(list: Element, sb: StringBuilder, ordered: Boolean) {
        var index = 1
        for (child in list.children()) {
            if (child.tagName() == "li") {
                val prefix = if (ordered) "${index++}. " else "- "
                sb.append(prefix)
                convertNode(child, sb)
                sb.append("\n")
            }
        }
    }

    private fun convertTable(table: Element, sb: StringBuilder) {
        val rows = table.select("tr")
        if (rows.isEmpty()) return

        for ((i, row) in rows.withIndex()) {
            val cells = row.select("th, td")
            sb.append("| ")
            sb.append(cells.joinToString(" | ") { it.text() })
            sb.append(" |\n")

            // Add separator after header row
            if (i == 0) {
                sb.append("| ")
                sb.append(cells.joinToString(" | ") { "---" })
                sb.append(" |\n")
            }
        }
        sb.append("\n")
    }
}
