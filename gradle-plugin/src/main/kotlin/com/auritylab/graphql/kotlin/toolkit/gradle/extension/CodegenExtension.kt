package com.auritylab.graphql.kotlin.toolkit.gradle.extension

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

open class CodegenExtension(objects: ObjectFactory) {
    val schemas = objects.fileCollection()

    var outputDirectory = objects.directoryProperty()

    val generatedGlobalPrefix = objects.property<String?>()

    val generatedBasePackage = objects.property<String>()

    val generateAll = objects.property<Boolean>()

    val enableSpringBootIntegration = objects.property<Boolean>()
}
