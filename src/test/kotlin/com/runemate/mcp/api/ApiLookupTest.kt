package com.runemate.mcp.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiLookupTest {

    @Test
    fun `api index loads and parses into non-empty list`() {
        assertTrue(apiIndex.isNotEmpty(), "Expected at least one API class in the index")
        apiIndex.forEach { cls ->
            assertTrue(cls.name.isNotBlank(), "Class name should not be blank")
            assertTrue(cls.`package`.isNotBlank(), "Package should not be blank")
            assertTrue(cls.kind in listOf("class", "interface", "enum"), "Unexpected kind: ${cls.kind}")
        }
    }

    @Test
    fun `search for Npcs returns the Npcs class`() {
        val results = searchApi("Npcs")
        assertTrue(results.isNotEmpty(), "Expected results for 'Npcs'")
        assertTrue(
            results.any { it.name == "Npcs" },
            "Expected Npcs class in results, got: ${results.map { it.name }}",
        )
    }

    @Test
    fun `search for newQuery returns query builder classes`() {
        val results = searchApi("newQuery")
        assertTrue(results.isNotEmpty(), "Expected results for 'newQuery'")
        assertTrue(
            results.any { cls -> cls.members.any { it.name == "newQuery" } },
            "Expected at least one class with a newQuery member",
        )
    }

    @Test
    fun `search with type filter returns only matching kinds`() {
        val results = searchApi("prayer", type = "enum")
        results.forEach { cls ->
            assertEquals("enum", cls.kind, "Expected only enum results when filtering by enum")
        }
    }

    @Test
    fun `limit parameter caps results`() {
        val results = searchApi("get", limit = 3)
        assertTrue(results.size <= 3, "Expected at most 3 results, got ${results.size}")
    }

    @Test
    fun `search for nonsense term returns empty`() {
        val results = searchApi("xyzzyplughtwisty")
        assertTrue(results.isEmpty(), "Expected no results for nonsense term")
    }

    @Test
    fun `search is case-insensitive`() {
        val lower = searchApi("npcs").map { it.name }
        val upper = searchApi("NPCS").map { it.name }
        assertEquals(lower, upper, "Search should be case-insensitive")
    }

    @Test
    fun `ID classes have no members in the index`() {
        val idClasses = apiIndex.filter { it.name.endsWith("ID") }
        if (idClasses.isNotEmpty()) {
            idClasses.forEach { cls ->
                assertTrue(cls.members.isEmpty(), "${cls.name} should have no members (ID class)")
            }
        }
    }
}
