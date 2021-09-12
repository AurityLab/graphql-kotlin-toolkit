package com.auritylab.graphql.kotlin.toolkit.common.directive.implementation

import com.auritylab.graphql.kotlin.toolkit.common.directive.AbstractDirective
import com.auritylab.graphql.kotlin.toolkit.common.directive.HasArgumentsDirective
import graphql.Scalars
import graphql.introspection.Introspection
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLDirectiveContainer
import graphql.schema.GraphQLList
import graphql.schema.GraphQLNonNull

object RepresentationDirective :
    AbstractDirective("kRepresentation"),
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
            // Argument which defines the FQN of the representation class.
            .argument {
                it.name("class")
                it.type(GraphQLNonNull(Scalars.GraphQLString))
            }
            // Argument which defines a list with parameters for the type.
            .argument {
                it.name("parameters")
                it.type(GraphQLList(GraphQLNonNull(Scalars.GraphQLString)))
            }
            .build()

    override fun getArguments(directive: GraphQLDirective): Model {
        val className = directive.getArgument("class").value as? String ?: ""
        val parameters = directive.getArgument("parameters")?.value as? List<String>

        return Model(className, parameters)
    }

    override fun getArguments(container: GraphQLDirectiveContainer): Model? {
        val directive = container.getDirective(name)
            ?: return null

        return getArguments(directive)
    }

    data class Model(
        /**
         * The FQN name of the representing class.
         * E.g. "java.util.UUID"
         */
        val className: String?,

        /**
         * Optional parameters for the representing class. Each entry is a FQN name of a class.
         * One exception is a star-projection which is represented as "*".
         */
        val parameters: List<String>?
    )
}
