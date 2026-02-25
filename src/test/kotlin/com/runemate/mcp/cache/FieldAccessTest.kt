package com.runemate.mcp.cache

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FieldAccessTest {

    // region getFieldValue

    @Suppress("unused")
    private class PublicFieldObj {
        @JvmField var name: String = "test"
    }

    @Test
    fun `getFieldValue - public field`() {
        assertEquals("test", getFieldValue(PublicFieldObj(), "name"))
    }

    @Suppress("unused")
    private class PrivateFieldObj {
        private var level: Int = 99
    }

    @Test
    fun `getFieldValue - private field via direct access`() {
        assertEquals(99, getFieldValue(PrivateFieldObj(), "level"))
    }

    @Suppress("unused")
    private open class Parent {
        @JvmField var inherited: String = "from-parent"
    }

    @Suppress("unused")
    private class Child : Parent() {
        @JvmField var own: String = "from-child"
    }

    @Test
    fun `getFieldValue - inherited field from superclass`() {
        val obj = Child()
        assertEquals("from-parent", getFieldValue(obj, "inherited"))
        assertEquals("from-child", getFieldValue(obj, "own"))
    }

    @Test
    fun `getFieldValue - missing field returns null`() {
        assertNull(getFieldValue(PublicFieldObj(), "nonExistent"))
    }

    // endregion

    // region matchesField

    @Suppress("unused")
    private class MatchTestObj {
        @JvmField var name: String = "Guard captain"
        @JvmField var options: Array<String> = arrayOf("Talk-to", "Attack", "Examine")
        @JvmField var level: Int = 42
    }

    @Test
    fun `matchesField - string contains partial match case-insensitive`() {
        val obj = MatchTestObj()
        assertTrue(matchesField(obj, "name", "guard"))
        assertTrue(matchesField(obj, "name", "Captain"))
        assertFalse(matchesField(obj, "name", "Goblin"))
    }

    @Test
    fun `matchesField - string array matches element`() {
        val obj = MatchTestObj()
        assertTrue(matchesField(obj, "options", "Attack"))
        assertTrue(matchesField(obj, "options", "talk-to"))
        assertFalse(matchesField(obj, "options", "Pickpocket"))
    }

    @Test
    fun `matchesField - numeric exact match via toString`() {
        val obj = MatchTestObj()
        assertTrue(matchesField(obj, "level", "42"))
        assertFalse(matchesField(obj, "level", "43"))
    }

    @Test
    fun `matchesField - missing field returns false`() {
        assertFalse(matchesField(MatchTestObj(), "nonExistent", "anything"))
    }

    // endregion
}
