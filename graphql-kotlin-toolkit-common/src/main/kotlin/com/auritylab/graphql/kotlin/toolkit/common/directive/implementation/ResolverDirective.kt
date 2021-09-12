package com.auritylab.graphql.kotlin.toolkit.common.directive.implementation

import com.auritylab.graphql.kotlin.toolkit.common.directive.AbstractDirective
import graphql.introspection.Introspection
import graphql.schema.GraphQLDirective

object ResolverDirective : AbstractDirective("kResolver", false) {
    override val reference: GraphQLDirective =
        GraphQLDirective.newDirective()
            .name(name)
            .validLocations(
                Introspection.DirectiveLocation.FIELD_DEFINITION,
                Introspection.DirectiveLocation.OBJECT,
                Introspection.DirectiveLocation.INTERFACE
            )
            .build()
}
