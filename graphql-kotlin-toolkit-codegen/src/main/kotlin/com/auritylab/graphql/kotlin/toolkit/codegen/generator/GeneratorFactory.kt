package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.ImplementerMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLObjectType

internal class GeneratorFactory(
    private val options: CodegenOptions,
    private val kotlinTypeMapper: KotlinTypeMapper,
    private val generatedMapper: GeneratedMapper,
    private val argumentCodeBlockGenerator: ArgumentCodeBlockGenerator,
    private val implementerMapper: ImplementerMapper
) {
    fun enum(enum: GraphQLEnumType): EnumGenerator =
        EnumGenerator(enum, options, kotlinTypeMapper, generatedMapper)

    fun environmentWrapper(): EnvironmentWrapperGenerator =
        EnvironmentWrapperGenerator(options, kotlinTypeMapper, generatedMapper)

    fun fieldResolver(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): FieldResolverGenerator =
        FieldResolverGenerator(
            container,
            field,
            implementerMapper,
            argumentCodeBlockGenerator,
            options,
            kotlinTypeMapper,
            generatedMapper
        )

    fun inputObject(inputObject: GraphQLInputObjectType): InputObjectGenerator =
        InputObjectGenerator(inputObject, argumentCodeBlockGenerator, options, kotlinTypeMapper, generatedMapper)

    fun objectType(objectType: GraphQLObjectType): ObjectTypeGenerator =
        ObjectTypeGenerator(objectType, options, kotlinTypeMapper, generatedMapper)

    fun valueWrapper(): ValueWrapperGenerator =
        ValueWrapperGenerator(options, kotlinTypeMapper, generatedMapper)
}
