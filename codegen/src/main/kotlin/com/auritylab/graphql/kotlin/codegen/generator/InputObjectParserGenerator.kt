package com.auritylab.graphql.kotlin.codegen.generator

import com.auritylab.graphql.kotlin.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.codegen.mapper.NameMapper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import graphql.schema.GraphQLInputObjectType

class InputObjectParserGenerator(
        options: CodegenOptions, kotlinTypeMapper: KotlinTypeMapper, private val nameMapper: NameMapper
) : AbstractGenerator(options, kotlinTypeMapper, nameMapper) {
    private val mapType = ClassName("kotlin.collections", "Map")
            .parameterizedBy(
                    ClassName("kotlin", "String"),
                    ClassName("kotlin", "Any"))

    fun getInputObjectParsers(inputObjects: Collection<GraphQLInputObjectType>): FileSpec {
        return getFileSpecBuilder("InputObjectParsers")
                .addType(TypeSpec
                        .objectBuilder("InputObjectParsers")
                        .also {
                            inputObjects.forEach { obj ->
                                it.addFunction(buildInputObjectParser(obj))
                            }
                        }
                        .build())
                .build()
    }

    fun buildInputObjectParser(inputObjectType: GraphQLInputObjectType): FunSpec {
        val parserName = nameMapper.getInputObjectParser(inputObjectType)
        val inputType = getTypeName(inputObjectType)


        val namedArgs = inputObjectType.fields.map { field ->
            val t = field.type
            val kType = getKotlinType(field.type)
            if (t is GraphQLInputObjectType)
                if (kType.isNullable) {
                    "${field.name} = if (map.containsKey(\"${field.name}\")) ${nameMapper.getInputObjectParser(t).simpleName}(map.get(\"${field.name}\")) as %T else null"
                } else {
                    "${field.name} = ${nameMapper.getInputObjectParser(t).simpleName}(map.get(\"${field.name}\")) as %T"
                }
            else
                if (kType.isNullable) {
                    "${field.name} = if (map.containsKey(\"${field.name}\")) map.get(\"${field.name}\") as %T else null"
                } else {
                    "${field.name} = map.get(\"${field.name}\") as %T"
                }
        }

        return FunSpec.builder(parserName.simpleName)
                .addParameter("map", mapType)
                .addStatement("return %T(${namedArgs.joinToString(", ")})", inputType, *inputObjectType.fields.map { getKotlinType(it.type) }.toTypedArray())
                .build()
    }
}
