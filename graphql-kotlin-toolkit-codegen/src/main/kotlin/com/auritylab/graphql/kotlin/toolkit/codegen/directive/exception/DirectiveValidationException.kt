package com.auritylab.graphql.kotlin.toolkit.codegen.directive.exception

import com.auritylab.graphql.kotlin.toolkit.codegen.directive.Directive

/**
 * Represents a exception, which will be thrown if the directive validation was not successful
 *
 * @see Directive.validateDefinition
 */
class DirectiveValidationException(
    directive: Directive,
    message: String
) : Exception("Directive '${directive.name}': $message")
