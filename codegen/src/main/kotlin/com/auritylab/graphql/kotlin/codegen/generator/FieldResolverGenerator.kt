package com.auritylab.graphql.kotlin.codegen.generator

import com.auritylab.graphql.kotlin.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.codegen.helper.GraphQLTypeHelper
import com.auritylab.graphql.kotlin.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.codegen.mapper.GeneratedMapper
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLInputObjectType

class FieldResolverGenerator(
        options: CodegenOptions, kotlinTypeMapper: KotlinTypeMapper, private val generatedMapper: GeneratedMapper
) : AbstractGenerator(options, kotlinTypeMapper, generatedMapper) {
    val inputMapType = ClassName("kotlin.collections", "Map")
            .parameterizedBy(
                    ClassName("kotlin", "String"),
                    ClassName("kotlin", "Any"))

    fun getFieldResolver(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): FileSpec {
        val fieldResolverClassName = generatedMapper.getGeneratedFieldResolverClassName(container, field)

        return getFileSpecBuilder(fieldResolverClassName)
                .addType(buildFieldResolverClass(container, field)).build()
    }

    private fun buildFieldResolverClass(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): TypeSpec {
        val fieldResolverClassName = generatedMapper.getGeneratedFieldResolverClassName(container, field)
        val fieldOutputTypeName = getKotlinType(field.type)

        return TypeSpec.classBuilder(fieldResolverClassName)
                .addModifiers(KModifier.ABSTRACT)
                .addSuperinterface(ClassName("graphql.schema", "DataFetcher").parameterizedBy(fieldOutputTypeName))
                .addFunction(FunSpec.builder("resolve").also {
                    it.modifiers.add(KModifier.ABSTRACT)

                    it.addParameters(buildResolverFunArguments(field))
                    it.addParameter("env", ClassName("graphql.schema", "DataFetchingEnvironment"))

                    it.returns(getKotlinType(field.type))
                }.build())
                .also { typeSpec ->
                    typeSpec.addFunction(FunSpec.builder("get")
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameter("env", ClassName("graphql.schema", "DataFetchingEnvironment"))
                            .returns(getKotlinType(field.type))
                            .also { getFunSpec ->
                                field.arguments.forEach { arg ->
                                    val stmt = createArgumentParserStatement(arg)
                                    getFunSpec.addStatement(stmt.statement, *stmt.args.toTypedArray())
                                }

                                val resolveArgs = field.arguments.let { args ->
                                    if (args.isEmpty())
                                       return@let ""

                                    return@let args.joinToString(", ") { it.name + "Arg" } + ", "
                                }

                                getFunSpec.addStatement("return resolve(${resolveArgs}env)")
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

    private fun createArgumentParserStatement(argument: GraphQLArgument): Statement {
        val argName = argument.name
        val argType = GraphQLTypeHelper.unwrapTypeFull(argument.type)
        val kType = getKotlinType(argument.type)

        return if (argType is GraphQLInputObjectType) {
            val inputObjectParser = generatedMapper.getInputObjectBuilderMemberName(argType)

            if (kType.isNullable)
                Statement(
                        "val ${argName}Arg = if (env.containsArgument(\"$argName\")) %M(env.getArgument<%T>(\"$argName\")) else null",
                        listOf(inputObjectParser, inputMapType))
            else
                Statement(
                        "val ${argName}Arg = %M(env.getArgument<%T>(\"$argName\"))",
                        listOf(inputObjectParser, inputMapType))
        } else {
            if (kType.isNullable)
                Statement(
                        "val ${argName}Arg = if (env.containsArgument(\"$argName\")) env.getArgument<%T>(\"$argName\") else null",
                        listOf(getKotlinType(argument.type)))
            else
                Statement(
                        "val ${argName}Arg = env.getArgument<%T>(\"$argName\")",
                        listOf(getKotlinType(argument.type)))
        }
    }

    data class Statement(val statement: String, val args: Collection<Any>)
}
