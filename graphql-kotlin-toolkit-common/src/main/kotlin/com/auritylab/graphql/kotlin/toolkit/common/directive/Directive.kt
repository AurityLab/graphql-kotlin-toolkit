package com.auritylab.graphql.kotlin.toolkit.common.directive

import com.auritylab.graphql.kotlin.toolkit.common.directive.exception.DirectiveValidationException
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLDirectiveContainer

/**
 * Describes a directive which is used by the code generator.
 */
interface Directive {
    /**
     * The name of the directive.
     */
    val name: String

    /**
     * If the directive MUST be present on the schema.
     */
    val required: Boolean

    /**
     * The reference [GraphQLDirective], which is represented here.
     */
    val reference: GraphQLDirective

    /**
     * Will validate the definition of given [directive]. This has to validate if all arguments are defined with
     * the correct scalar, etc.
     *
     * @param directive The directive definition to validate against.
     * @throws DirectiveValidationException If the validation was not successful.
     */
    fun validateDefinition(directive: GraphQLDirective)

    /**
     * Will check if this directive exists on the given [container].
     *
     * @param container The container to check against.
     * @return If this directive exists on the given [container].
     */
    fun existsOnContainer(container: GraphQLDirectiveContainer): Boolean =
        container.getDirective(name) != null

    /**
     * @see existsOnContainer
     */
    operator fun get(container: GraphQLDirectiveContainer): Boolean =
        existsOnContainer(container)
}
