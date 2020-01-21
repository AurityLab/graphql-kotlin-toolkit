package com.auritylab.graphql.kotlin.toolkit.codegen.mock

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import graphql.schema.TypeResolver
import graphql.schema.idl.InterfaceWiringEnvironment
import graphql.schema.idl.ScalarInfo
import graphql.schema.idl.ScalarWiringEnvironment
import graphql.schema.idl.UnionWiringEnvironment
import graphql.schema.idl.WiringFactory

/**
 * Implements a [WiringFactory] which does exactly nothing.
 * This is necessary because graphql-java requires some additional type information during the parsing.
 */
class WiringFactoryMock : WiringFactory {
    override fun getScalar(environment: ScalarWiringEnvironment): GraphQLScalarType? {
        return GraphQLScalarType.newScalar()
            .name(environment.scalarTypeDefinition.name)
            .coercing(object : Coercing<String, String> {
                override fun parseValue(input: Any?): String {
                    return ""
                }

                override fun parseLiteral(input: Any?): String {
                    return ""
                }

                override fun serialize(dataFetcherResult: Any?): String {
                    return ""
                }
            })
            .build()
    }

    override fun providesScalar(environment: ScalarWiringEnvironment): Boolean =
        !ScalarInfo.isStandardScalar(environment.scalarTypeDefinition.name)

    override fun getTypeResolver(environment: InterfaceWiringEnvironment?): TypeResolver? = TypeResolver { null }

    override fun getTypeResolver(environment: UnionWiringEnvironment?): TypeResolver? = TypeResolver { null }

    override fun providesTypeResolver(environment: InterfaceWiringEnvironment?): Boolean = true

    override fun providesTypeResolver(environment: UnionWiringEnvironment?): Boolean = true
}
