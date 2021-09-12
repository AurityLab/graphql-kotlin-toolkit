package com.auritylab.graphql.kotlin.toolkit.common.directive.exception

import com.auritylab.graphql.kotlin.toolkit.common.directive.Directive

/**
 * Represents an exception, which will be thrown if the directive validation failed.
 *
 * @see Directive.validateDefinition
 */
class DirectiveValidationException(
    directive: Directive,
    message: String
) : Exception("Directive '${directive.name}': $message")
