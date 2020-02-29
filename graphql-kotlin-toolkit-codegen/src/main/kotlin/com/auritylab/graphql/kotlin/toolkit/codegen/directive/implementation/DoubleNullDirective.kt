package com.auritylab.graphql.kotlin.toolkit.codegen.directive.implementation

import com.auritylab.graphql.kotlin.toolkit.codegen.directive.AbstractDirective
import graphql.introspection.Introspection
import graphql.schema.GraphQLDirective

class DoubleNullDirective : AbstractDirective("kDoubleNull", false) {
    override val reference: GraphQLDirective =
        GraphQLDirective.newDirective()
            .name(name)
            .validLocations(
                Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION,
                Introspection.DirectiveLocation.ARGUMENT_DEFINITION
            )
            .build()
}
