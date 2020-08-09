package com.auritylab.graphql.kotlin.toolkit.jpa.hint

import com.auritylab.graphql.kotlin.toolkit.jpa.EntityGraphBuilder
import com.auritylab.graphql.kotlin.toolkit.jpa.hint.graph.SelectionSetGraph
import graphql.schema.DataFetchingFieldSelectionSet
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLSchema
import javax.persistence.EntityGraph
import javax.persistence.EntityManager
import kotlin.reflect.KClass

class GQLEntityGraphBuilder(
    private val schema: GraphQLSchema,
    private val entityManager: EntityManager
) {
    val fullHintGraph = FullHintGraph(schema)

    fun <T : Any> build(
        entity: KClass<T>,
        selection: DataFetchingFieldSelectionSet,
        container: GraphQLFieldsContainer
    ): EntityGraph<T>? {
        val selectionSetHints = SelectionSetHints(fullHintGraph, selection, container)

        val g = selectionSetHints.selectionSetGraph
            ?: return null

        val paths = buildPaths(g)

        val rootEntityGraph = entityManager.createEntityGraph(entity.java)

        paths.forEach { path ->
            EntityGraphBuilder.populateGraph(path, rootEntityGraph)
        }

        return rootEntityGraph
    }

    private fun buildPaths(selection: SelectionSetGraph): Set<Array<String>> {
        val paths = mutableSetOf<Array<String>>()

        selection.allNodes.forEach { node ->
            node.hint.hints?.forEach { hint ->
                paths.add(hint.split(".").toTypedArray())
            }
        }

        selection.allGraphs.forEach { key, value ->
            val hints = key.hint.hints

            if (hints != null) {
                val subPaths = buildPaths(value)

                hints.forEach { hint ->
                    paths.addAll(subPaths.map { subPath -> arrayOf(hint, *subPath) })
                }
            }
        }

        return paths
    }
}
