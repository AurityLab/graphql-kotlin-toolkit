package com.auritylab.graphql.kotlin.codegen

import java.nio.file.Path

internal data class CodegenInternalOptions(
        val schemas: Collection<Path>,
        val outputDirectory: Path,
        val generatedGlobalPrefix: String?,
        val generatedBasePackage: String
)
