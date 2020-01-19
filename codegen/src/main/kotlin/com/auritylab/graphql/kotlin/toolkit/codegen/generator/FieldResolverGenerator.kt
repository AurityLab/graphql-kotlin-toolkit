package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.NamingHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer

internal class FieldResolverGenerator(
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    private val generatedMapper: GeneratedMapper,
    private val argumentCodeBlockGenerator: ArgumentCodeBlockGenerator
) : AbstractGenerator(options, kotlinTypeMapper, generatedMapper) {
    val inputMapType = ClassName("kotlin.collections", "Map")
        .parameterizedBy(
            ClassName("kotlin", "String"),
            ClassName("kotlin", "Any")
        )

    fun getFieldResolver(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): FileSpec {
        val fieldResolverClassName = generatedMapper.getGeneratedFieldResolverClassName(container, field)

        return getFileSpecBuilder(fieldResolverClassName)
            .addType(buildFieldResolverClass(container, field)).build()
    }

    private fun buildFieldResolverClass(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): TypeSpec {
        val fieldResolverClassName = generatedMapper.getGeneratedFieldResolverClassName(container, field)
        val fieldOutputTypeName = getKotlinType(field.type)
        val parentType = getKotlinType(container).copy(false)
        val environmentWrapperClassName = generatedMapper.getEnvironmentWrapperClassName().parameterizedBy(parentType)

        return TypeSpec.classBuilder(fieldResolverClassName)
            .addModifiers(KModifier.ABSTRACT)
            .addSuperinterface(ClassName("graphql.schema", "DataFetcher").parameterizedBy(fieldOutputTypeName))
            .addFunction(FunSpec.builder("resolve").also {
                it.modifiers.add(KModifier.ABSTRACT)

                it.addParameters(buildResolverFunArguments(field))
                it.addParameter("env", environmentWrapperClassName)

                it.returns(getKotlinType(field.type))
            }.build())
            .also { typeSpec ->
                field.arguments.forEach {
                    typeSpec.addFunction(argumentCodeBlockGenerator.buildArgumentResolverFun(it.name, "map", it.type))
                }

                typeSpec.addFunction(FunSpec.builder("get")
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
                            environmentWrapperClassName
                        )
                    }
                    .build())
            }
            .build()
    }

    private fun buildResolverFunArguments(field: GraphQLFieldDefinition): Collection<ParameterSpec> {
        return field.arguments.map { argument ->
            ParameterSpec(argument.name, getKotlinType(argument.type))
        }
    }
}
