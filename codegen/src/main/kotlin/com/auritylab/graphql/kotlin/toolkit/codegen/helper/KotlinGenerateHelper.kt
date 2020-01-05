package com.auritylab.graphql.kotlin.toolkit.codegen.helper

import graphql.schema.GraphQLDirectiveContainer

internal object KotlinGenerateHelper {
    private const val DIRECTIVE_NAME = "kotlinGenerate"

    fun shouldGenerate(directivesContainer: GraphQLDirectiveContainer): Boolean {
        return directivesContainer.getDirective(DIRECTIVE_NAME) != null
    }
}
