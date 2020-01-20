package com.auritylab.graphql.kotlin.toolkit.codegen.helper

import com.squareup.kotlinpoet.ClassName
import graphql.schema.GraphQLDirectiveContainer

object DirectiveHelper {
    private const val GENERATE_DIRECTIVE = "kGenerate"
    private const val RESOLVER_DIRECTIVE = "kResolver"
    private const val REPRESENTATION_DIRECTIVE = "kRepresentation"

    fun hasGenerateDirective(container: GraphQLDirectiveContainer): Boolean =
        hasDirective(container, GENERATE_DIRECTIVE)

    fun hasResolverDirective(container: GraphQLDirectiveContainer): Boolean =
        hasDirective(container, RESOLVER_DIRECTIVE)

    fun hasRepresentationDirective(container: GraphQLDirectiveContainer): Boolean =
        hasDirective(container, REPRESENTATION_DIRECTIVE)

    fun getRepresentationClass(container: GraphQLDirectiveContainer): ClassName? {
        if (hasRepresentationDirective(container))
            return null

        val className = container.getDirective(REPRESENTATION_DIRECTIVE)
            ?.getArgument("class")
            ?.value as String? ?: return null

        return ClassName.bestGuess(className)
    }

    private fun hasDirective(container: GraphQLDirectiveContainer, name: String): Boolean =
        container.getDirective(name) != null
}
