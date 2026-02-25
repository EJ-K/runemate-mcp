package com.runemate.mcp.cache

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.io.File

/**
 * Integration tests that read from the real OSRS cache on disk.
 * Skipped automatically if the cache directory or required runtime classes are not available.
 */
class CacheIntegrationTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initCache() {
            val cacheDir = System.getenv("OSRS_CACHE_DIR")?.let { File(it) }
                ?: File(System.getProperty("user.home"), ".runelite/jagexcache/oldschool/LIVE")

            assumeTrue(
                cacheDir.isDirectory && File(cacheDir, "main_file_cache.dat2").exists(),
                "OSRS cache not available at ${cacheDir.absolutePath} — skipping integration tests"
            )

            try {
                // Triggers CacheManager.init which needs the cache on disk
                CacheManager.loaders
            } catch (e: NoClassDefFoundError) {
                assumeTrue(false, "Required runtime classes not available: ${e.message}")
            } catch (e: ExceptionInInitializerError) {
                assumeTrue(false, "Class initialization failed: ${e.cause?.message}")
            }
        }
    }

    private fun serialize(obj: Any): Map<String, Any?> =
        objectMapper.readValue(objectMapper.writeValueAsString(obj))

    @Test
    fun `load NPC by id - King Black Dragon`() {
        val loader = CacheManager.loaders["npc"]!!
        val kbd = loader.load(239)
        assertNotNull(kbd, "NPC id 239 should exist in cache")

        val json = serialize(kbd)

        assertEquals(239, json["id"])
        assertEquals("King Black Dragon", json["name"])
        assertEquals(5, json["size"])
        assertEquals(276, json["combatLevel"])
        assertEquals(90, json["standingAnimation"])
        assertEquals(4635, json["walkingAnimation"])
        assertEquals(347, json["category"])

        // models array
        assertEquals(listOf(17414, 17415, 17429, 17422, 17423), json["models"])

        // actions array — index 1 should be "Attack"
        @Suppress("UNCHECKED_CAST")
        val actions = json["actions"] as List<String?>
        assertEquals(null, actions[0])
        assertEquals("Attack", actions[1])

        // stats array: [attack, defence, strength, hitpoints, ranged, magic]
        @Suppress("UNCHECKED_CAST")
        val stats = json["stats"] as List<Int>
        assertEquals(240, stats[0]) // attack
        assertEquals(240, stats[1]) // defence
        assertEquals(240, stats[2]) // strength
        assertEquals(240, stats[3]) // hitpoints
        assertEquals(240, stats[5]) // magic

        // params map
        @Suppress("UNCHECKED_CAST")
        val params = json["params"] as Map<String, Any>
        assertTrue(params.isNotEmpty(), "KBD should have params")
    }

    @Test
    fun `filter NPCs by name - King Black Dragon`() {
        val loader = CacheManager.loaders["npc"]!!
        val results = loader.loadAll { config ->
            matchesField(config, "name", "King Black Dragon")
        }

        assertTrue(results.isNotEmpty(), "Should find at least one NPC named King Black Dragon")

        val json = serialize(results.first())
        assertEquals("King Black Dragon", json["name"])
        assertEquals(239, json["id"])
    }

    @Test
    fun `load item by id - Abyssal whip`() {
        val loader = CacheManager.loaders["item"]!!
        val whip = loader.load(4151)
        assertNotNull(whip, "Item id 4151 should exist in cache")

        val json = serialize(whip)
        assertEquals(4151, json["id"])
        assertEquals("Abyssal whip", json["name"])
    }

    @Test
    fun `load object by id - Oak tree`() {
        val loader = CacheManager.loaders["object"]!!
        val oak = loader.load(10820)
        assertNotNull(oak, "Object id 10820 should exist in cache")

        val json = serialize(oak)
        assertEquals(10820, json["id"])
        assertEquals("Oak tree", json["name"])
    }

    @Test
    fun `filter with matchesField on actions array`() {
        val loader = CacheManager.loaders["npc"]!!
        val results = loader.loadAll { config ->
            matchesField(config, "actions", "Pickpocket")
        }.take(5)

        assertTrue(results.isNotEmpty(), "Should find NPCs with Pickpocket action")
        results.forEach { npc ->
            val json = serialize(npc)
            @Suppress("UNCHECKED_CAST")
            val actions = json["actions"] as List<String?>
            assertTrue(actions.any { it.equals("Pickpocket", ignoreCase = true) })
        }
    }
}
