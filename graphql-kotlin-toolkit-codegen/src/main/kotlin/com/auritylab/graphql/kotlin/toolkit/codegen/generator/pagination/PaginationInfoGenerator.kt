package com.auritylab.graphql.kotlin.toolkit.codegen.generator.pagination

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.AbstractInputDataClassGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ClassName
import graphql.Scalars

internal class PaginationInfoGenerator(
    argumentCodeBlockGenerator: ArgumentCodeBlockGenerator,
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper
) : AbstractInputDataClassGenerator(argumentCodeBlockGenerator, options, kotlinTypeMapper, generatedMapper) {
    override val fileClassName: ClassName =
        generatedMapper.getPaginationInfoClassName()

    override val dataProperties: List<DataProperty> =
        listOf(
            DataProperty(
                "first",
                Scalars.GraphQLInt
            ),
            DataProperty(
                "last",
                Scalars.GraphQLInt
            ),
            DataProperty(
                "after",
                Scalars.GraphQLString
            ),
            DataProperty(
                "before",
                Scalars.GraphQLString
            )
        )
}
