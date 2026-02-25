package com.runemate.mcp.gradle

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateApiIndexTask : DefaultTask() {

    @get:InputDirectory
    abstract val sourceDir: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        StaticJavaParser.getConfiguration().setLanguageLevel(
            ParserConfiguration.LanguageLevel.valueOf("JAVA_17")
        )

        val srcDir = sourceDir.get().asFile
        val apiDir = File(srcDir, "com/runemate/game/api")
        if (!apiDir.isDirectory) {
            error("API source directory not found: $apiDir")
        }

        val javaFiles = apiDir.walkTopDown().filter { it.extension == "java" }.toList()
        logger.lifecycle("Found ${javaFiles.size} Java files under ${apiDir.relativeTo(srcDir)}")

        val index = mutableListOf<Map<String, Any?>>()

        for (file in javaFiles) {
            try {
                val cu = StaticJavaParser.parse(file)
                index.addAll(extractClasses(cu, file))
            } catch (e: Exception) {
                logger.warn("Failed to parse ${file.name}: ${e.message}")
            }
        }

        val outFile = outputFile.get().asFile
        outFile.parentFile.mkdirs()

        val mapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
        mapper.writeValue(outFile, index)
        logger.lifecycle("Wrote ${index.size} API entries to ${outFile.relativeTo(project.projectDir)}")
    }

    private fun extractClasses(cu: CompilationUnit, file: File): List<Map<String, Any?>> {
        val packageName = cu.packageDeclaration.map { it.nameAsString }.orElse("")
        val results = mutableListOf<Map<String, Any?>>()
        val isIdClass = file.name.endsWith("ID.java")

        for (type in cu.types) {
            if (!type.isPublic) continue
            results.add(extractType(type, packageName, isIdClass))
        }
        return results
    }

    private fun extractType(type: TypeDeclaration<*>, packageName: String, isIdClass: Boolean): Map<String, Any?> {
        val kind = when (type) {
            is EnumDeclaration -> "enum"
            is ClassOrInterfaceDeclaration -> if (type.isInterface) "interface" else "class"
            else -> "class"
        }

        val superclass = if (type is ClassOrInterfaceDeclaration) {
            type.extendedTypes.firstOrNull()?.toString()
        } else null

        val interfaces = if (type is ClassOrInterfaceDeclaration) {
            type.implementedTypes.map { it.toString() }
        } else if (type is EnumDeclaration) {
            type.implementedTypes.map { it.toString() }
        } else emptyList()

        val members = if (isIdClass) {
            // For ID classes, skip individual constant members — too many and served by cache-lookup
            emptyList()
        } else {
            extractMembers(type)
        }

        return mapOf(
            "package" to packageName,
            "name" to type.nameAsString,
            "kind" to kind,
            "description" to extractJavadocDescription(type),
            "superclass" to superclass,
            "interfaces" to interfaces,
            "members" to members,
        )
    }

    private fun extractMembers(type: TypeDeclaration<*>): List<Map<String, Any?>> {
        val members = mutableListOf<Map<String, Any?>>()

        for (method in type.methods) {
            if (!isPublicOrProtected(method.modifiers)) continue
            val params = method.parameters.joinToString(", ") { p ->
                "${p.typeAsString} ${p.nameAsString}"
            }
            val signature = "${method.typeAsString} ${method.nameAsString}($params)"
            members.add(
                mapOf(
                    "name" to method.nameAsString,
                    "kind" to "method",
                    "signature" to signature,
                    "description" to extractJavadocDescription(method),
                )
            )
        }

        for (field in type.fields) {
            if (!isPublicOrProtected(field.modifiers)) continue
            for (variable in field.variables) {
                members.add(
                    mapOf(
                        "name" to variable.nameAsString,
                        "kind" to "field",
                        "signature" to "${field.elementType} ${variable.nameAsString}",
                        "description" to extractJavadocDescription(field),
                    )
                )
            }
        }

        // Include enum constants
        if (type is EnumDeclaration) {
            for (entry in type.entries) {
                members.add(
                    mapOf(
                        "name" to entry.nameAsString,
                        "kind" to "enum_constant",
                        "signature" to entry.nameAsString,
                        "description" to extractJavadocDescription(entry),
                    )
                )
            }
        }

        return members
    }

    private fun isPublicOrProtected(modifiers: List<Modifier>): Boolean {
        return modifiers.any { it.keyword == Modifier.Keyword.PUBLIC || it.keyword == Modifier.Keyword.PROTECTED }
    }

    private fun extractJavadocDescription(node: NodeWithJavadoc<*>): String? {
        return node.javadoc.map { javadoc ->
            javadoc.description.toText().trim().ifEmpty { null }
        }.orElse(null)
    }
}
