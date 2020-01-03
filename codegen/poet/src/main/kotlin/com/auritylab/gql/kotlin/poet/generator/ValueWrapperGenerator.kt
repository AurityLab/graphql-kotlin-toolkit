package com.auritylab.gql.kotlin.poet.generator

import com.auritylab.gql.kotlin.poet.PoetOptions
import com.auritylab.gql.kotlin.poet.mapper.KotlinTypeMapper
import com.auritylab.gql.kotlin.poet.mapper.NameMapper
import com.squareup.kotlinpoet.*

class ValueWrapperGenerator(
        options: PoetOptions, kotlinTypeMapper: KotlinTypeMapper, private val nameMapper: NameMapper
) : AbstractGenerator(options, kotlinTypeMapper, nameMapper) {
    fun getValueWrapper(): FileSpec {
        val name = nameMapper.getValueWrapperName()

        val classType = TypeVariableName("T", Any::class)

        return getFileSpecBuilder(name.className)
                .addType(TypeSpec
                        .classBuilder(ClassName(name.packageName, name.className))
                        .addTypeVariable(classType)
                        .primaryConstructor(FunSpec.constructorBuilder()
                                .addParameter("value", classType)
                                .build())
                        .addProperty(PropertySpec.builder("value", classType).initializer("value").build())
                        .build())
                .build()
    }
}
