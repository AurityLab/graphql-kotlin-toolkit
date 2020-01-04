package com.auritylab.graphql.kotlin.codegen

import java.nio.file.Path

data class CodegenOptions(
        val outputDirectory: Path? = null,
        val generatedFilesPrefix: String? = null,
        val generatedFilesPackage: String = "graphql.kotlin.toolkit.generated",
        val generatedEnumPrefix: String? = "E",
        val generatedInputObjectPrefix: String? = "I",
        val generatedResolverPrefix: String? = "R"
)
