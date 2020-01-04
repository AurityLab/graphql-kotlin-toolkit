package com.auritylab.graphql.kotlin.codegen.generator

import com.auritylab.graphql.kotlin.codegen.PoetOptions
import com.auritylab.graphql.kotlin.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.codegen.mapper.NameMapper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLType

abstract class AbstractGenerator(
        private val options: PoetOptions,
        private val kotlinTypeMapper: KotlinTypeMapper,
        private val nameMapper: NameMapper
) {
    protected fun getFileSpecBuilder(fileName: String): FileSpec.Builder {
        return FileSpec.builder(options.generatedFilesPackage, buildFileName(fileName))
    }

    protected fun getKotlinType(type: GraphQLType): TypeName {
        return kotlinTypeMapper.getKotlinType(type)
    }

    protected fun getTypeName(type: GraphQLType): ClassName {
        val typeName = nameMapper.getTypeName(type)

        return ClassName(typeName.packageName, typeName.className)
    }

    protected fun getFieldResolverName(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): ClassName {
        val typeName = nameMapper.getFieldResolverName(container, field)

        return ClassName(typeName.packageName, typeName.className)
    }

    /**
     * Will uppercase the first letter of the given [string].
     * If the given [string] is `getUser` this method will return `GetUser`.
     */
    protected fun uppercaseFirstLetter(string: String): String =
            (string.substring(0, 1).toUpperCase()) + string.substring(1)


    private fun buildFileName(baseFileName: String): String {
        return if (options.generatedFilesPrefix == null)
            baseFileName
        else
            options.generatedFilesPrefix + baseFileName
    }
}
