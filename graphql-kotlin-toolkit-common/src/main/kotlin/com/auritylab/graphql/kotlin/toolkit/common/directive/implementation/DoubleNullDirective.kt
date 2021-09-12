package com.auritylab.graphql.kotlin.toolkit.common.directive.implementation

import com.auritylab.graphql.kotlin.toolkit.common.directive.AbstractDirective
import graphql.introspection.Introspection
import graphql.schema.GraphQLDirective

object DoubleNullDirective : AbstractDirective("kDoubleNull", false) {
    override val reference: GraphQLDirective =
        GraphQLDirective.newDirective()
            .name(name)
            .validLocations(
                Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION,
                Introspection.DirectiveLocation.ARGUMENT_DEFINITION
            )
            .build()
}
