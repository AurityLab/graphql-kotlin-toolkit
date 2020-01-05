package com.auritylab.graphql.kotlin.toolkit.codegen

import java.nio.file.Path

data class CodegenOptions(
        val schemas: Collection<Path>? = null,
        val outputDirectory: Path? = null,
        val generatedGlobalPrefix: String? = null,
        val generatedBasePackage: String? = null,
        val generateAll: Boolean? = null
)
