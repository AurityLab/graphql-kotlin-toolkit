package com.auritylab.graphql.kotlin.codegen.generator

import com.auritylab.graphql.kotlin.codegen.CodegenInternalOptions
import com.auritylab.graphql.kotlin.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLType

internal abstract class AbstractGenerator(
        private val options: CodegenInternalOptions,
        private val kotlinTypeMapper: KotlinTypeMapper,
        private val generatedMapper: GeneratedMapper
) {
    protected fun getFileSpecBuilder(className: ClassName): FileSpec.Builder {
        return FileSpec.builder(className.packageName, className.simpleName)
    }

    protected fun getKotlinType(type: GraphQLType): TypeName {
        return kotlinTypeMapper.getKotlinType(type)
    }

    protected fun getGeneratedTypeClassName(type: GraphQLType): ClassName {
        return generatedMapper.getGeneratedTypeClassName(type)
    }

    protected fun getGeneratedFieldResolverClassName(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): ClassName {
        return generatedMapper.getGeneratedFieldResolverClassName(container, field)
    }
}
