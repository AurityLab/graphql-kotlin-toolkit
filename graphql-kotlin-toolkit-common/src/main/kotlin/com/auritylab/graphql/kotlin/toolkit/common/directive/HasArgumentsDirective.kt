package com.auritylab.graphql.kotlin.toolkit.common.directive

import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLDirectiveContainer

/**
 * Describes a [Directive], which has arguments.
 */
interface HasArgumentsDirective<M : Any> : Directive {
    /**
     * Will resolve the given [directive] into an ínstance of [M], which is a representation of the arguments.
     */
    fun getArguments(directive: GraphQLDirective): M

    /**
     * Will resolve this directive in the given [container] into an instance of [M], which is a representation of the
     * arguments.
     */
    fun getArguments(container: GraphQLDirectiveContainer): M?
}
