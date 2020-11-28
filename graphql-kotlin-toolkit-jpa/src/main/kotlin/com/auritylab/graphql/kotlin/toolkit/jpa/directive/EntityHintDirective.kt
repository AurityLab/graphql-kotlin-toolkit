package com.auritylab.graphql.kotlin.toolkit.jpa.directive

import com.auritylab.graphql.kotlin.toolkit.common.directive.AbstractDirective
import com.auritylab.graphql.kotlin.toolkit.common.directive.HasArgumentsDirective
import graphql.Scalars
import graphql.introspection.Introspection
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLDirectiveContainer
import graphql.schema.GraphQLList

/**
 * Describes the "@kEntityHint" directive which can hold multiple hints for the graph builder.
 */
object EntityHintDirective : AbstractDirective("kEntityHint", false), HasArgumentsDirective<EntityHintDirective.Model> {
    override val reference: GraphQLDirective
        get() = GraphQLDirective.newDirective()
            .name(name)
            .validLocations(Introspection.DirectiveLocation.FIELD)
            .argument(
                GraphQLArgument.newArgument()
                    .name("hints")
                    .type(GraphQLList(Scalars.GraphQLString))
            )
            .build()

    @Suppress("UNCHECKED_CAST")
    override fun getArguments(directive: GraphQLDirective): Model {
        val hints = directive.getArgument("hints").value as? List<String>

        return Model(hints?.toList())
    }

    override fun getArguments(container: GraphQLDirectiveContainer): Model? {
        val directive = container.getDirective(name)
            ?: return null

        return getArguments(directive)
    }

    data class Model(
        val hints: List<String>?
    )
}
