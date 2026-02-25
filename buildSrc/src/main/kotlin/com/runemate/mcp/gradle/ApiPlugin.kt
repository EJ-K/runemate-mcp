package com.runemate.mcp.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class ApiPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("generateApiIndex", GenerateApiIndexTask::class.java) {
            group = "documentation"
            description = "Generate API index from runemate-game-api source files"
            sourceDir.set(project.file("../runemate-game-api/src/main/java"))
            outputFile.set(project.file("src/main/resources/api/api-index.json"))
        }
    }
}
