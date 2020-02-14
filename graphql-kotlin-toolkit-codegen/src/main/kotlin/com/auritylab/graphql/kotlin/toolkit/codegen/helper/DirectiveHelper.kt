package com.auritylab.graphql.kotlin.toolkit.codegen.helper

import com.squareup.kotlinpoet.ClassName
import graphql.schema.GraphQLDirectiveContainer

/**
 * Describes a helper which provides functions to interact with GraphQL Directives.
 *
 * This code generator currently supports the following directives:
 * - "kGenerate"
 * - "kResolvers"
 * - "kRepresentation"
 */
object DirectiveHelper {
    private const val GENERATE_DIRECTIVE = "kGenerate"
    private const val RESOLVER_DIRECTIVE = "kResolver"
    private const val REPRESENTATION_DIRECTIVE = "kRepresentation"
    private const val DOUBLE_NULL_DIRECTIVE = "kDoubleNull"

    /**
     * Returns if the given [container] contains the [GENERATE_DIRECTIVE].
     */
    fun hasGenerateDirective(container: GraphQLDirectiveContainer): Boolean =
        hasDirective(container, GENERATE_DIRECTIVE)

    /**
     * Returns if the given [container] contains the [RESOLVER_DIRECTIVE].
     */
    fun hasResolverDirective(container: GraphQLDirectiveContainer): Boolean =
        hasDirective(container, RESOLVER_DIRECTIVE)

    /**
     * Returns if the given [container] contains the [REPRESENTATION_DIRECTIVE].
     */
    fun hasRepresentationDirective(container: GraphQLDirectiveContainer): Boolean =
        hasDirective(container, REPRESENTATION_DIRECTIVE)

    /**
     * Returns if the given [container] contains the [DOUBLE_NULL_DIRECTIVE].
     */
    fun hasDoubleNullDirective(container: GraphQLDirectiveContainer): Boolean =
        hasDirective(container, DOUBLE_NULL_DIRECTIVE)

    /**
     * Will check if the given [container] contains the [REPRESENTATION_DIRECTIVE] and
     * if it's present it will take the "class" argument and creates a [ClassName].
     */
    fun getRepresentationClass(container: GraphQLDirectiveContainer): ClassName? {
        // Nothing to do here...
        if (!hasRepresentationDirective(container))
            return null

        // Fetch the directive, the class argument and cast the value to `String?`.
        val className = container.getDirective(REPRESENTATION_DIRECTIVE)
            ?.getArgument("class")
            ?.value as String? ?: return null

        return ClassName.bestGuess(className)
    }

    /**
     * Returns if the given [container] has the given [name] as directive.
     */
    private fun hasDirective(container: GraphQLDirectiveContainer, name: String): Boolean =
        container.getDirective(name) != null
}
