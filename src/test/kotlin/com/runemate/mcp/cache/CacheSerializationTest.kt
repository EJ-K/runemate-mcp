package com.runemate.mcp.cache

import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CacheSerializationTest {

    @Suppress("unused")
    private class SimpleConfig {
        @JvmField var id: Int = 1
        @JvmField var name: String = "Guard"
        @JvmField var active: Boolean = true
        @JvmField var values: IntArray = intArrayOf(10, 20)
    }

    @Test
    fun `serialize simple POJO with mixed field types`() {
        val json = objectMapper.readValue<Map<String, Any?>>(objectMapper.writeValueAsString(SimpleConfig()))

        assertEquals(1, json["id"])
        assertEquals("Guard", json["name"])
        assertEquals(true, json["active"])
        assertEquals(listOf(10, 20), json["values"])
    }

    @Suppress("unused")
    private class ConfigWithNullableFields {
        @JvmField var id: Int = 1
        @JvmField var name: String? = null
        @JvmField var options: Array<String?> = arrayOf(null, "Attack", null)
    }

    @Test
    fun `serialize config with null values`() {
        val json = objectMapper.readValue<Map<String, Any?>>(objectMapper.writeValueAsString(ConfigWithNullableFields()))

        assertEquals(1, json["id"])
        assertTrue(json.containsKey("name"))
        assertEquals(null, json["name"])
        assertEquals(listOf(null, "Attack", null), json["options"])
    }

    @Suppress("unused")
    private class ConfigWithMap {
        @JvmField var id: Int = 1
        @JvmField var params: Map<Int, Any> = mapOf(26 to 0, 6 to 90)
    }

    @Test
    fun `serialize config with map field`() {
        val json = objectMapper.readValue<Map<String, Any?>>(objectMapper.writeValueAsString(ConfigWithMap()))

        assertEquals(1, json["id"])
        @Suppress("UNCHECKED_CAST")
        val params = json["params"] as Map<String, Any>
        assertEquals(0, params["26"])
        assertEquals(90, params["6"])
    }

    @Suppress("unused")
    private open class ParentConfig {
        @JvmField var parentField: String = "parent"
    }

    @Suppress("unused")
    private class ChildConfig : ParentConfig() {
        @JvmField var childField: String = "child"
    }

    @Test
    fun `serialize includes fields from superclass`() {
        val json = objectMapper.readValue<Map<String, Any?>>(objectMapper.writeValueAsString(ChildConfig()))

        assertEquals("child", json["childField"])
        assertEquals("parent", json["parentField"])
    }

    @Suppress("unused")
    private class ConfigWithStaticField {
        @JvmField var id: Int = 5

        companion object {
            @JvmStatic val CONSTANT = "should_not_appear"
        }
    }

    @Test
    fun `serialize excludes static fields`() {
        val json = objectMapper.readValue<Map<String, Any?>>(objectMapper.writeValueAsString(ConfigWithStaticField()))

        assertTrue(json.containsKey("id"))
        assertFalse(json.containsKey("CONSTANT"))
    }
}
