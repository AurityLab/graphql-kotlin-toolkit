package com.auritylab.graphql.kotlin.codegen.generator

import com.auritylab.graphql.kotlin.codegen.PoetOptions
import com.auritylab.graphql.kotlin.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.codegen.mapper.NameMapper
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLInputObjectType

class FieldResolverGenerator(
        options: PoetOptions, kotlinTypeMapper: KotlinTypeMapper, private val nameMapper: NameMapper
) : AbstractGenerator(options, kotlinTypeMapper, nameMapper) {
    val inputMapType = ClassName("kotlin.collections", "Map")
            .parameterizedBy(
                    ClassName("kotlin", "String"),
                    ClassName("kotlin", "Any"))

    fun getFieldResolver(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): FileSpec {
        val fieldResolverName = nameMapper.getFieldResolverName(container, field)

        return getFileSpecBuilder(fieldResolverName.className)
                .addType(buildFieldResolverClass(container, field)).build()
    }

    private fun buildFieldResolverClass(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): TypeSpec {
        val fieldResolverClassName = getFieldResolverName(container, field)
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

                                getFunSpec.addStatement("return resolve(${field.arguments.joinToString(", ") { it.name }}, env)")
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
        val argType = argument.type

        return if (argType is GraphQLInputObjectType) {
            val inputObjectParser = nameMapper.getInputObjectParser(argType)

            Statement(
                    "val ${argName}Arg = if (env.containsArgument(\"$argName\")) %M(env.getArgument<%T>(\"$argName\")) else null",
                    listOf(inputObjectParser, inputMapType))
        } else {
            Statement(
                    "val ${argName}Arg = if (env.containsArgument(\"$argName\")) env.getArgument<%T>(\"$argName\") else null",
                    listOf(getKotlinType(argument.type)))
        }
    }

    data class Statement(val statement: String, val args: Collection<Any>)
}
