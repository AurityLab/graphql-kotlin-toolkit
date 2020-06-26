package com.auritylab.graphql.kotlin.toolkit.common.directive.implementation

import com.auritylab.graphql.kotlin.toolkit.common.directive.AbstractDirective
import com.auritylab.graphql.kotlin.toolkit.common.directive.HasArgumentsDirective
import graphql.Scalars
import graphql.introspection.Introspection
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLDirectiveContainer
import graphql.schema.GraphQLNonNull

class RepresentationDirective : AbstractDirective("kRepresentation", false),
    HasArgumentsDirective<RepresentationDirective.Model> {

    override val reference: GraphQLDirective =
        GraphQLDirective.newDirective()
            .name(name)
            .validLocations(
                Introspection.DirectiveLocation.OBJECT,
                Introspection.DirectiveLocation.SCALAR,
                Introspection.DirectiveLocation.INTERFACE,
                Introspection.DirectiveLocation.ENUM
            )
            .argument {
                it.name("class")
                it.type(GraphQLNonNull(Scalars.GraphQLString))
            }
            .build()

    override fun getArguments(directive: GraphQLDirective): Model {
        val className = directive.getArgument("class").value as? String ?: ""

        return Model(className)
    }

    override fun getArguments(container: GraphQLDirectiveContainer): Model? {
        val directive = container.getDirective(name)
            ?: return null

        return getArguments(directive)
    }

    data class Model(
        val className: String?
    )
}
