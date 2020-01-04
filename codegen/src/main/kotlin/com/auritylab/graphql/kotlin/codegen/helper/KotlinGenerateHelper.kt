package com.auritylab.graphql.kotlin.codegen.helper

import graphql.language.DirectivesContainer

internal object KotlinGenerateHelper {
    private const val DIRECTIVE_NAME = "kotlinGenerate"

    fun shouldGenerate(directivesContainer: DirectivesContainer<*>): Boolean {
        return directivesContainer.getDirective(DIRECTIVE_NAME) != null
    }
}
