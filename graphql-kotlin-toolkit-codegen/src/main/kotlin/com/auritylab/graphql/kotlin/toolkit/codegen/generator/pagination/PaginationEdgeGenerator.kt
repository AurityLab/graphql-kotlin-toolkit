package com.auritylab.graphql.kotlin.toolkit.codegen.generator.pagination

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.AbstractClassGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.BindingMapper
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName

internal class PaginationEdgeGenerator(
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper,
    bindingMapper: BindingMapper
) : AbstractClassGenerator(options, kotlinTypeMapper, generatedMapper, bindingMapper) {
    override val fileClassName: ClassName = generatedMapper.getPaginationEdgeClassName()

    override fun build(builder: FileSpec.Builder) {
        builder.addType(buildEdgeClass())
    }

    private fun buildEdgeClass(): TypeSpec {
        val typeVariable = TypeVariableName("T", ANY.copy(true))
        val cursorType = STRING

        return TypeSpec.classBuilder(fileClassName)
            .addTypeVariable(typeVariable)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("node", typeVariable)
                    .addParameter("cursor", cursorType)
                    .build()
            )
            .addProperty(PropertySpec.builder("node", typeVariable).initializer("node").build())
            .addProperty(PropertySpec.builder("cursor", cursorType).initializer("cursor").build())
            .build()
    }
}
