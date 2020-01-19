package com.auritylab.graphql.kotlin.toolkit.codegen

import java.nio.file.Path

data class CodegenOptions(
    val schemas: Collection<Path>,
    val outputDirectory: Path,
    var generatedGlobalPrefix: String? = null,
    var generatedBasePackage: String = "graphql.kotlin.toolkit.codegen",
    var generateAll: Boolean = true
)
