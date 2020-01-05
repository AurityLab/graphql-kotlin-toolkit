package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenInternalOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.GraphQLTypeHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLInputObjectType


/**
 * Implements a [AbstractGenerator] which will generate the source code for a [GraphQLInputObjectType].
 * It will generate the actual `data class` and a method which can parse a map to the `data class`
 */
internal class InputObjectGenerator(
        options: CodegenInternalOptions, kotlinTypeMapper: KotlinTypeMapper, private val generatedMapper: GeneratedMapper
) : AbstractGenerator(options, kotlinTypeMapper, generatedMapper) {
    companion object {
        private val MAP_STRING_ANY_TYPE = ClassName("kotlin.collections", "Map")
                .parameterizedBy(
                        ClassName("kotlin", "String"),
                        ClassName("kotlin", "Any"))
    }

    fun getInputObject(inputObject: GraphQLInputObjectType): FileSpec {
        val fieldResolverName = generatedMapper.getGeneratedTypeClassName(inputObject)

        return getFileSpecBuilder(fieldResolverName)
                .addType(buildInputObjectTypeClass(inputObject))
                .build()
    }

    /**
     * Will create the [TypeSpec] which represents the `data class` for the given [inputObject].
     */
    private fun buildInputObjectTypeClass(inputObject: GraphQLInputObjectType): TypeSpec {
        return TypeSpec.classBuilder(getGeneratedTypeClassName(inputObject))
                // Add the `DATA` modifier to make it a `data class`.
                .addModifiers(KModifier.DATA)
                // Create the primary constructor with all available parameters.
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameters(buildParameters(inputObject))
                        .build())
                .addProperties(buildProperties(inputObject))
                .addType(buildInputObjectTypeCompanionObject(inputObject))
                .build()
    }

    private fun buildInputObjectTypeCompanionObject(inputObject: GraphQLInputObjectType): TypeSpec {
        return TypeSpec.companionObjectBuilder()
                .addFunction(createBuilderFun(inputObject))
                .build()
    }

    /**
     * Will create a [ParameterSpec] for each field in the given [inputObject] to use in the primary constructor.
     */
    private fun buildParameters(inputObject: GraphQLInputObjectType): Collection<ParameterSpec> {
        return inputObject.fields.map { field ->
            val kType = getKotlinType(field.type)
                    .let { if (it.isNullable) createWrappedValue(it) else it }

            ParameterSpec(field.name, kType)
        }
    }

    /**
     * Will create a [PropertySpec] for each field in the given [inputObject] to use in the `data class`.
     * Each property will be initialized by the corresponding parameter in the primary constructor.
     */
    private fun buildProperties(inputObject: GraphQLInputObjectType): Collection<PropertySpec> {
        return inputObject.fields.map { field ->
            val kType = getKotlinType(field.type)
                    .let { if (it.isNullable) createWrappedValue(it) else it }

            PropertySpec.builder(field.name, kType)
                    .initializer(field.name)
                    .build()
        }
    }

    /**
     * Will wrap the given [type] with a nullable value wrapper.
     */
    private fun createWrappedValue(type: TypeName): TypeName {
        val wrapper = generatedMapper.getValueWrapperName()
        return wrapper.parameterizedBy(type).copy(true)
    }

    private fun createBuilderFun(inputObject: GraphQLInputObjectType): FunSpec {
        val builderMemberName = generatedMapper.getInputObjectBuilderMemberName(inputObject)
        val inputObjectClassName = generatedMapper.getGeneratedTypeClassName(inputObject)
        val valueWrapper = generatedMapper.getValueWrapperName()


        return FunSpec.builder(builderMemberName.simpleName)
                .addParameter("map", MAP_STRING_ANY_TYPE)
                .returns(inputObjectClassName)
                .also { spec ->
                    // Go through each input object field and create the according parser statement
                    inputObject.fields.forEach { field ->
                        val gType = GraphQLTypeHelper.unwrapTypeFull(field.type)
                        val kType = getKotlinType(field.type)

                        if (gType is GraphQLInputObjectType) {
                            // Type is a InputObject -> Delegate to builder fun.

                            // Fetch the input object builder.
                            val fieldTypeBuilder = generatedMapper.getInputObjectBuilderMemberName(gType)

                            if (kType.isNullable)
                                spec.addStatement(
                                        "val %LArg = if(map.containsKey(\"${field.name}\")) %T(%M(map[\"${field.name}\"] as %T)) else null",
                                        field.name, valueWrapper, fieldTypeBuilder, MAP_STRING_ANY_TYPE)
                            else
                                spec.addStatement("val %LArg = %T(%M(map[\"${field.name}\"] as %T))", valueWrapper, fieldTypeBuilder)
                        } else if (gType is GraphQLEnumType) {
                            val enum = generatedMapper.getGeneratedTypeClassName(gType)

                            if (kType.isNullable)
                                spec.addStatement(
                                        "val %LArg = if(map.containsKey(\"${field.name}\")) %T(%T.valueOf(map[\"${field.name}\"] as %T)) else null",
                                        field.name, valueWrapper, enum, STRING)
                            else
                                spec.addStatement(
                                        "val %LArg = %T.valueOf(map[\"${field.name}\"] as %T)",
                                        field.name, enum, STRING)
                        } else {
                            if (kType.isNullable)
                                spec.addStatement(
                                        "val %LArg = if(map.containsKey(\"${field.name}\")) %T(map[\"${field.name}\"] as %T) else null",
                                        field.name, valueWrapper, kType)
                            else
                                spec.addStatement(
                                        "val %LArg = map[\"${field.name}\"] as %T",
                                        field.name, kType)
                        }
                    }
                }
                .also { spec ->
                    val namedParameters = inputObject.fields.joinToString(", ") {
                        "${it.name} = ${it.name}Arg"
                    }

                    spec.addStatement("return %T($namedParameters)", inputObjectClassName)
                }
                .build()
    }
}
