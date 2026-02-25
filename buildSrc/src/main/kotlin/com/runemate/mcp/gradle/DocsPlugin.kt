package com.runemate.mcp.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class DocsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("regenerateDocs", RegenerateDocsTask::class.java) {
            group = "documentation"
            description = "Regenerate bundled RuneMate documentation from the GitBook site"
            outputFile.set(project.file("src/main/resources/docs/runemate-docs.json"))
        }
    }
}
