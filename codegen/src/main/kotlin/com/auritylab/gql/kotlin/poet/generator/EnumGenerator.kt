package com.auritylab.gql.kotlin.poet.generator

import com.auritylab.gql.kotlin.poet.PoetOptions
import com.auritylab.gql.kotlin.poet.mapper.KotlinTypeMapper
import com.auritylab.gql.kotlin.poet.mapper.NameMapper
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.schema.GraphQLEnumType

class EnumGenerator(
        options: PoetOptions, kotlinTypeMapper: KotlinTypeMapper, private val nameMapper: NameMapper
) : AbstractGenerator(options, kotlinTypeMapper, nameMapper) {
    fun getEnum(enum: GraphQLEnumType): FileSpec {
        val fieldResolverName = nameMapper.getTypeName(enum)

        return getFileSpecBuilder(fieldResolverName.className)
                .addType(buildEnumClass(enum))
                .build()
    }

    fun buildEnumClass(enum: GraphQLEnumType): TypeSpec {
        return TypeSpec.enumBuilder(getTypeName(enum))
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter("stringValue", String::class)
                        .build())
                .addProperty(PropertySpec.builder("stringValue", String::class)
                        .initializer("stringValue")
                        .build())
                .also {
                    enum.values.forEach { enum ->
                        it.addEnumConstant(enum.name.toUpperCase(), TypeSpec.anonymousClassBuilder()
                                .addSuperclassConstructorParameter("%S", enum.name.toUpperCase())
                                .build())
                    }
                }
                .build()
    }
}
