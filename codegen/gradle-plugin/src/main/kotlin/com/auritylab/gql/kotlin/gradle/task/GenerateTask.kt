package com.auritylab.gql.kotlin.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

open class GenerateTask: DefaultTask() {
    @Input
    val inputSchemas = project.objects.listProperty<String>()

    @Input
    val outputDir = project.objects.property<String>()
}
