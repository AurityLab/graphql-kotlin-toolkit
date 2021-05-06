package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.fieldResolver.FieldResolverGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.fieldResolver.PaginationFieldResolverGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.meta.MetaObjectTypeFieldGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.meta.MetaObjectTypeGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.pagination.PaginationConnectionGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.pagination.PaginationEdgeGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.pagination.PaginationInfoGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.pagination.PaginationPageInfoGenerator
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

    fun paginationFieldResolver(
        container: GraphQLFieldsContainer,
        field: GraphQLFieldDefinition
    ): PaginationFieldResolverGenerator =
        PaginationFieldResolverGenerator(
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

    fun objectTypeMeta(objectType: GraphQLObjectType): MetaObjectTypeGenerator =
        MetaObjectTypeGenerator(objectType, options, kotlinTypeMapper, generatedMapper)

    fun metaObjectTypeField(): MetaObjectTypeFieldGenerator =
        MetaObjectTypeFieldGenerator(options, kotlinTypeMapper, generatedMapper)

    fun paginationInfo(): PaginationInfoGenerator =
        PaginationInfoGenerator(
            argumentCodeBlockGenerator,
            options,
            kotlinTypeMapper,
            generatedMapper
        )

    fun paginationConnection() = PaginationConnectionGenerator(options, kotlinTypeMapper, generatedMapper)

    fun paginationEdge() = PaginationEdgeGenerator(options, kotlinTypeMapper, generatedMapper)

    fun paginationPageInfo() =
        PaginationPageInfoGenerator(argumentCodeBlockGenerator, options, kotlinTypeMapper, generatedMapper)
}
