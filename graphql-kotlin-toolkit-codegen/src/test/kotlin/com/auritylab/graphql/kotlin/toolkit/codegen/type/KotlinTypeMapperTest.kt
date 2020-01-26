package com.auritylab.graphql.kotlin.toolkit.codegen.type

import com.auritylab.graphql.kotlin.toolkit.codegen._TestObjects
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
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
        val generatedClassNaming = GeneratedMapper(_TestObjects.mockOptions)

        val mapper = KotlinTypeMapper(_TestObjects.mockOptions, generatedClassNaming)

        mapper.getKotlinType(GraphQLList(GraphQLString))
    }
})
