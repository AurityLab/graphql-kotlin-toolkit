package com.auritylab.graphql.kotlin.toolkit.spring.internal

import com.auritylab.graphql.kotlin.toolkit.spring.annotation.AnnotationResolver
import com.auritylab.graphql.kotlin.toolkit.spring.provided.providedUploadScalar
import graphql.schema.DataFetcher
import graphql.schema.GraphQLScalarType
import graphql.schema.TypeResolver
import graphql.schema.idl.FieldWiringEnvironment
import graphql.schema.idl.InterfaceWiringEnvironment
import graphql.schema.idl.ScalarWiringEnvironment
import graphql.schema.idl.UnionWiringEnvironment
import graphql.schema.idl.WiringFactory
import org.springframework.context.annotation.Configuration

/**
 * Describes a [WiringFactory] which resolves types using [AnnotationResolver].
 */
@Configuration
class InternalWiringFactory(
    private val annotationResolver: AnnotationResolver
) : WiringFactory {
    private val providedScalars = mapOf(Pair("Upload", providedUploadScalar))

    override fun getDataFetcher(environment: FieldWiringEnvironment): DataFetcher<*> =
        annotationResolver.getResolver(environment)!!

    override fun getScalar(environment: ScalarWiringEnvironment): GraphQLScalarType =
        providedScalars.get(environment.scalarTypeDefinition.name)
            ?: annotationResolver.getScalar(environment)!!

    override fun getTypeResolver(environment: InterfaceWiringEnvironment): TypeResolver =
        annotationResolver.getTypeResolver(environment)!!

    override fun getTypeResolver(environment: UnionWiringEnvironment): TypeResolver =
        annotationResolver.getTypeResolver(environment)!!

    override fun providesScalar(environment: ScalarWiringEnvironment): Boolean =
        providedScalars.containsKey(environment.scalarTypeDefinition.name) ||
            annotationResolver.getScalar(environment) != null

    override fun providesTypeResolver(environment: InterfaceWiringEnvironment): Boolean =
        annotationResolver.getTypeResolver(environment) != null

    override fun providesTypeResolver(environment: UnionWiringEnvironment): Boolean =
        annotationResolver.getTypeResolver(environment) != null

    override fun providesDataFetcher(environment: FieldWiringEnvironment): Boolean =
        annotationResolver.getResolver(environment) != null
}
