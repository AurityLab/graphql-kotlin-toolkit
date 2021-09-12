package com.auritylab.graphql.kotlin.toolkit.common.directive

import com.auritylab.graphql.kotlin.toolkit.common.directive.exception.DirectiveValidationException
import com.auritylab.graphql.kotlin.toolkit.common.helper.GraphQLEqualityHelper
import graphql.introspection.Introspection
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLNamedType
import graphql.schema.GraphQLType

abstract class AbstractDirective(
    override val name: String,
    override val required: Boolean
) : Directive {
    /**
     * Will check if the given [directive] will match against the reference ([reference]).
     */
    override fun validateDefinition(directive: GraphQLDirective) {
        // Validate the arguments of the directive.
        reference.arguments.forEach {
            // Resolve the argument by the name and throw exception if not found.
            val argOfDirective = directive.getArgument(it.name)
                ?: throw buildArgumentNotFoundException(it.name)

            // Check if the type of the reference argument is the same as on the given directive.
            if (!GraphQLEqualityHelper.isEqual(argOfDirective.type, it.type))
                throw buildInvalidArgumentTypeException(it.name, it.type)
        }

        // Validate the locations of the directive.
        val locationsOfDirective = directive.validLocations()
        reference.validLocations().forEach {
            // Check if the location from the reference exists on the directive.
            if (!locationsOfDirective.contains(it))
                throw buildInvalidLocationException(it)
        }
    }

    /**
     * Will build a [DirectiveValidationException] which tells that the argument with the given [name] could not be
     * found on the directive definition.
     */
    private fun buildArgumentNotFoundException(
        name: String
    ): DirectiveValidationException =
        DirectiveValidationException(this, "Argument '$name' not found on directive")

    /**
     * Will build a [DirectiveValidationException] which tells that the argument with the given [argumentName] was
     * defined with the wrong type. The [expectedType] tells the expected type for the argument.
     */
    private fun buildInvalidArgumentTypeException(
        argumentName: String,
        expectedType: GraphQLType
    ): DirectiveValidationException =
        DirectiveValidationException(
            this,
            "Argument '$argumentName' is expected to be type of '${if (expectedType is GraphQLNamedType) expectedType.name else "unknown"}'"
        )

    /**
     * Will build a [DirectiveValidationException] which tells directive requires the given [requiredLocation].
     */
    private fun buildInvalidLocationException(
        requiredLocation: Introspection.DirectiveLocation
    ): DirectiveValidationException =
        DirectiveValidationException(this, "Directive location '${requiredLocation.name}' not found on directive")
}
