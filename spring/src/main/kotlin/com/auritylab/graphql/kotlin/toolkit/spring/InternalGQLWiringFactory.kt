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
import org.springframework.stereotype.Component

@Component
@ConditionalOnMissingBean(WiringFactory::class)
internal class InternalGQLWiringFactory(
    private val annotationWiring: GQLAnnotationWiring
) : WiringFactory {
    override fun getDataFetcher(environment: FieldWiringEnvironment): DataFetcher<*> =
        annotationWiring.getResolver(environment)!!

    override fun getScalar(environment: ScalarWiringEnvironment): GraphQLScalarType =
        annotationWiring.getScalar(environment)!!

    override fun getTypeResolver(environment: InterfaceWiringEnvironment): TypeResolver =
        annotationWiring.getTypeResolver(environment)!!

    override fun getTypeResolver(environment: UnionWiringEnvironment): TypeResolver =
        annotationWiring.getTypeResolver(environment)!!

    override fun providesScalar(environment: ScalarWiringEnvironment): Boolean =
        annotationWiring.getScalar(environment) != null

    override fun providesTypeResolver(environment: InterfaceWiringEnvironment): Boolean =
        annotationWiring.getTypeResolver(environment) != null

    override fun providesTypeResolver(environment: UnionWiringEnvironment): Boolean =
        annotationWiring.getTypeResolver(environment) != null

    override fun providesDataFetcher(environment: FieldWiringEnvironment): Boolean =
        annotationWiring.getResolver(environment) != null
}
