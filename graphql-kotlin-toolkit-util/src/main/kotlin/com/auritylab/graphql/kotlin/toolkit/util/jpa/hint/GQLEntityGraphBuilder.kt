package com.auritylab.graphql.kotlin.toolkit.util.jpa.hint

import com.auritylab.graphql.kotlin.toolkit.common.helper.GraphQLTypeHelper
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingFieldSelectionSet
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLSchema
import javax.persistence.EntityGraph
import javax.persistence.EntityManager
import kotlin.reflect.KClass

/**
 * Implements a EntityGraph builder which is based on the SelectionSet of a GraphQL query.
 *
 * WARNING: This implementation is a working prototype! Most functionality has been implemented, but not yet
 * fully tested. Therefore it's not recommended to use it in production, as there may also be some performance issues.
 */
object GQLEntityGraphBuilder {
    /**
     * Will build the [EntityGraph] based on the given properties. When adding the [skip] property, you can for example
     * skip to the an sub-type if necessary.
     */
    private fun <T : Any> build(
        entity: KClass<T>,
        schema: GraphQLSchema,
        entityManager: EntityManager,
        selection: DataFetchingFieldSelectionSet,
        container: GraphQLFieldsContainer,
        vararg skip: String
    ): EntityGraph<T> {
        // Traverse the selection set using the skip array.
        val traverseSelectionSet = traverseSelectionSet(selection, skip)

        // Traverse the container using the skip array.
        val traverseContainer = traverseContainer(container, skip)

        // If the skipping was not successful, we just exit with an exception here.
        if (traverseSelectionSet == null || traverseContainer == null)
            throw IllegalStateException("Unable to skip ${skip.contentToString()}")

        // Build the selection set graph based on the full hint graph of the schema and the selection set and container.
        val selectionSetHints = SelectionSetGraph(FullHintGraph(schema), traverseSelectionSet, traverseContainer)

        // Load the root EntityGraph from the entity manager.
        val rootEntityGraph: EntityGraph<T> = entityManager.createEntityGraph(entity.java) as EntityGraph<T>

        // Build the paths for the selection set graph and populate the root graph with it.
        buildPaths(selectionSetHints.root)
            .forEach { EntityGraphBuilder.populateGraph(it, rootEntityGraph) }

        // Return the populated root entity graph.
        return rootEntityGraph
    }

    /**
     * Will build the [EntityGraph] based on the given properties. This method supports just passing
     * a [DataFetchingEnvironment], which holds the required data already.
     */
    fun <T : Any> build(
        entity: KClass<T>,
        entityManager: EntityManager,
        env: DataFetchingEnvironment,
        vararg skip: String
    ): EntityGraph<T>? {
        // Just unwrap the root field type.
        val unwrapped = GraphQLTypeHelper.unwrapType(env.fieldType)

        // / Check if the unwrapped type is a fields container.
        if (unwrapped !is GraphQLFieldsContainer)
            return null

        // Build the actual EntityGraph.
        return build(entity, env.graphQLSchema, entityManager, env.selectionSet, unwrapped, *skip)
    }

    inline fun <reified T : Any> build(
        entityManager: EntityManager,
        env: DataFetchingEnvironment,
        skip: String
    ): EntityGraph<T>? {
        return build(T::class, entityManager, env, skip)
    }

    private fun buildPaths(selectionNode: SelectionSetGraphNode): Set<Array<String>> {
        val paths = mutableSetOf<Array<String>>()

        selectionNode.allNodes.forEach { node ->
            node.hints.forEach { hint ->
                paths.add(hint.split(".").toTypedArray())
            }
        }

        selectionNode.allGraphs.forEach { (key, value) ->
            val subPaths = buildPaths(value)

            key.hints.forEach { hint ->
                paths.addAll(subPaths.map { subPath -> arrayOf(hint, *subPath) })
            }
        }

        return paths
    }

    private fun traverseSelectionSet(
        selectionSet: DataFetchingFieldSelectionSet,
        skip: Array<out String>
    ): DataFetchingFieldSelectionSet? {
        return skip.fold(
            selectionSet,
            { acc, it ->
                acc.getField(it)?.selectionSet ?: return null
            }
        )
    }

    private fun traverseContainer(container: GraphQLFieldsContainer, skip: Array<out String>): GraphQLFieldsContainer? {
        return skip.fold(
            container,
            { acc, it ->
                val field = acc.getFieldDefinition(it) ?: return null
                val unwrapType = GraphQLTypeHelper.unwrapType(field.type)

                if (unwrapType !is GraphQLFieldsContainer)
                    return null

                return unwrapType
            }
        )
    }
}
