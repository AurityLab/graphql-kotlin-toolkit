package com.auritylab.graphql.kotlin.codegen

data class CodegenOptions(
        val generatedFilesPrefix: String? = null,
        val generatedFilesPackage: String = "graphql.kotlin.toolkit.generated",
        val generatedEnumPrefix: String? = "E",
        val generatedInputObjectPrefix: String? = "I",
        val generatedResolverPrefix: String? = "R"
)
