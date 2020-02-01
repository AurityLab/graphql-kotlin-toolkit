package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.NamingHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.SpringBootIntegrationHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.ImplementerMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLInterfaceType
import graphql.schema.GraphQLObjectType

internal class FieldResolverGenerator(
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    private val implementorMapper: ImplementerMapper,
    private val generatedMapper: GeneratedMapper,
    private val argumentCodeBlockGenerator: ArgumentCodeBlockGenerator
) : AbstractGenerator(options, kotlinTypeMapper, generatedMapper) {
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
            .addFunction(
                FunSpec.builder("resolve")
                    .addModifiers(KModifier.ABSTRACT)
                    .addParameters(buildResolverFunArguments(field))
                    .addParameter("env", environmentWrapperClassName)
                    .returns(getKotlinType(field.type)).build()
            )
            .addType(
                TypeSpec.companionObjectBuilder("Meta")
                    .addProperty(
                        PropertySpec.builder("CONTAINER", STRING, KModifier.CONST)
                            .initializer("\"${container.name}\"")
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("FIELD", STRING, KModifier.CONST)
                            .initializer("\"${field.name}\"")
                            .build()
                    )
                    .build()
            )
            .also { typeSpec ->
                // Add the resolver annotation if the spring boot integration is enabled.
                if (options.enableSpringBootIntegration)
                    typeSpec.addAnnotation(buildSpringBootIntegrationAnnotation(container, field))

                field.arguments.forEach {
                    typeSpec.addFunction(argumentCodeBlockGenerator.buildArgumentResolverFun(it.name, it.type))
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

    private fun buildSpringBootIntegrationAnnotation(
        container: GraphQLFieldsContainer,
        field: GraphQLFieldDefinition
    ): AnnotationSpec {
        if (container is GraphQLObjectType) {
            return SpringBootIntegrationHelper.createResolverAnnotation(
                generatedMapper.getFieldResolverContainerMemberName(container, field),
                generatedMapper.getFieldResolverFieldMemberName(container, field)
            )
        } else if (container is GraphQLInterfaceType) {
            // Search for the implementors of the interface and map it to the according field definitions.
            val implementersMapping = implementorMapper
                .getImplementers(container)
                .map {
                    val fieldDefinition = it.getFieldDefinition(field.name)
                    Pair(it.name, fieldDefinition.name)
                }

            return SpringBootIntegrationHelper.createMultiResolverAnnotation(implementersMapping)
        }

        throw UnsupportedOperationException()
    }
}
