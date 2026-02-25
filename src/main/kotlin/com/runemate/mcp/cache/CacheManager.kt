package com.runemate.mcp.cache

import com.runemate.game.api.osrs.cache.definition.loader.*
import com.runemate.game.api.osrs.cache.fs.JagexCache
import org.apache.logging.log4j.LogManager
import java.io.File

object CacheManager {

    private val log = LogManager.getLogger(CacheManager::class.java)

    val loaders: Map<String, ConfigLoader<*>>

    init {
        val cacheDir = resolveCacheDirectory()
        log.info("Using OSRS cache directory: {}", cacheDir.absolutePath)

        val cache = JagexCache(cacheDir)
        log.info("JagexCache initialized successfully")

        loaders = mapOf(
            "item" to ItemLoader(cache),
            "npc" to NpcLoader(cache),
            "object" to ObjectLoader(cache),
            "enum" to EnumLoader(cache),
            "struct" to StructLoader(cache),
            "varbit" to VarbitLoader(cache),
            "dbrow" to DBRowLoader(cache),
            "dbtable" to DBTableLoader(cache),
            "spotanim" to SpotAnimationLoader(cache),
            "inventory" to InventoryLoader(cache),
            "combatgauge" to CombatGaugeLoader(cache),
            "worldentity" to WorldEntityLoader(cache),
        )
    }

    private fun resolveCacheDirectory(): File {
        val envDir = System.getenv("OSRS_CACHE_DIR")
        val cacheDir = if (envDir != null) {
            File(envDir)
        } else {
            File(System.getProperty("user.home"), ".runelite/jagexcache/oldschool/LIVE")
        }

        require(cacheDir.isDirectory) {
            "OSRS cache directory does not exist: ${cacheDir.absolutePath}. " +
                "Set the OSRS_CACHE_DIR environment variable to the correct path."
        }
        require(File(cacheDir, "main_file_cache.dat2").exists()) {
            "main_file_cache.dat2 not found in ${cacheDir.absolutePath}. " +
                "This does not appear to be a valid OSRS cache directory."
        }

        return cacheDir
    }
}
