package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenInternalOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.*

internal class EnvironmentWrapperGenerator(
        options: CodegenInternalOptions, kotlinTypeMapper: KotlinTypeMapper, private val generatedMapper: GeneratedMapper
) : AbstractGenerator(options, kotlinTypeMapper, generatedMapper) {
    companion object {
        private val dataFetchingEnvironmentClassName = ClassName("graphql.schema", "DataFetchingEnvironment")
    }

    fun getEnvironmentWrapper(): FileSpec {
        val className = generatedMapper.getEnvironmentWrapperClassName()

        val typeVariableName = TypeVariableName("T", ANY)

        return getFileSpecBuilder(className)
                .addType(TypeSpec.classBuilder(className)
                        .primaryConstructor(FunSpec.constructorBuilder()
                                .addParameter("original", dataFetchingEnvironmentClassName)
                                .addTypeVariable(typeVariableName)
                                .build())
                        .addTypeVariable(typeVariableName)
                        .addSuperinterface(dataFetchingEnvironmentClassName, "original")
                        .addProperty(PropertySpec.builder("parent", typeVariableName)
                                .getter(FunSpec.getterBuilder()
                                        .addStatement("return getSource()")
                                        .build())
                                .build())
                        .build())
                .build()
    }
}
