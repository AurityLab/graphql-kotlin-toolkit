package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.NamingHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.SpringBootIntegrationHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.ImplementerMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLInterfaceType
import graphql.schema.GraphQLObjectType

internal class FieldResolverGenerator(
    private val container: GraphQLFieldsContainer,
    private val field: GraphQLFieldDefinition,
    private val implementerMapper: ImplementerMapper,
    private val argumentCodeBlockGenerator: ArgumentCodeBlockGenerator,
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper
) : AbstractClassGenerator(options, kotlinTypeMapper, generatedMapper) {
    companion object {
        private val dataFetchingEnvironmentClassName = ClassName("graphql.schema", "DataFetchingEnvironment")
    }

    override val fileClassName: ClassName = generatedMapper.getGeneratedFieldResolverClassName(container, field)

    override fun build(builder: FileSpec.Builder) {
        builder.addType(buildFieldResolverClass(container, field))
    }

    private fun buildFieldResolverClass(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): TypeSpec {
        val fieldResolverClassName = generatedMapper.getGeneratedFieldResolverClassName(container, field)
        val fieldOutputTypeName = getKotlinType(field.type)
        val parentType = getParentType(container).copy(false)
        val environmentWrapperClassName = generatedMapper.getEnvironmentWrapperClassName().parameterizedBy(parentType)

        return TypeSpec.classBuilder(fieldResolverClassName)
            .addModifiers(KModifier.ABSTRACT)
            .addSuperinterface(ClassName("graphql.schema", "DataFetcher").parameterizedBy(fieldOutputTypeName))
            .addFunction(
                FunSpec.builder("resolve")
                    .addModifiers(KModifier.ABSTRACT)
                    .addParameters(buildResolverFunArguments(field))
                    .addParameter("env", generatedMapper.getFieldResolverEnvironment(container, field))
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
            .addType(buildEnvironmentType())
            .also { typeSpec ->
                // Add the resolver annotation if the spring boot integration is enabled.
                if (options.enableSpringBootIntegration)
                    typeSpec.addAnnotation(buildSpringBootIntegrationAnnotation(container, field))

                field.arguments.forEach {
                    typeSpec.addFunction(argumentCodeBlockGenerator.buildArgumentResolverFun(it.name, it.type, it))
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
    private fun buildResolverFunArguments(field: GraphQLFieldDefinition): Collection<ParameterSpec> =
        field.arguments
            .map { argument -> ParameterSpec(argument.name, getKotlinType(argument.type, argument)) }

    /**
     * Will build the [AnnotationSpec] for the Spring Boot integration support. If the given [container] is a
     * [GraphQLObjectType] it will return a GQLResolver annotation which points to a single field definition. If it is
     * a [GraphQLInterfaceType] it will return a GQLResolvers annotation which points to all all implementors
     * field definitions.
     */
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
            val implementersMapping = implementerMapper
                .getImplementers(container)
                .map {
                    val fieldDefinition = it.getFieldDefinition(field.name)
                    Pair(it.name, fieldDefinition.name)
                }

            return SpringBootIntegrationHelper.createMultiResolverAnnotation(implementersMapping)
        }

        throw UnsupportedOperationException()
    }

    /**
     * Will build the [TypeName] which for the parent type for the resolver. If the [container] is just a
     * [GraphQLObjectType] it will resolve it to the according Kotlin type. If it's a [GraphQLInterfaceType] it will
     * fetch all implementers of that interface and check if all of them are represented with the same type.
     * If they are represented with the same type the according Kotlin type will be returned, if not simply Any will
     * returned.
     */
    private fun getParentType(container: GraphQLFieldsContainer): TypeName {
        if (container is GraphQLObjectType) {
            // Simply resolve the container to the according Kotlin type.
            return getKotlinType(container)
        } else if (container is GraphQLInterfaceType) {
            // Fetch the implementers of the interface and map them to the Kotlin type.
            val implementersTypes = implementerMapper
                .getImplementers(container)
                .map { getKotlinType(it) }

            val firstType = implementersTypes.first()
            return if (implementersTypes.all { it == firstType })
                firstType
            else
                ANY
        }

        throw UnsupportedOperationException()
    }

    private fun buildEnvironmentType(): TypeSpec {
        val parentType = getParentType(container).copy(false)
        val contextType = options.globalContext?.let { ClassName.bestGuess(it) } ?: ANY

        return TypeSpec.classBuilder(generatedMapper.getFieldResolverEnvironment(container, field))
            .primaryConstructor(
                // Create the primary constructor which accepts a parameter "original" of type "DataFetchingEnvironment".
                FunSpec.constructorBuilder()
                    .addParameter(ParameterSpec.builder("original", dataFetchingEnvironmentClassName).build())
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder("original", dataFetchingEnvironmentClassName)
                    .initializer("original")
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder("parent", parentType)
                    .getter(FunSpec.getterBuilder().addCode("return original.getSource()").build())
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder("context", contextType)
                    .getter(FunSpec.getterBuilder().addCode("return original.getContext()").build())
                    .build()
            )
            .build()
    }
}
