package com.auritylab.gql.kotlin.gradle.extension

import org.gradle.api.Project
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

open class GenerateExtension(project: Project) {
    val inputSchemas = project.objects.listProperty<String>()

    val outputDir = project.objects.property<String>()
}
