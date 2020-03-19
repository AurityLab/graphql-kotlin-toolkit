package com.auritylab.graphql.kotlin.toolkit.common.directive.implementation

import com.auritylab.graphql.kotlin.toolkit.common.directive.AbstractDirective
import graphql.Scalars
import graphql.introspection.Introspection
import graphql.schema.GraphQLDirective

class ErrorDirective : AbstractDirective("kError", false) {
    override val reference: GraphQLDirective =
        GraphQLDirective.newDirective()
            .name(name)
            .argument {
                it.name("name")
                it.type(Scalars.GraphQLString)
            }
            .argument {
                it.name("description")
                it.type(Scalars.GraphQLString)
            }
            .validLocations(Introspection.DirectiveLocation.FIELD_DEFINITION)
            .build()
}
