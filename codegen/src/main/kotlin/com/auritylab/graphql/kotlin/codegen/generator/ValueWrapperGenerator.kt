package com.auritylab.graphql.kotlin.codegen.generator

import com.auritylab.graphql.kotlin.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.codegen.mapper.NameMapper
import com.squareup.kotlinpoet.*

class ValueWrapperGenerator(
        options: CodegenOptions, kotlinTypeMapper: KotlinTypeMapper, private val nameMapper: NameMapper
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
