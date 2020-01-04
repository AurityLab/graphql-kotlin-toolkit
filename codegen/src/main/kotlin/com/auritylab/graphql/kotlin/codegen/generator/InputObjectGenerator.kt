package com.auritylab.graphql.kotlin.codegen.generator

import com.auritylab.graphql.kotlin.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.codegen.mapper.NameMapper
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import graphql.schema.GraphQLInputObjectType


class InputObjectGenerator(
        options: CodegenOptions, kotlinTypeMapper: KotlinTypeMapper, private val nameMapper: NameMapper
) : AbstractGenerator(options, kotlinTypeMapper, nameMapper) {
    fun getInputObject(inputObject: GraphQLInputObjectType): FileSpec {
        val fieldResolverName = nameMapper.getTypeName(inputObject)

        return getFileSpecBuilder(fieldResolverName.className)
                .addType(buildInputObjectTypeClass(inputObject))
                .build()
    }

    private fun buildInputObjectTypeClass(inputObject: GraphQLInputObjectType): TypeSpec {
        return TypeSpec.classBuilder(getTypeName(inputObject))
                .addModifiers(KModifier.DATA)
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameters(buildParameters(inputObject))
                        .build())
                .build()
    }

    private fun buildParameters(inputObject: GraphQLInputObjectType): Collection<ParameterSpec> {
        return inputObject.fields.map { field ->
            val kType = getKotlinType(field.type)
                    .let { if (it.isNullable) createWrappedValue(it) else it }

            ParameterSpec(field.name, kType)
        }
    }

    private fun createWrappedValue(type: TypeName): TypeName {
        val wrapper = nameMapper.getValueWrapperName()
        return ClassName(wrapper.packageName, wrapper.className).parameterizedBy(type).copy(true)
    }
}
