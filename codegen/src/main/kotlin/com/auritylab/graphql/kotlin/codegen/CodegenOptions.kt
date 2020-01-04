package com.auritylab.graphql.kotlin.codegen

import java.nio.file.Path

data class CodegenOptions(
        val outputDirectory: Path? = null,
        val generatedGlobalPrefix: String? = null,
        val generatedBasePackage: String = "graphql.kotlin.toolkit.codegen"
)
