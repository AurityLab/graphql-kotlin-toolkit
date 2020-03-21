package com.auritylab.graphql.kotlin.toolkit.codegen.generator.pagination

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.AbstractInputDataClassGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ClassName
import graphql.Scalars
import graphql.schema.GraphQLNonNull

internal class PaginationPageInfoGenerator(
    argumentCodeBlockGenerator: ArgumentCodeBlockGenerator,
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper
) : AbstractInputDataClassGenerator(argumentCodeBlockGenerator, options, kotlinTypeMapper, generatedMapper) {
    override val fileClassName: ClassName = generatedMapper.getPaginationPageInfoClassName()

    override val dataProperties: List<DataProperty> = listOf(
        DataProperty("hasNextPage", GraphQLNonNull(Scalars.GraphQLBoolean)),
        DataProperty("hasPreviousPage", GraphQLNonNull(Scalars.GraphQLBoolean)),
        DataProperty("startCursor", Scalars.GraphQLString),
        DataProperty("endCursor", Scalars.GraphQLString)
    )
}
