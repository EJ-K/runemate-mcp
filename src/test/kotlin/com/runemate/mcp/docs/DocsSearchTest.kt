package com.runemate.mcp.docs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocsSearchTest {

    @Test
    fun `docs resource loads and parses into non-empty page list`() {
        assertTrue(docs.isNotEmpty(), "Expected at least one documentation page")
        docs.forEach { page ->
            assertTrue(page.title.isNotBlank(), "Page title should not be blank: $page")
            assertTrue(page.content.isNotBlank(), "Page content should not be blank: ${page.path}")
        }
    }

    @Test
    fun `section splitting produces sections with headings`() {
        val sectionsWithHeadings = sections.filter { it.heading != it.pageTitle }
        assertTrue(sectionsWithHeadings.isNotEmpty(), "Expected sections split by ## headings")
        sections.forEach { section ->
            assertTrue(section.heading.isNotBlank(), "Section heading should not be blank")
            assertTrue(section.content.isNotBlank(), "Section content should not be blank")
        }
    }

    @Test
    fun `search for query returns results from querying page`() {
        val results = searchDocs("query")
        assertTrue(results.isNotEmpty(), "Expected results for 'query'")
        assertTrue(
            results.any { it.pagePath == "api/querying" },
            "Expected a result from the querying page, got: ${results.map { it.pagePath }}",
        )
    }

    @Test
    fun `search for nonsense term returns empty`() {
        val results = searchDocs("xyzzyplughtwisty")
        assertTrue(results.isEmpty(), "Expected no results for nonsense term")
    }

    @Test
    fun `limit parameter caps results`() {
        val results = searchDocs("the", limit = 3)
        assertTrue(results.size <= 3, "Expected at most 3 results, got ${results.size}")
    }

    @Test
    fun `search is case-insensitive`() {
        val lower = searchDocs("pathfinding")
        val upper = searchDocs("PATHFINDING")
        assertEquals(lower.map { it.heading }, upper.map { it.heading })
    }

    @Test
    fun `heading matches are weighted higher than content matches`() {
        val results = searchDocs("navigation")
        assertTrue(results.isNotEmpty())
        // The section with "Navigation" in the heading should rank first
        assertEquals("Navigation", results.first().heading)
    }
}
