package com.auritylab.graphql.kotlin.toolkit.codegen.generator.pagination

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.AbstractClassGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName

internal class PaginationConnectionGenerator(
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper
) : AbstractClassGenerator(options, kotlinTypeMapper, generatedMapper) {
    override val fileClassName: ClassName = generatedMapper.getPaginationConnectionClassName()

    override fun build(builder: FileSpec.Builder) {
        builder.addType(buildConnectionClass())
    }

    private fun buildConnectionClass(): TypeSpec {
        val typeVariable = TypeVariableName("T", ANY.copy(true))
        val edgesType = LIST.parameterizedBy(generatedMapper.getPaginationEdgeClassName().parameterizedBy(typeVariable))
        val pageInfoType = generatedMapper.getPaginationPageInfoClassName()

        return TypeSpec.classBuilder(fileClassName)
            .addTypeVariable(typeVariable)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("edges", edgesType)
                    .addParameter("pageInfo", pageInfoType)
                    .build()
            )
            .addProperty(PropertySpec.builder("edges", edgesType).initializer("edges").build())
            .addProperty(PropertySpec.builder("pageInfo", pageInfoType).initializer("pageInfo").build())
            .build()
    }
}
