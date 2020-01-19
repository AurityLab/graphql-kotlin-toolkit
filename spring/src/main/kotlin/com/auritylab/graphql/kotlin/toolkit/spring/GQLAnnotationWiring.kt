package com.auritylab.graphql.kotlin.toolkit.spring

import com.auritylab.graphql.kotlin.toolkit.spring.annotation.GQLDirective
import com.auritylab.graphql.kotlin.toolkit.spring.annotation.GQLResolver
import com.auritylab.graphql.kotlin.toolkit.spring.annotation.GQLResolvers
import com.auritylab.graphql.kotlin.toolkit.spring.annotation.GQLScalar
import com.auritylab.graphql.kotlin.toolkit.spring.annotation.GQLTypeResolver
import graphql.schema.Coercing
import graphql.schema.DataFetcher
import graphql.schema.GraphQLScalarType
import graphql.schema.TypeResolver
import graphql.schema.idl.FieldWiringEnvironment
import graphql.schema.idl.InterfaceWiringEnvironment
import graphql.schema.idl.ScalarWiringEnvironment
import graphql.schema.idl.SchemaDirectiveWiring
import graphql.schema.idl.SchemaDirectiveWiringEnvironment
import graphql.schema.idl.UnionWiringEnvironment
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component

/**
 * Will fetch all annotated GraphQL components.
 * Including [GQLResolvers], [GQLResolver], [GQLTypeResolver], [GQLDirective] and [GQLScalar].
 */
@Component
class GQLAnnotationWiring(
    private val context: ApplicationContext
) {
    val resolvers = fetchDataFetcherComponents()
    val directives = fetchDirectiveComponents()
    val scalars = fetchScalarComponents()
    val interfaceTypeResolvers: Map<GQLTypeResolver, TypeResolver>
    val unionTypeResolvers: Map<GQLTypeResolver, TypeResolver>

    init {
        val typeResolvers = fetchTypeResolverComponents()

        interfaceTypeResolvers = typeResolvers.filter { it.key.scope == GQLTypeResolver.Scope.INTERFACE }
        unionTypeResolvers = typeResolvers.filter { it.key.scope == GQLTypeResolver.Scope.UNION }
    }

    fun getResolver(env: FieldWiringEnvironment): DataFetcher<*>? =
        resolvers.filter {
            env.parentType.name == it.key.container &&
                env.fieldDefinition.name == it.key.field
        }.values.firstOrNull()

    fun getDirective(env: SchemaDirectiveWiringEnvironment<*>): SchemaDirectiveWiring? =
        directives.filter {
            if (env.directive != null)
                env.directive.name == it.key.directive
            false
        }.values.firstOrNull()

    fun getTypeResolver(env: InterfaceWiringEnvironment): TypeResolver? =
        interfaceTypeResolvers.filter {
            env.interfaceTypeDefinition.name == it.key.type
        }.values.firstOrNull()

    fun getTypeResolver(env: UnionWiringEnvironment): TypeResolver? =
        unionTypeResolvers.filter {
            env.unionTypeDefinition.name == it.key.type
        }.values.firstOrNull()

    fun getScalar(env: ScalarWiringEnvironment): GraphQLScalarType? =
        scalars.filter {
            env.scalarTypeDefinition.name == it.key.name
        }.values.firstOrNull()

    /**
     * Will fetch all components which are marked with [GQLResolver] or [GQLResolvers].
     * The components will then be mapped with the [DataFetcher] instance and the according annotation.
     */
    private fun fetchDataFetcherComponents(): Map<GQLResolver, DataFetcher<*>> {
        val singleComponents = context.getBeansWithAnnotation(GQLResolver::class.java).values
        val multiComponents = context.getBeansWithAnnotation(GQLResolvers::class.java).values

        // Map the single components.
        val flat = mapComponents<GQLResolver, DataFetcher<*>>(singleComponents)

        // Map the multi components.
        val multi = mapComponents<GQLResolvers, DataFetcher<*>>(multiComponents)
            .flatMap { map -> map.key.resolvers.map { Pair(it, map.value) } }
            .associate { it }

        // Join the maps.
        return flat.plus(multi)
    }

    /**
     * Will fetch all components which are marked with [GQLDirective].
     * The components will be then be mapped with the [SchemaDirectiveWiring] instance and the according annotation.
     */
    private fun fetchDirectiveComponents(): Map<GQLDirective, SchemaDirectiveWiring> =
        mapComponents(context.getBeansWithAnnotation(GQLDirective::class.java).values)

    /**
     * Will fetch all components which are marked with [GQLTypeResolver].
     * The components will be then mapped with the [TypeResolver] instance and the according instance.
     */
    private fun fetchTypeResolverComponents(): Map<GQLTypeResolver, TypeResolver> =
        mapComponents(context.getBeansWithAnnotation(GQLTypeResolver::class.java).values)

    /**
     * Will fetch all components which are marked with [GQLScalar]
     */
    private fun fetchScalarComponents(): Map<GQLScalar, GraphQLScalarType> {
        val components = context.getBeansWithAnnotation(GQLScalar::class.java).values

        return mapComponents<GQLScalar, Coercing<*, *>>(components)
            // Map the value to an actual GraphQLScalarType instance.
            .map { Pair(it.key, buildGraphQLScalarType(it.key.name, it.value)) }
            .associate { it }
    }

    /**
     * Will build a new [GraphQLScalarType] instance with the given [name] and [coercing].
     */
    private fun buildGraphQLScalarType(name: String, coercing: Coercing<*, *>): GraphQLScalarType =
        GraphQLScalarType.newScalar().name(name).coercing(coercing).build()

    /**
     * Will check if the component is [C] and map it to the correct type.
     * In additional it will associate the annotation instance.
     *
     * @param components Collection of all components.
     * @return Map of all valid components mapped with the annotation as key.
     */
    private inline fun <reified A : Annotation, reified C : Any> mapComponents(components: Collection<Any?>): Map<A, C> {
        return components
            // Filter with type check
            .filterIsInstance<C>()
            // Associate with the annotation (It assumes there is a annotation of that type).
            .associateBy {
                AnnotationUtils.findAnnotation(it::class.java, A::class.java)!!
            }
    }
}
