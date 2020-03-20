package com.auritylab.graphql.kotlin.toolkit.codegen.generator.fieldResolver

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.NamingHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.ImplementerMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer

internal class FieldResolverGenerator(
    container: GraphQLFieldsContainer,
    field: GraphQLFieldDefinition,
    implementerMapper: ImplementerMapper,
    argumentCodeBlockGenerator: ArgumentCodeBlockGenerator,
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper
) : AbstractFieldResolverGenerator(
    container,
    field,
    implementerMapper,
    argumentCodeBlockGenerator,
    options,
    kotlinTypeMapper,
    generatedMapper
) {
    override fun buildFieldResolverClass(builder: TypeSpec.Builder) {
        builder
            .addSuperinterface(dataFetcherClassName)
            .addType(environmentTypeSpec)

        builder.addFunction(
            FunSpec.builder("resolve")
                .addModifiers(KModifier.ABSTRACT)
                .addParameters(field.arguments.map { ParameterSpec(it.name, getKotlinType(it.type, it)) })
                .addParameter("env", generatedMapper.getFieldResolverEnvironment(container, field))
                .returns(fieldKotlinType).build()
        )

        builder.addFunction(FunSpec.builder("get")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("env", ClassName("graphql.schema", "DataFetchingEnvironment"))
            .returns(getKotlinType(field.type))
            .also { getFunSpec ->
                val resolveArgs = field.arguments.let { args ->
                    if (args.isEmpty())
                        return@let ""

                    return@let args.joinToString(", ") {
                        "${it.name} = resolve${NamingHelper.uppercaseFirstLetter(it.name)}(map)"
                    } + ", "
                }

                getFunSpec.addStatement("val map = env.arguments")
                getFunSpec.addStatement(
                    "return resolve(${resolveArgs}env = %T(env))",
                    generatedMapper.getFieldResolverEnvironment(container, field)
                )
            }
            .build())
    }

    private val environmentTypeSpec =
        TypeSpec.classBuilder(generatedMapper.getFieldResolverEnvironment(container, field))
            .primaryConstructor(
                // Create the primary constructor which accepts a parameter "original" of type "DataFetchingEnvironment".
                FunSpec.constructorBuilder()
                    .addParameter("original", dataFetchingEnvironmentClassName)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("original", dataFetchingEnvironmentClassName)
                    .initializer("original")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("parent", parentTypeName.copy(false))
                    .getter(FunSpec.getterBuilder().addCode("return original.getSource()").build())
                    .build()
            )
            .addProperty(
                PropertySpec.builder("context", contextClassName)
                    .getter(FunSpec.getterBuilder().addCode("return original.getContext()").build())
                    .build()
            )
            .build()
}