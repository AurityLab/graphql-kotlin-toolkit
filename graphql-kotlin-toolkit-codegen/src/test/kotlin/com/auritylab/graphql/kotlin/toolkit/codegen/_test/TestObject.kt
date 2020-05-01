package com.auritylab.graphql.kotlin.toolkit.codegen._test

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.ImplementerMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema
import java.nio.file.Path

internal object TestObject {
    val options = CodegenOptions(hashSetOf(), Path.of(""))
    val generatedMapper = GeneratedMapper(options)
    val kotlinTypeMapper = KotlinTypeMapper(options, generatedMapper)
    val argumentCodeBlockGenerator = ArgumentCodeBlockGenerator(kotlinTypeMapper, generatedMapper)
    val implementerMapper = ImplementerMapper(options, schema)

    val schema: GraphQLSchema
        get() = GraphQLSchema.newSchema()
            .query(GraphQLObjectType.newObject().name("Query").build())
            .mutation(GraphQLObjectType.newObject().name("Mutation").build())
            .build()
}


