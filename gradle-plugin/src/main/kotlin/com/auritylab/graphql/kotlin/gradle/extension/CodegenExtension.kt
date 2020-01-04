package com.auritylab.graphql.kotlin.gradle.extension

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

open class CodegenExtension(objects: ObjectFactory) {
    val schemas = objects.fileCollection()

    var outputDirectory = objects.directoryProperty()
}
