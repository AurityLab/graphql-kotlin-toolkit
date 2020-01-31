package com.auritylab.graphql.kotlin.toolkit.spring

import com.auritylab.graphql.kotlin.toolkit.spring.annotation.Directive
import com.auritylab.graphql.kotlin.toolkit.spring.annotation.Resolver
import com.auritylab.graphql.kotlin.toolkit.spring.annotation.GQLResolvers
import com.auritylab.graphql.kotlin.toolkit.spring.annotation.Scalar
import graphql.schema.Coercing
import graphql.schema.DataFetcher
import graphql.schema.GraphQLScalarType
import graphql.schema.idl.FieldWiringEnvironment
import graphql.schema.idl.InterfaceWiringEnvironment
import graphql.schema.idl.ScalarWiringEnvironment
import graphql.schema.idl.SchemaDirectiveWiring
import graphql.schema.idl.UnionWiringEnvironment
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component

/**
 * Will fetch all annotated GraphQL components.
 * Including [GQLResolvers], [Resolver], [TypeResolver], [Directive] and [Scalar].
 */
@Component
class AnnotationResolver(
    private val context: ApplicationContext
) {
    private final val resolvers = fetchDataFetcherComponents()
    final val directives = fetchDirectiveComponents()
    private final val scalars = fetchScalarComponents()
    private val typeResolvers = fetchTypeResolverComponents()
    private final val interfaceTypeResolvers = typeResolvers
        .filter { it.key.scope == TypeResolver.Scope.INTERFACE }
    private final val unionTypeResolvers = typeResolvers
        .filter { it.key.scope == TypeResolver.Scope.UNION }

    /**
     * Will search for a [DataFetcher] which matches the given [FieldWiringEnvironment].
     * If none were found it will return null.
     */
    fun getResolver(env: FieldWiringEnvironment): DataFetcher<*>? =
        resolvers.entries
            .firstOrNull {
                env.parentType.name == it.key.container &&
                    env.fieldDefinition.name == it.key.field
            }?.value

    /**
     * Will search for a [TypeResolver] which matches the given [InterfaceWiringEnvironment].
     * If none were found it will return null.
     */
    fun getTypeResolver(env: InterfaceWiringEnvironment): graphql.schema.TypeResolver? =
        interfaceTypeResolvers.entries
            .firstOrNull { env.interfaceTypeDefinition.name == it.key.type }
            ?.value

    /**
     * Will search for a [TypeResolver] which matches the given [UnionWiringEnvironment].
     * If none were found it will return null.
     */
    fun getTypeResolver(env: UnionWiringEnvironment): graphql.schema.TypeResolver? =
        unionTypeResolvers.entries
            .firstOrNull { env.unionTypeDefinition.name == it.key.type }
            ?.value

    /**
     * Will search for a [GraphQLScalarType] which matches the given [ScalarWiringEnvironment].
     * If none were found it will return null.
     */
    fun getScalar(env: ScalarWiringEnvironment): GraphQLScalarType? =
        scalars.entries
            .firstOrNull { env.scalarTypeDefinition.name == it.key.name }
            ?.value

    /**
     * Will fetch all components which are marked with [Resolver] or [GQLResolvers].
     * The components will then be mapped with the [DataFetcher] instance and the according annotation.
     */
    private fun fetchDataFetcherComponents(): Map<Resolver, DataFetcher<*>> {
        val singleComponents = context.getBeansWithAnnotation(Resolver::class.java).values
        val multiComponents = context.getBeansWithAnnotation(GQLResolvers::class.java).values

        // Map the single components.
        val flat = mapComponents<Resolver, DataFetcher<*>>(singleComponents)

        // Map the multi components.
        val multi = mapComponents<GQLResolvers, DataFetcher<*>>(multiComponents)
            .flatMap { map -> map.key.resolvers.map { Pair(it, map.value) } }
            .associate { it }

        // Join the maps.
        return flat.plus(multi)
    }

    /**
     * Will fetch all components which are marked with [Directive].
     * The components will be then be mapped with the [SchemaDirectiveWiring] instance and the according annotation.
     */
    private fun fetchDirectiveComponents(): Map<Directive, SchemaDirectiveWiring> =
        mapComponents(context.getBeansWithAnnotation(Directive::class.java).values)

    /**
     * Will fetch all components which are marked with [TypeResolver].
     * The components will be then mapped with the [TypeResolver] instance and the according instance.
     */
    private fun fetchTypeResolverComponents(): Map<com.auritylab.graphql.kotlin.toolkit.spring.annotation.TypeResolver, graphql.schema.TypeResolver> =
        mapComponents(context.getBeansWithAnnotation(TypeResolver::class.java).values)

    /**
     * Will fetch all components which are marked with [Scalar]
     */
    private fun fetchScalarComponents(): Map<Scalar, GraphQLScalarType> =
        mapComponents<Scalar, Coercing<*, *>>(context.getBeansWithAnnotation(Scalar::class.java).values)
            // Map the value to an actual GraphQLScalarType instance.
            .map { Pair(it.key, buildGraphQLScalarType(it.key.name, it.value)) }
            .associate { it }

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
    private inline fun <reified A : Annotation, reified C : Any> mapComponents(
        components: Collection<Any?>
    ): Map<A, C> = components
        // Filter with type check
        .filterIsInstance<C>()
        // Associate with the annotation (It assumes there is a annotation of that type).
        .associateBy {
            AnnotationUtils.findAnnotation(it::class.java, A::class.java)!!
        }
}
