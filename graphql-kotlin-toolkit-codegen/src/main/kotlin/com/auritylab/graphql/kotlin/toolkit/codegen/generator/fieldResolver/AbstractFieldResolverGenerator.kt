package com.auritylab.graphql.kotlin.toolkit.codegen.generator.fieldResolver

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.AbstractClassGenerator
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
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLInterfaceType
import graphql.schema.GraphQLObjectType

internal abstract class AbstractFieldResolverGenerator(
    protected val container: GraphQLFieldsContainer,
    protected val field: GraphQLFieldDefinition,
    protected val implementerMapper: ImplementerMapper,
    protected val argumentCodeBlockGenerator: ArgumentCodeBlockGenerator,
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper
) : AbstractClassGenerator(options, kotlinTypeMapper, generatedMapper) {
    /**
     * Describes the [ClassName] for the actual class which describes the field resolver.
     * This will be used on the [TypeSpec] of [buildFieldResolverClass].
     */
    override val fileClassName: ClassName = generatedMapper.getGeneratedFieldResolverClassName(container, field)

    /**
     * Describes the [TypeName] for the return type of the field.
     */
    protected val fieldTypeName = getKotlinType(field.type)

    /**
     * Describes the actual return [TypeName] which will be returned by the "graphql.schema.DataFetcher", which will
     * be implemented, by this field resolver interface. This can be overridden by subclasses to adjust the
     */
    protected open val returnTypeName: TypeName = fieldTypeName

    protected val dataFetchingEnvironmentClassName = ClassName("graphql.schema", "DataFetchingEnvironment")

    /**
     * Describes the "graphql.schema.DataFetcher" [TypeName] which is parameterized by the [returnTypeName].
     */
    protected val parameterizedDataFetcherTypeName: TypeName
        get() = ClassName("graphql.schema", "DataFetcher").parameterizedBy(returnTypeName)

    protected val contextClassName = options.globalContext?.let { ClassName.bestGuess(it) } ?: ANY

    override fun build(builder: FileSpec.Builder) {
        val typeBuilder = TypeSpec.interfaceBuilder(fileClassName)

        typeBuilder.addSuperinterface(parameterizedDataFetcherTypeName)

        // Call the build methods with the type builder.
        buildFieldResolverClass(typeBuilder)

        // Add the resolver annotation if the spring boot integration is enabled.
        if (options.enableSpringBootIntegration)
            typeBuilder.addAnnotation(springBootIntegrationAnnotationSpec)

        // Add the common functions and types.
        typeBuilder.addFunctions(argumentResolverFunSpecs)
        typeBuilder.addType(metaTypeSpec)

        builder.addType(typeBuilder.build())
    }

    /**
     * Will continue the building of the resolver. Some types, functions, etc. have already been.
     * This shall only be used for additionally required types, functions, etc.
     */
    protected abstract fun buildFieldResolverClass(builder: TypeSpec.Builder)

    /**
     * Will build the [TypeName] which for the parent type for the resolver. If the [container] is just a
     * [GraphQLObjectType] it will resolve it to the according Kotlin type. If it's a [GraphQLInterfaceType] it will
     * fetch all implementers of that interface and check if all of them are represented with the same type.
     * If they are represented with the same type the according Kotlin type will be returned, if not simply Any will
     * returned.
     */
    protected val parentTypeName: TypeName =
        if (container is GraphQLObjectType) {
            // Simply resolve the container to the according Kotlin type.
            getKotlinType(container)
        } else if (container is GraphQLInterfaceType) {
            // Fetch the implementers of the interface and map them to the Kotlin type.
            val implementersTypes = implementerMapper
                .getImplementers(container)
                .map { getKotlinType(it) }

            // Fetch the first type to have a reference type to check against.
            val firstType = implementersTypes.first()

            // If all implementers have the same type as the first, the reference type can be returned.
            if (implementersTypes.all { it == firstType }) firstType
            else ANY
        } else throw UnsupportedOperationException()

    /**
     * Will build the [AnnotationSpec] for the Spring Boot integration support. If the given [container] is a
     * [GraphQLObjectType] it will return a GQLResolver annotation which points to a single field definition. If it is
     * a [GraphQLInterfaceType] it will return a GQLResolvers annotation which points to all all implementors
     * field definitions.
     */
    private val springBootIntegrationAnnotationSpec: AnnotationSpec =
        when (container) {
            is GraphQLObjectType -> SpringBootIntegrationHelper.createResolverAnnotation(
                generatedMapper.getFieldResolverContainerMemberName(container, field),
                generatedMapper.getFieldResolverFieldMemberName(container, field)
            )
            is GraphQLInterfaceType -> {
                // Search for the implementors of the interface and map it to the according field definitions.
                val implementersMapping = implementerMapper
                    .getImplementers(container)
                    .map {
                        val fieldDefinition = it.getFieldDefinition(field.name)
                        Pair(it.name, fieldDefinition.name)
                    }

                SpringBootIntegrationHelper.createMultiResolverAnnotation(implementersMapping)
            }
            else -> throw UnsupportedOperationException()
        }

    /**
     * Will build the [TypeSpec] for the meta companion object. It contains the name of the CONTAINER and the FIELD.
     * The meta type is required for the spring boot annotation, which relies on the values of the meta type.
     * As the properties of the meta type are constant they're open to use for everything.
     */
    private val metaTypeSpec: TypeSpec =
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

    /**
     * Will build the [FunSpec] for all arguments on the current [field]. The returned value also holds a [MemberName]
     * which represents the resolve Method for each argument.
     */
    private val argumentResolverFunSpecs: Collection<FunSpec> = field.arguments
        .map { argumentCodeBlockGenerator.buildResolver(it) }
}
