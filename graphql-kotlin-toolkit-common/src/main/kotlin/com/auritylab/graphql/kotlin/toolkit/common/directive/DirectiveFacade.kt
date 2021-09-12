package com.auritylab.graphql.kotlin.toolkit.common.directive

import com.auritylab.graphql.kotlin.toolkit.common.directive.exception.DirectiveValidationException
import com.auritylab.graphql.kotlin.toolkit.common.directive.implementation.DoubleNullDirective
import com.auritylab.graphql.kotlin.toolkit.common.directive.implementation.GenerateDirective
import com.auritylab.graphql.kotlin.toolkit.common.directive.implementation.PaginationDirective
import com.auritylab.graphql.kotlin.toolkit.common.directive.implementation.RepresentationDirective
import com.auritylab.graphql.kotlin.toolkit.common.directive.implementation.ResolverDirective
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLSchema

object DirectiveFacade {
    // List of all available directives.
    private val directivesList = listOf<Directive>(
        Defaults.generate,
        Defaults.resolver,
        Defaults.representation,
        Defaults.doubleNull,
        Defaults.pagination
    )

    /**
     * Will validate all existing [Directive]s on the given [schema].
     *
     * @throws DirectiveValidationException If the validation of any directive fails.
     */
    fun validateAllOnSchema(schema: GraphQLSchema) {
        directivesList.forEach { codegenDirective ->
            getDirectiveDefinition(codegenDirective, schema)
                ?.let { codegenDirective.validateDefinition(it) }
        }
    }

    /**
     * Will check if the given [directive] exists on the given [schema] (using [Directive.name]).
     * Additional it will return the [GraphQLDirective] if it available on the schema
     *
     * @throws DirectiveValidationException If the directive is required but is not present on the [schema].
     */
    private fun getDirectiveDefinition(directive: Directive, schema: GraphQLSchema): GraphQLDirective? {
        val schemaDirective = schema.getDirective(directive.name)

        // Throw exception if the schema is required but not given on the schema.
        if (schemaDirective == null && directive.required)
            throw DirectiveValidationException(
                directive,
                "Not present on the schema, but is required. Consider adding it."
            )

        return schemaDirective
    }

    /**
     * Object which contains all available default directives.
     */
    object Defaults {
        val generate = GenerateDirective
        val resolver = ResolverDirective
        val representation = RepresentationDirective
        val doubleNull = DoubleNullDirective
        val pagination = PaginationDirective
    }
}
