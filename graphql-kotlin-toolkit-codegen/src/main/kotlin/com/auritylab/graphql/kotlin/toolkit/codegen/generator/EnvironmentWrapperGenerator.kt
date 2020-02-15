package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName

/**
 * Implements a [AbstractClassGenerator] which will generate a Class which delegates to
 * `graphql.schema.DataFetchingEnvironment`.
 * In addition to the delegation, it will add "parent" property of type `T`, which simply returns the casted value
 * of `getSource()`.
 * */
internal class EnvironmentWrapperGenerator(
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper
) : AbstractClassGenerator(options, kotlinTypeMapper, generatedMapper) {
    companion object {
        private val dataFetchingEnvironmentClassName = ClassName("graphql.schema", "DataFetchingEnvironment")
        private val typeVariableName = TypeVariableName("T", ANY)
    }

    override val fileClassName: ClassName = generatedMapper.getEnvironmentWrapperClassName()

    override fun build(builder: FileSpec.Builder) {
        builder.addType(
            TypeSpec.classBuilder(fileClassName)
                .primaryConstructor(
                    // Create the primary constructor, which accepts a instance of DataFetchingEnvironment.
                    FunSpec.constructorBuilder()
                        .addParameter("original", dataFetchingEnvironmentClassName)
                        .addTypeVariable(typeVariableName)
                        .build()
                )
                .addTypeVariable(typeVariableName)
                .addSuperinterface(dataFetchingEnvironmentClassName, "original")
                .addProperty(
                    // Add the "parent" property, which delegates to "getSource()".
                    // The type is defined through "T".
                    PropertySpec.builder("parent", typeVariableName)
                        .getter(
                            FunSpec.getterBuilder()
                                .addStatement("return getSource()")
                                .build()
                        ).build()
                ).build()
        )
    }
}
