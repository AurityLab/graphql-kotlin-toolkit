package com.auritylab.graphql.kotlin.toolkit.codegen.codeblock

import com.auritylab.graphql.kotlin.toolkit.codegen.directive.DirectiveFacade
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.NamingHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import graphql.schema.GraphQLDirectiveContainer
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLModifiedType
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
     * Will build a function which takes a map (parameterized by String, Any) to access the given [argumentName] from it.
     * A [type] is also required to decide how to access the argument value.
     */
    fun buildArgumentResolverFun(
        argumentName: String,
        type: GraphQLType,
        fieldDirectiveContainer: GraphQLDirectiveContainer
    ): FunSpec {
        val kotlinType = typeMapper.getKotlinType(type, fieldDirectiveContainer)

        return FunSpec.builder("resolve${NamingHelper.uppercaseFirstLetter(argumentName)}")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("map", MAP.parameterizedBy(STRING, ANY))
            .returns(kotlinType)
            .addCode(buildArgumentResolverCodeBlock(argumentName, type, fieldDirectiveContainer))
            .build()
    }

    private fun buildArgumentResolverCodeBlock(
        name: String,
        type: GraphQLType,
        fieldDirectiveContainer: GraphQLDirectiveContainer
    ): CodeBlock {
        val code = CodeBlock.builder()

        // Get all layers of the type.
        val layers = unwrapTypeLayers(type).asReversed()

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

        if (lastType.isNullable && DirectiveFacade.doubleNull[fieldDirectiveContainer])
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
     * A [GraphQLType] can be wrapped with unmodifiable types (e.g. List, NonNull).
     * This method will unwrap until an actual type.
     */
    private fun unwrapTypeLayers(type: GraphQLType): List<GraphQLType> {
        val wraps = mutableListOf<GraphQLType>()

        var c = type
        while (c is GraphQLModifiedType) {
            wraps.add(c)
            c = c.wrappedType
        }

        wraps.add(c)

        return wraps
    }

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
