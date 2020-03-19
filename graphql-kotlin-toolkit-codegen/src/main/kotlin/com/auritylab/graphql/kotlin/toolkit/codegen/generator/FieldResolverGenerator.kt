package com.auritylab.graphql.kotlin.toolkit.codegen.generator

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
    override fun buildFieldResolverClass(): TypeSpec {
        val fieldResolverClassName = generatedMapper.getGeneratedFieldResolverClassName(container, field)

        return TypeSpec.classBuilder(fieldResolverClassName)
            .addModifiers(KModifier.ABSTRACT)
            .addSuperinterface(dataFetcherClassName)
            .addFunction(
                FunSpec.builder("resolve")
                    .addModifiers(KModifier.ABSTRACT)
                    .addParameters(buildResolverFunArguments())
                    .addParameter("env", generatedMapper.getFieldResolverEnvironment(container, field))
                    .returns(fieldKotlinType).build()
            )
            .addType(metaTypeSpec)
            .addType(buildEnvironmentType())
            .addFunctions(argumentResolverFunSpecs)
            .also { typeSpec ->
                // Add the resolver annotation if the spring boot integration is enabled.
                if (options.enableSpringBootIntegration)
                    typeSpec.addAnnotation(springBootIntegrationAnnotationSpec)

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
                            generatedMapper.getFieldResolverEnvironment(container, field)
                        )
                    }
                    .build())
            }
            .build()
    }

    /**
     * Will build a list of [ParameterSpec] for all parameters of the given [GraphQLFieldDefinition].
     */
    private fun buildResolverFunArguments(): Collection<ParameterSpec> {
        return field.arguments.map { ParameterSpec(it.name, getKotlinType(it.type, it)) }
    }

    private fun buildEnvironmentType(): TypeSpec {
        val parentType = parentTypeName.copy(false)

        return TypeSpec.classBuilder(generatedMapper.getFieldResolverEnvironment(container, field)).also { builder ->
            builder.primaryConstructor(
                // Create the primary constructor which accepts a parameter "original" of type "DataFetchingEnvironment".
                FunSpec.constructorBuilder().also { constructorBuilder ->
                        constructorBuilder
                            .addParameter(
                                ParameterSpec
                                    .builder("original", dataFetchingEnvironmentClassName)
                                    .build()
                            )
                    }
                    .build()
            )

            builder.addProperty(
                PropertySpec
                    .builder("original", dataFetchingEnvironmentClassName)
                    .initializer("original")
                    .build()
            )
            builder.addProperty(
                PropertySpec
                    .builder("parent", parentType)
                    .getter(FunSpec.getterBuilder().addCode("return original.getSource()").build())
                    .build()
            )
            builder.addProperty(
                PropertySpec
                    .builder("context", contextClassName)
                    .getter(FunSpec.getterBuilder().addCode("return original.getContext()").build())
                    .build()
            )
        }.build()
    }
}
