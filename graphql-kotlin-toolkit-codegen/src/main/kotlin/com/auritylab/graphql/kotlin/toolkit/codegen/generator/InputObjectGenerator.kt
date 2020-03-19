package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ClassName
import graphql.schema.GraphQLInputObjectType

/**
 * Implements a [AbstractClassGenerator] which will generate the source code for a [GraphQLInputObjectType].
 * It will generate the actual `data class` and a method which can parse a map to the `data class`
 */
internal class InputObjectGenerator(
    inputObjectType: GraphQLInputObjectType,
    argumentCodeBlockGenerator: ArgumentCodeBlockGenerator,
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper
) : AbstractInputDataClassGenerator(argumentCodeBlockGenerator, options, kotlinTypeMapper, generatedMapper) {
    override val fileClassName: ClassName = getGeneratedType(inputObjectType)

    override val dataProperties: List<DataProperty> = inputObjectType.fields
        .map { DataProperty(it.name, it.type, it) }
}
