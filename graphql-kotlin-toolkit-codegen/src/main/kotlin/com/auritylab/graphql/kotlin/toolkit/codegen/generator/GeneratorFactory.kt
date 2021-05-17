package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.fieldResolver.FieldResolverGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.fieldResolver.PaginationFieldResolverGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.meta.MetaFieldsContainerGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.pagination.PaginationConnectionGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.pagination.PaginationEdgeGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.pagination.PaginationInfoGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.pagination.PaginationPageInfoGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.ImplementerMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.BindingMapper
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
    private val implementerMapper: ImplementerMapper,
    private val bindingMapper: BindingMapper
) {
    fun enum(enum: GraphQLEnumType): EnumGenerator =
        EnumGenerator(enum, options, kotlinTypeMapper, generatedMapper, bindingMapper)

    fun fieldResolver(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): FieldResolverGenerator =
        FieldResolverGenerator(
            container,
            field,
            implementerMapper,
            argumentCodeBlockGenerator,
            options,
            kotlinTypeMapper,
            generatedMapper,
            bindingMapper,
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
            generatedMapper,
            bindingMapper,
        )

    fun inputObject(inputObject: GraphQLInputObjectType): InputObjectGenerator =
        InputObjectGenerator(
            inputObject,
            argumentCodeBlockGenerator,
            options,
            kotlinTypeMapper,
            generatedMapper,
            bindingMapper,
        )

    fun objectType(objectType: GraphQLObjectType): ObjectTypeGenerator =
        ObjectTypeGenerator(objectType, options, kotlinTypeMapper, generatedMapper, bindingMapper)

    fun fieldsContainerMeta(fieldsContainer: GraphQLFieldsContainer): MetaFieldsContainerGenerator =
        MetaFieldsContainerGenerator(fieldsContainer, options, kotlinTypeMapper, generatedMapper, bindingMapper)

    fun paginationInfo(): PaginationInfoGenerator =
        PaginationInfoGenerator(
            argumentCodeBlockGenerator,
            options,
            kotlinTypeMapper,
            generatedMapper,
            bindingMapper,
        )

    fun paginationConnection() =
        PaginationConnectionGenerator(options, kotlinTypeMapper, generatedMapper, bindingMapper)

    fun paginationEdge() = PaginationEdgeGenerator(options, kotlinTypeMapper, generatedMapper, bindingMapper)

    fun paginationPageInfo() =
        PaginationPageInfoGenerator(
            argumentCodeBlockGenerator,
            options,
            kotlinTypeMapper,
            generatedMapper,
            bindingMapper,
        )
}
