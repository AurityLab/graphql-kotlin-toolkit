package com.auritylab.graphql.kotlin.toolkit.codegen.directive.exception

import com.auritylab.graphql.kotlin.toolkit.codegen.directive.Directive

/**
 * Represents a exception, which will be thrown when the directive was not found on a container.
 */
class DirectiveNotFoundException(directive: Directive, message: String) :
    Exception("Directive '${directive.name}': $message")
