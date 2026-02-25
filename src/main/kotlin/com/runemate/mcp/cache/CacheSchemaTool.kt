package com.runemate.mcp.cache

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.*
import java.lang.reflect.ParameterizedType

private val SUPPORTED_TYPES = CacheManager.loaders.keys.sorted()

fun Server.registerCacheSchemaTool() {
    addTool(
        name = "cache-schema",
        description = "List the available field names and types for a cache definition type, " +
            "so you know which fields can be used with cache-lookup filters. " +
            "Call with no arguments to list all supported types.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("type", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("The definition type to inspect. Omit to list all types."))
                    putJsonArray("enum") { SUPPORTED_TYPES.forEach { add(it) } }
                })
            },
            required = emptyList(),
        ),
    ) { request ->
        val args = request.arguments ?: buildJsonObject {}
        val type = args["type"]?.jsonPrimitive?.contentOrNull

        if (type == null) {
            val summary = SUPPORTED_TYPES.joinToString("\n") { "- $it" }
            CallToolResult(content = listOf(TextContent("Supported cache types:\n$summary\n\nPass a type to see its fields.")))
        } else {
            val loader = CacheManager.loaders[type]
                ?: return@addTool CallToolResult(
                    content = listOf(TextContent("Unknown type '$type'. Supported types: $SUPPORTED_TYPES")),
                    isError = true,
                )

            val configClass = resolveConfigClass(loader)
                ?: return@addTool CallToolResult(
                    content = listOf(TextContent("Could not resolve config class for type '$type'.")),
                    isError = true,
                )

            val fields = collectFields(configClass)
            val result = buildJsonObject {
                put("type", JsonPrimitive(type))
                put("configClass", JsonPrimitive(configClass.simpleName))
                putJsonArray("fields") {
                    for ((name, fieldType) in fields) {
                        addJsonObject {
                            put("name", JsonPrimitive(name))
                            put("type", JsonPrimitive(fieldType))
                        }
                    }
                }
            }
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
}

/**
 * Resolve the config class `T` from a `ConfigLoader<T>` by walking the generic superclass chain.
 */
internal fun resolveConfigClass(loader: Any): Class<*>? {
    var clazz: Class<*>? = loader.javaClass
    while (clazz != null) {
        val superType = clazz.genericSuperclass
        if (superType is ParameterizedType && superType.rawType.let { it is Class<*> && it.simpleName == "ConfigLoader" }) {
            val typeArg = superType.actualTypeArguments.firstOrNull()
            return if (typeArg is Class<*>) typeArg else null
        }
        clazz = clazz.superclass
    }
    return null
}

/**
 * Walk the class hierarchy collecting all declared fields, returning (name, humanReadableType) pairs.
 * Excludes synthetic/compiler-generated fields.
 */
internal fun collectFields(clazz: Class<*>): List<Pair<String, String>> {
    val fields = mutableListOf<Pair<String, String>>()
    var current: Class<*>? = clazz
    while (current != null && current != Any::class.java) {
        for (field in current.declaredFields) {
            if (field.isSynthetic) continue
            fields.add(field.name to friendlyTypeName(field.type, field.genericType))
        }
        current = current.superclass
    }
    return fields
}

private fun friendlyTypeName(raw: Class<*>, generic: java.lang.reflect.Type): String {
    if (raw.isArray) return friendlyTypeName(raw.componentType, raw.componentType) + "[]"
    if (generic is ParameterizedType) {
        val base = (generic.rawType as? Class<*>)?.simpleName ?: raw.simpleName
        val args = generic.actualTypeArguments.joinToString(", ") {
            if (it is Class<*>) it.simpleName else it.typeName
        }
        return "$base<$args>"
    }
    return raw.simpleName
}
