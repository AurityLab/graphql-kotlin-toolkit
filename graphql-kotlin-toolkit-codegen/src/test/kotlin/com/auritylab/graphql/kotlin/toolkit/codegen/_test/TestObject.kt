package com.auritylab.graphql.kotlin.toolkit.codegen._test

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.ImplementerMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.BindingMapper
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema
import java.io.File

internal object TestObject {
    val options = CodegenOptions(hashSetOf(), File("").toPath(), generateAll = false)
    val generatedMapper = GeneratedMapper(options)
    val supportMapper = BindingMapper()
    val kotlinTypeMapper = KotlinTypeMapper(options, generatedMapper, supportMapper)
    val argumentCodeBlockGenerator = ArgumentCodeBlockGenerator(kotlinTypeMapper, supportMapper)
    val implementerMapper = ImplementerMapper(options, schema)

    val schema: GraphQLSchema
        get() = GraphQLSchema.newSchema()
            .query(GraphQLObjectType.newObject().name("Query").build())
            .mutation(GraphQLObjectType.newObject().name("Mutation").build())
            .build()
}
