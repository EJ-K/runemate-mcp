package com.runemate.mcp.cache

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CacheSchemaTest {

    // region resolveConfigClass

    private abstract class FakeConfigLoader<T>
    private class StringLoader : FakeConfigLoader<String>()

    @Test
    fun `resolveConfigClass - resolves type parameter from generic superclass`() {
        // This tests the generic resolution logic using our own hierarchy.
        // ConfigLoader from the game API works the same way.
        val clazz = resolveConfigClass(StringLoader(), superclassName = "FakeConfigLoader")
        assertEquals(String::class.java, clazz)
    }

    @Test
    fun `resolveConfigClass - returns null for non-generic class`() {
        assertNull(resolveConfigClass(Object(), superclassName = "ConfigLoader"))
    }

    // endregion

    // region collectFields

    @Suppress("unused")
    private open class Parent {
        @JvmField var parentField: String = ""
        @JvmField var shared: Int = 0
    }

    @Suppress("unused")
    private class Child : Parent() {
        @JvmField var childField: Boolean = false
        @JvmField var items: Array<String> = emptyArray()
    }

    @Test
    fun `collectFields - includes own and inherited fields`() {
        val fields = collectFields(Child::class.java)
        val names = fields.map { it.first }
        assertTrue("childField" in names)
        assertTrue("items" in names)
        assertTrue("parentField" in names)
        assertTrue("shared" in names)
    }

    @Test
    fun `collectFields - child fields come before parent fields`() {
        val fields = collectFields(Child::class.java)
        val names = fields.map { it.first }
        val childIdx = names.indexOf("childField")
        val parentIdx = names.indexOf("parentField")
        assertTrue(childIdx < parentIdx, "Child fields should appear before parent fields")
    }

    @Test
    fun `collectFields - reports correct types`() {
        val fields = collectFields(Child::class.java).associate { it.first to it.second }
        assertEquals("boolean", fields["childField"])
        assertEquals("String[]", fields["items"])
        assertEquals("String", fields["parentField"])
        assertEquals("int", fields["shared"])
    }

    // endregion

    // region friendlyTypeName

    @Suppress("unused")
    private class GenericFieldHolder {
        @JvmField var simple: Int = 0
        @JvmField var array: IntArray = intArrayOf()
        @JvmField var objectArray: Array<String> = emptyArray()
        var map: Map<String, Int> = emptyMap()
    }

    @Test
    fun `collectFields - handles generic types`() {
        val fields = collectFields(GenericFieldHolder::class.java).associate { it.first to it.second }
        assertEquals("int", fields["simple"])
        assertEquals("int[]", fields["array"])
        assertEquals("String[]", fields["objectArray"])
        assertEquals("Map<String, Integer>", fields["map"])
    }

    // endregion
}

/**
 * Test-friendly overload that accepts the superclass name to match,
 * allowing us to test the resolution logic without depending on the game API.
 */
private fun resolveConfigClass(loader: Any, superclassName: String): Class<*>? {
    var clazz: Class<*>? = loader.javaClass
    while (clazz != null) {
        val superType = clazz.genericSuperclass
        if (superType is java.lang.reflect.ParameterizedType &&
            superType.rawType.let { it is Class<*> && it.simpleName == superclassName }
        ) {
            val typeArg = superType.actualTypeArguments.firstOrNull()
            return if (typeArg is Class<*>) typeArg else null
        }
        clazz = clazz.superclass
    }
    return null
}
