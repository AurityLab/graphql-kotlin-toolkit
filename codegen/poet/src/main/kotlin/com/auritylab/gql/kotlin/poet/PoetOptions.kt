package com.auritylab.gql.kotlin.poet

data class PoetOptions(
        val generatedFilesPrefix: String? = null,
        val generatedFilesPackage: String = "graphql.kotlin.toolkit.generated",
        val generatedEnumPrefix: String? = "E",
        val generatedInputObjectPrefix: String? = "I",
        val generatedResolverPrefix: String? = "R"
)
