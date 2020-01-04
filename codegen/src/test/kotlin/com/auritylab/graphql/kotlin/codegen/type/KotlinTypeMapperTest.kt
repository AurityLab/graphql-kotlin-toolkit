package com.auritylab.graphql.kotlin.codegen.type

import com.auritylab.graphql.kotlin.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.codegen.mapper.GeneratedMapper
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
        val generatedClassNaming = GeneratedMapper(CodegenOptions())

        val mapper = KotlinTypeMapper(CodegenOptions(), generatedClassNaming, schema)

        mapper.getKotlinType(GraphQLList(GraphQLString))
    }
})
