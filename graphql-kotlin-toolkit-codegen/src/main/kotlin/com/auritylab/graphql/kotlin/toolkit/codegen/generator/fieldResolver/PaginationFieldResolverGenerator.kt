package com.auritylab.graphql.kotlin.toolkit.codegen.generator.fieldResolver

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.NamingHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.ImplementerMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.toolkit.common.helper.GraphQLTypeHelper
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer

internal class PaginationFieldResolverGenerator(
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
    private val resultHolderClassName: ClassName =
        ClassName(fileClassName.packageName, *fileClassName.simpleNames.toTypedArray(), "Result")

    // Fetch the wrapped type of the list.
    private val unwrappedReturnType = GraphQLTypeHelper.getListType(field.type) ?: throw IllegalStateException()
    private val unwrappedReturnKotlinType = getKotlinType(unwrappedReturnType)
    private val resolveReturnType: TypeName = getKotlinType(field.type, null, LIST)

    override fun buildFieldResolverClass(builder: TypeSpec.Builder) {
        builder
            .addType(environmentTypeSpec)
            .addType(resultHolderTypeSpec)
            .addFunction(resolveCursorFunSpec)
            .addFunction(resultFunSpec)
            .addFunction(buildEdgeFunSpec)
            .addFunction(buildConnectionFunSpec)
            .addFunction(buildPageInfoFunSpec)
            .addSuperinterface(
                ClassName(
                    "graphql.schema",
                    "DataFetcher"
                ).parameterizedBy(
                    generatedMapper.getPaginationConnectionClassName().parameterizedBy(unwrappedReturnKotlinType)
                )
            )

        builder.addFunction(
            FunSpec.builder("resolve")
                .addModifiers(KModifier.ABSTRACT)
                .addParameters(buildResolverFunArguments())
                .addParameter("env", generatedMapper.getFieldResolverEnvironment(container, field))
                .returns(resultHolderClassName).build()
        )

        builder.addFunction(FunSpec.builder("get")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("env", ClassName("graphql.schema", "DataFetchingEnvironment"))
            .returns(generatedMapper.getPaginationConnectionClassName().parameterizedBy(unwrappedReturnKotlinType))
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
                    "val result = resolve(${resolveArgs}env = %T(env, %M(map)))",
                    generatedMapper.getFieldResolverEnvironment(container, field),
                    generatedMapper.getPaginationInfoBuilderMemberName()
                )

                getFunSpec.addStatement("val edges = result.data.map { _buildEdge(it, env) }")
                getFunSpec.addStatement("val pageInfo = _buildPageInfo(result.hasNextPage, result.hasPreviousPage, edges.first().cursor, edges.last().cursor)")
                getFunSpec.addStatement("return _buildConnection(edges, pageInfo)")
            }
            .build())
    }

    /**
     * Will build a list of [ParameterSpec] for all parameters of the given [GraphQLFieldDefinition].
     */
    private fun buildResolverFunArguments(): Collection<ParameterSpec> {
        return field.arguments.map { ParameterSpec(it.name, getKotlinType(it.type, it)) }
    }

    /**
     * Will build the abstract function which can convert a given input object into a cursor, which is represented
     * as a string.
     */
    private val resolveCursorFunSpec: FunSpec =
        FunSpec.builder("resolveCursor")
            .addModifiers(KModifier.ABSTRACT)
            .addParameter("input", unwrappedReturnKotlinType)
            .addParameter("env", generatedMapper.getFieldResolverEnvironment(container, field))
            .returns(STRING)
            .build()

    private val environmentTypeSpec =
        TypeSpec.classBuilder(generatedMapper.getFieldResolverEnvironment(container, field))
            .primaryConstructor(
                // Create the primary constructor which accepts a parameter "original" of type "DataFetchingEnvironment".
                FunSpec.constructorBuilder()
                    .addParameter("original", dataFetchingEnvironmentClassName)
                    .addParameter("pagination", generatedMapper.getPaginationInfoClassName())
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
            .addProperty(
                PropertySpec.builder("pagination", generatedMapper.getPaginationInfoClassName())
                    .initializer("pagination")
                    .build()
            )
            .build()

    private val resultFunSpec = FunSpec.builder("result")
        .addModifiers()
        .addParameter("data", resolveReturnType)
        .addParameter("hasPreviousPage", BOOLEAN)
        .addParameter("hasNextPage", BOOLEAN)
        .returns(resultHolderClassName)
        .addCode(CodeBlock.of("return %T(data, hasPreviousPage, hasNextPage)", resultHolderClassName))
        .build()

    private val resultHolderTypeSpec = TypeSpec.classBuilder(resultHolderClassName)
        .addModifiers()
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("data", resolveReturnType)
                .addParameter("hasPreviousPage", BOOLEAN)
                .addParameter("hasNextPage", BOOLEAN)
                .build()
        )
        .addProperty(PropertySpec.builder("data", resolveReturnType).initializer("data").build())
        .addProperty(PropertySpec.builder("hasPreviousPage", BOOLEAN).initializer("hasPreviousPage").build())
        .addProperty(PropertySpec.builder("hasNextPage", BOOLEAN).initializer("hasNextPage").build())
        .build()

    private val buildEdgeFunSpec = FunSpec.builder("_buildEdge")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("data", unwrappedReturnKotlinType)
        .addParameter("env", generatedMapper.getFieldResolverEnvironment(container, field))
        .returns(generatedMapper.getPaginationEdgeClassName().parameterizedBy(unwrappedReturnKotlinType))
        .addCode(CodeBlock.of("return %T(data, resolveCursor(data, env))", generatedMapper.getPaginationEdgeClassName()))
        .build()

    private val buildConnectionFunSpec = FunSpec.builder("_buildConnection")
        .addModifiers(KModifier.PRIVATE)
        .addParameter(
            "edges",
            LIST.parameterizedBy(
                generatedMapper.getPaginationEdgeClassName().parameterizedBy(unwrappedReturnKotlinType)
            )
        )
        .addParameter("pageInfo", generatedMapper.getPaginationPageInfoClassName())
        .returns(generatedMapper.getPaginationConnectionClassName().parameterizedBy(unwrappedReturnKotlinType))
        .addCode(CodeBlock.of("return %T(edges, pageInfo)", generatedMapper.getPaginationConnectionClassName()))
        .build()

    private val buildPageInfoFunSpec = FunSpec.builder("_buildPageInfo")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("hasNextPage", BOOLEAN)
        .addParameter("hasPreviousPage", BOOLEAN)
        .addParameter("startCursor", STRING)
        .addParameter("endCursor", STRING)
        .returns(generatedMapper.getPaginationPageInfoClassName())
        .addStatement(
            "return %T(hasNextPage, hasPreviousPage, startCursor, endCursor)",
            generatedMapper.getPaginationPageInfoClassName()
        )
        .build()
}
