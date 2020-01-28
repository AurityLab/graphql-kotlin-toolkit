package com.auritylab.graphql.kotlin.toolkit.spring

import graphql.schema.DataFetcher
import graphql.schema.GraphQLScalarType
import graphql.schema.TypeResolver
import graphql.schema.idl.FieldWiringEnvironment
import graphql.schema.idl.InterfaceWiringEnvironment
import graphql.schema.idl.ScalarWiringEnvironment
import graphql.schema.idl.UnionWiringEnvironment
import graphql.schema.idl.WiringFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
@ConditionalOnMissingBean(WiringFactory::class)
class GQLWiringFactory(
    private val annotationResolver: GQLAnnotationResolver
) : WiringFactory {
    override fun getDataFetcher(environment: FieldWiringEnvironment): DataFetcher<*> =
        annotationResolver.getResolver(environment)!!

    override fun getScalar(environment: ScalarWiringEnvironment): GraphQLScalarType =
        annotationResolver.getScalar(environment)!!

    override fun getTypeResolver(environment: InterfaceWiringEnvironment): TypeResolver =
        annotationResolver.getTypeResolver(environment)!!

    override fun getTypeResolver(environment: UnionWiringEnvironment): TypeResolver =
        annotationResolver.getTypeResolver(environment)!!

    override fun providesScalar(environment: ScalarWiringEnvironment): Boolean =
        annotationResolver.getScalar(environment) != null

    override fun providesTypeResolver(environment: InterfaceWiringEnvironment): Boolean =
        annotationResolver.getTypeResolver(environment) != null

    override fun providesTypeResolver(environment: UnionWiringEnvironment): Boolean =
        annotationResolver.getTypeResolver(environment) != null

    override fun providesDataFetcher(environment: FieldWiringEnvironment): Boolean =
        annotationResolver.getResolver(environment) != null
}
