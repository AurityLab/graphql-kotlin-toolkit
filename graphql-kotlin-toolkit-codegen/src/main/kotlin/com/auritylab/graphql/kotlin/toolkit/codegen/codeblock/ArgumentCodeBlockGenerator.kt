package com.auritylab.graphql.kotlin.toolkit.codegen.codeblock

import com.auritylab.graphql.kotlin.toolkit.codegen.helper.NamingHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.toolkit.common.directive.DirectiveFacade
import com.auritylab.graphql.kotlin.toolkit.common.helper.GraphQLTypeHelper
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLDirectiveContainer
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLType

/**
 * Describes a generator which generates functions which are able to access specific arguments from any given GraphQL input.
 */
internal class ArgumentCodeBlockGenerator(
    private val typeMapper: KotlinTypeMapper,
    private val generatedMapper: GeneratedMapper
) {
    /**
     * Will build a function which takes a map (parameterized by String, Any) to access the name of the given [definition].
     */
    fun buildResolver(definition: GraphQLArgument): FunSpec =
        buildResolver(definition.name, definition.type, definition)

    /**
     * Will build a function which takes a map (parameterized by String, Any) to access the given [argumentName] from it.
     * A [type] is also required to decide how to access the argument value.
     */
    fun buildResolver(
        argumentName: String,
        type: GraphQLType,
        fieldDirectiveContainer: GraphQLDirectiveContainer?
    ) = FunSpec.builder("resolve${NamingHelper.uppercaseFirstLetter(argumentName)}")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("map", MAP.parameterizedBy(STRING, ANY))
        .returns(typeMapper.getKotlinType(type, fieldDirectiveContainer))
        .addCode(buildArgumentResolverCodeBlock(argumentName, type, fieldDirectiveContainer))
        .build()

    /**
     * Will build the [CodeBlock], which contains all the functions to resolve each layer and build the final result
     * for the given [type].
     */
    private fun buildArgumentResolverCodeBlock(
        name: String,
        type: GraphQLType,
        fieldDirectiveContainer: GraphQLDirectiveContainer?
    ): CodeBlock {
        val code = CodeBlock.builder()

        // Get all layers of the type.
        val layers = GraphQLTypeHelper.unwrapTypeLayers(type).asReversed()

        var currentIndex = 0
        var lastType: TypeName = ANY
        layers.forEachIndexed { index, tt ->
            val next = if (index < layers.lastIndex) layers[index + 1] else null

            if (next !is GraphQLNonNull) {
                val layerParser = buildLayerParser(tt, currentIndex, null)
                code.add(layerParser.first)
                lastType = layerParser.second
                currentIndex++
            }
        }

        val lastLayerIndex = currentIndex - 1

        if (fieldDirectiveContainer != null && lastType.isNullable && DirectiveFacade.doubleNull[fieldDirectiveContainer])
            code.addStatement(
                "return if (map.containsKey(\"%L\")) %T(layer%L(map[\"%L\"] as %T)) else null",
                name,
                generatedMapper.getValueWrapperName(),
                lastLayerIndex,
                name,
                lastType
            )
        else
            code.addStatement(
                "return layer%L(map[\"%L\"] as %T)",
                lastLayerIndex,
                name,
                lastType
            )

        return code.build()
    }

    /**
     * Will build the [CodeBlock] which will resolve the given [type].
     */
    private fun buildLayerParser(
        type: GraphQLType,
        index: Int,
        kotlinType: TypeName? = null
    ): Pair<CodeBlock, TypeName> {
        val code = CodeBlock.builder()
        val kType = kotlinType ?: typeMapper.getInputKotlinType(type)

        when (type) {
            is GraphQLNonNull -> return Pair(buildLayerParser(type.wrappedType, index, kType).first, kType)
            is GraphQLList -> {
                val wrappedKType = typeMapper.getInputKotlinType(type.wrappedType)

                if (wrappedKType.isNullable) {
                    if (kType.isNullable)
                        code.addStatement(
                            "val layer$index = {it: %T -> if(it == null) null else it.map { if (it == null) null else layer${index - 1}(it) }}",
                            kType
                        )
                    else
                        code.addStatement(
                            "val layer$index = {it: %T -> it.map { if (it == null) null else layer${index - 1}(it) }}",
                            kType
                        )
                } else {
                    if (kType.isNullable)
                        code.addStatement(
                            "val layer$index = {it: %T -> if(it == null) null else it.map { layer${index - 1}(it) }}",
                            kType
                        )
                    else
                        code.addStatement(
                            "val layer$index = {it: %T -> it.map { layer${index - 1}(it) }}",
                            kType
                        )
                }
            }
            is GraphQLEnumType -> {
                // Fetch the Kotlin Type for the enum and copy it to a not nullable type.
                // This is required because it would generate code like this:
                // "GQLAnyEnum?.valueOf(it)"
                // which is definitely invalid.
                val enumClass = typeMapper.getKotlinType(type).copy(false)

                if (kType.isNullable)
                    code.addStatement(
                        "val layer$index = {it: %T -> if(it == null) null else %T.valueOf(it)}",
                        kType,
                        enumClass
                    )
                else
                    code.addStatement(
                        "val layer$index = {it: %T -> %T.valueOf(it)}",
                        kType,
                        enumClass
                    )
            }
            is GraphQLInputObjectType -> {
                val inputObjectBuilder = typeMapper.getInputObjectBuilder(type)
                if (kType.isNullable)
                    code.addStatement(
                        "val layer$index = {it: %T -> if(it == null) null else %M(it)}",
                        kType,
                        inputObjectBuilder
                    )
                else
                    code.addStatement(
                        "val layer$index = {it: %T -> %M(it)}",
                        kType,
                        inputObjectBuilder
                    )
            }
            else -> {
                code.addStatement("val layer$index = {it: %T -> it as %T}", kType, kType)
            }
        }

        return Pair(code.build(), kType)
    }
}
