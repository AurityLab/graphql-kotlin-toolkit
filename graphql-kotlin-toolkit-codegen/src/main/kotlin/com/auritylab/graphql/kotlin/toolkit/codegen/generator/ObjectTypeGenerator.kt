package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.toolkit.common.directive.DirectiveFacade
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType

internal class ObjectTypeGenerator(
    private val objectType: GraphQLObjectType,
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper
) : AbstractClassGenerator(options, kotlinTypeMapper, generatedMapper) {
    override val fileClassName: ClassName = getGeneratedType(objectType)

    override fun build(builder: FileSpec.Builder) {
        builder.addType(buildObjectDataClass(objectType))
    }

    private fun buildObjectDataClass(objectType: GraphQLObjectType): TypeSpec {
        return TypeSpec.classBuilder(getGeneratedType(objectType))
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(buildParameters(objectType.fieldDefinitions))
                    .build()
            )
            .addProperties(buildProperties(objectType.fieldDefinitions))
            .build()
    }

    private fun buildParameters(fields: Collection<GraphQLFieldDefinition>): Collection<ParameterSpec> {
        return fields.map {
            ParameterSpec.builder(it.name, getKotlinType(it.type)).build()
        }
    }

    private fun buildProperties(fields: Collection<GraphQLFieldDefinition>): Collection<PropertySpec> {
        return fields
            .filter { !DirectiveFacade.resolver[it] }
            .map {
                PropertySpec.builder(it.name, getKotlinType(it.type))
                    .initializer(it.name)
                    .build()
            }
    }
}
