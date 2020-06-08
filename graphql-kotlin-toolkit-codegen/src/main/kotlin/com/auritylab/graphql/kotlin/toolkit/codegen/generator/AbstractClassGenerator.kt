package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import graphql.schema.GraphQLDirectiveContainer
import graphql.schema.GraphQLNamedType
import graphql.schema.GraphQLType

internal abstract class AbstractClassGenerator(
    protected val options: CodegenOptions,
    protected val kotlinTypeMapper: KotlinTypeMapper,
    protected val generatedMapper: GeneratedMapper
) : FileGenerator {
    /**
     * Defines the [ClassName] which is used to defined the [FileSpec.packageName] and the [FileSpec.name]
     * for the generated [FileSpec].
     */
    protected abstract val fileClassName: ClassName

    /**
     * Will configure the [FileSpec.Builder] with the required types, etc.
     */
    protected abstract fun build(builder: FileSpec.Builder)

    override fun generate(): FileSpec {
        // Create the FileSpec builder using the fileClassName.
        val builder = FileSpec.builder(fileClassName.packageName, fileClassName.simpleName)

        // Use the builder method to configure the file.
        build(builder)

        // Build the file.
        return builder.build()
    }

    /**
     * Will build the corresponding [TypeName] for the given [GraphQLType].
     * A [fieldDirectiveContainer] can be given additional to determine if a DoubleNull type is required.
     */
    protected fun getKotlinType(
        type: GraphQLType,
        fieldDirectiveContainer: GraphQLDirectiveContainer? = null,
        listType: ClassName? = null
    ): TypeName =
        kotlinTypeMapper.getKotlinType(type, fieldDirectiveContainer, listType)

    /**
     * Will build the corresponding [ClassName] for the given [GraphQLType].
     */
    protected fun getGeneratedType(type: GraphQLNamedType): ClassName =
        generatedMapper.getGeneratedTypeClassName(type)
}
