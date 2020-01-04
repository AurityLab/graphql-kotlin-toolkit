package com.auritylab.graphql.kotlin.codegen.mock

import graphql.TypeResolutionEnvironment
import graphql.schema.Coercing
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLScalarType
import graphql.schema.TypeResolver
import graphql.schema.idl.*

class WiringFactoryMock: WiringFactory {
    override fun getScalar(environment: ScalarWiringEnvironment): GraphQLScalarType? {
        return GraphQLScalarType.newScalar()
                .name(environment.scalarTypeDefinition.name)
                .coercing(object: Coercing<String, String> {
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

    override fun providesScalar(environment: ScalarWiringEnvironment): Boolean {
        return !ScalarInfo.isStandardScalar(environment.scalarTypeDefinition.name)
    }

    override fun getTypeResolver(environment: InterfaceWiringEnvironment?): TypeResolver? {
        return object : TypeResolver {
            override fun getType(env: TypeResolutionEnvironment?): GraphQLObjectType? {
                return null
            }
        }
    }

    override fun getTypeResolver(environment: UnionWiringEnvironment?): TypeResolver? {
        return object : TypeResolver {
            override fun getType(env: TypeResolutionEnvironment?): GraphQLObjectType? {
                return null
            }
        }    }

    override fun providesTypeResolver(environment: InterfaceWiringEnvironment?): Boolean {
        return true
    }

    override fun providesTypeResolver(environment: UnionWiringEnvironment?): Boolean {
        return true
    }
}
