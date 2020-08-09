package com.auritylab.graphql.kotlin.toolkit.jpa.directive

import com.auritylab.graphql.kotlin.toolkit.common.directive.AbstractDirective
import com.auritylab.graphql.kotlin.toolkit.common.directive.HasArgumentsDirective
import graphql.introspection.Introspection
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLDirectiveContainer

object EntityHintDirective : AbstractDirective("kEntityHint", false), HasArgumentsDirective<EntityHintDirective.Model> {
    override val reference: GraphQLDirective
        get() = GraphQLDirective.newDirective()
            .name(name)
            .validLocations(Introspection.DirectiveLocation.FIELD)
            .build()

    data class Model(
        val hints: List<String>?
    )

    override fun getArguments(directive: GraphQLDirective): Model {
        val hints = directive.getArgument("hints").value as? List<String>

        return Model(hints?.toList())
    }

    override fun getArguments(container: GraphQLDirectiveContainer): Model? {
        val directive = container.getDirective(name)
            ?: return null

        return getArguments(directive)
    }
}
