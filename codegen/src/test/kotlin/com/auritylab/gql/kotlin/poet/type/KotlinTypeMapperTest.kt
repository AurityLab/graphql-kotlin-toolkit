package com.auritylab.gql.kotlin.poet.type

import com.auritylab.gql.kotlin.poet.PoetOptions
import com.auritylab.gql.kotlin.poet.mapper.KotlinTypeMapper
import com.auritylab.gql.kotlin.poet.mapper.NameMapper
import graphql.Scalars.GraphQLString
import graphql.schema.GraphQLList
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema
import io.kotlintest.specs.StringSpec

internal class KotlinTypeMapperTest : StringSpec({
    "Should return correct type for default scalar" {
        val testObjectType = GraphQLObjectType.newObject()
                .name("Query").field { it.name("getUser"); it.type(GraphQLString) }.build()


        val schema = GraphQLSchema.newSchema().query(testObjectType).build()
        val generatedClassNaming = NameMapper(PoetOptions())

        val mapper = KotlinTypeMapper(PoetOptions(), generatedClassNaming, schema)

        mapper.getKotlinType(GraphQLList(GraphQLString))
    }
})
