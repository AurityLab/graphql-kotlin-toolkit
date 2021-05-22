package com.auritylab.graphql.kotlin.toolkit.codegen._test

import graphql.Scalars
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLList
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType

object TestUtils {
    /**
     * Will build a 'kRepresentation' directive with the given [clazz] as value for the class argument.
     */
    fun getRepresentationDirective(
        clazz: String = "kotlin.String",
        parameters: List<String>? = null
    ): GraphQLDirective =
        GraphQLDirective.newDirective().name("kRepresentation")
            .argument { arg ->
                arg.name("class")
                arg.type(Scalars.GraphQLString)
                arg.value(clazz)
            }
            .argument { arg ->
                arg.name("parameters")
                arg.type(GraphQLList(GraphQLNonNull(Scalars.GraphQLString)))
                arg.value(parameters)
            }
            .build()

    /**
     * Will create a new dummy ObjectType with the givne [name] and the given [directives].
     */
    fun getDummyObjectType(
        name: String = "TestObjectType",
        vararg directives: GraphQLDirective
    ): GraphQLObjectType =
        GraphQLObjectType.newObject()
            .name(name)
            .withDirectives(*directives)
            .build()
}
