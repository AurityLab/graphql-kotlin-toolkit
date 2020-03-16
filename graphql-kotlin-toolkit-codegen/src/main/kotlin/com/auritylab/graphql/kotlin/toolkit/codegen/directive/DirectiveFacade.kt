package com.auritylab.graphql.kotlin.toolkit.codegen.directive

import com.auritylab.graphql.kotlin.toolkit.codegen.directive.exception.DirectiveValidationException
import com.auritylab.graphql.kotlin.toolkit.codegen.directive.implementation.DoubleNullDirective
import com.auritylab.graphql.kotlin.toolkit.codegen.directive.implementation.ErrorDirective
import com.auritylab.graphql.kotlin.toolkit.codegen.directive.implementation.GenerateDirective
import com.auritylab.graphql.kotlin.toolkit.codegen.directive.implementation.PaginationDirective
import com.auritylab.graphql.kotlin.toolkit.codegen.directive.implementation.RepresentationDirective
import com.auritylab.graphql.kotlin.toolkit.codegen.directive.implementation.ResolverDirective
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLSchema

object DirectiveFacade {
    val generate = GenerateDirective()
    val resolver = ResolverDirective()
    val representation = RepresentationDirective()
    val doubleNull = DoubleNullDirective()
    val error = ErrorDirective()
    val pagination = PaginationDirective()

    // List of all CodegenDirectives
    private val directivesList = listOf<Directive>(generate, resolver, representation, doubleNull, error, pagination)

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
}
