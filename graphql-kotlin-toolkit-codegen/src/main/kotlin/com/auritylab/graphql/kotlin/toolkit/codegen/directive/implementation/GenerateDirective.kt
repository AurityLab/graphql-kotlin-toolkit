package com.auritylab.graphql.kotlin.toolkit.codegen.directive.implementation

import com.auritylab.graphql.kotlin.toolkit.codegen.directive.AbstractDirective
import graphql.introspection.Introspection
import graphql.schema.GraphQLDirective

class GenerateDirective : AbstractDirective("kGenerate", false) {
    override val reference: GraphQLDirective =
        GraphQLDirective.newDirective()
            .name(name)
            .validLocations(Introspection.DirectiveLocation.OBJECT)
            .build()
}
