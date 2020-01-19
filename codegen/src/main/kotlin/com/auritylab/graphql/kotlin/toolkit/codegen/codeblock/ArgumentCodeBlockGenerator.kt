package com.auritylab.graphql.kotlin.toolkit.codegen.codeblock

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
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLModifiedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLType

internal class ArgumentCodeBlockGenerator(
    private val kotlinTypeMapper: KotlinTypeMapper,
    private val generatedMapper: GeneratedMapper
) {
    private val inputMapType = MAP.parameterizedBy(STRING, ANY)

    fun buildArgumentResolverFun(argumentName: String, mapName: String, type: GraphQLType): FunSpec {
        val kotlinType = kotlinTypeMapper.getKotlinType(type)

        return FunSpec.builder("resolve${NamingHelper.uppercaseFirstLetter(argumentName)}")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("map", inputMapType)
            .returns(kotlinType)
            .addCode(buildArgumentResolverCodeBlock(argumentName, type))
            .build()
    }

    private fun buildArgumentResolverCodeBlock(name: String, type: GraphQLType): CodeBlock {
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

        code.addStatement("return layer${currentIndex - 1}(map.get(\"$name\") as %T)", lastType)

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
        val kType = kotlinType ?: kotlinTypeMapper.getInputKotlinType(type)

        when (type) {
            is GraphQLNonNull -> return Pair(buildLayerParser(type.wrappedType, index, kType).first, kType)
            is GraphQLList -> {
                val wrappedKType = kotlinTypeMapper.getInputKotlinType(type.wrappedType)

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
                val enumClass = generatedMapper.getGeneratedTypeClassName(type, false)

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
                val inputObjectBuilder = generatedMapper.getInputObjectBuilderMemberName(type)
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
