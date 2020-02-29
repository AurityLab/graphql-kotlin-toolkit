package com.auritylab.graphql.kotlin.toolkit.codegen.directive.implementation

import com.auritylab.graphql.kotlin.toolkit.codegen.directive.AbstractDirective
import graphql.introspection.Introspection
import graphql.schema.GraphQLDirective

class ResolverDirective : AbstractDirective("kResolver", false) {
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
