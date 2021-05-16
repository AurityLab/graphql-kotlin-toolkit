package com.auritylab.graphql.kotlin.toolkit.util.jpa.hint

import com.auritylab.graphql.kotlin.toolkit.common.helper.GraphQLTypeHelper
import graphql.schema.DataFetchingFieldSelectionSet
import graphql.schema.GraphQLFieldsContainer

/**
 * Implements a graph for a [DataFetchingFieldSelectionSet]. The graph will be created according to the given
 * [FullHintGraph], which holds the fully indexed schema. As the [DataFetchingFieldSelectionSet] does not hold the
 * parent type we need the [GraphQLFieldsContainer], which will represent the root object.
 */
class SelectionSetGraph(
    private val fullHintGraph: FullHintGraph,
    selectionSet: DataFetchingFieldSelectionSet,
    fieldContainer: GraphQLFieldsContainer
) {
    /**
     * Holds the root node for the graph.
     */
    val root: SelectionSetGraphNode = buildGraphBySelectionSet(selectionSet, fieldContainer)

    private fun buildGraphBySelectionSet(
        selectionSet: DataFetchingFieldSelectionSet,
        fieldContainer: GraphQLFieldsContainer
    ): SelectionSetGraphNode {
        // Create a new empty selection set graph
        val g = SelectionSetGraphNode()

        // Load the hinted field definitions for the given field container
        val hintedFields = fullHintGraph.graph[fieldContainer]
            ?: return g

        // Go through each hinted field and add it as nodes primarily.
        selectionSet.fields.forEach { field ->
            // Search for the matching hinted field.
            val filteredHint = hintedFields.firstOrNull { it.field == field.fieldDefinition }

            // Check if the hinted field is present.
            if (filteredHint != null) {
                // Add the hinted field as a single node to the current graph.
                g.addNode(filteredHint)

                // Unwrap the return type of the current field to check if we need to apply a subgraph to this graph.
                val unwrappedType = GraphQLTypeHelper.unwrapType(filteredHint.field.type)

                // Only a fields container can contain other entity hinted field definitions.
                if (unwrappedType is GraphQLFieldsContainer) {
                    val subSelectionSet = field.selectionSet
                    if (subSelectionSet != null) {
                        // Recursively build the graph for the selection and add the built graph to the current graph
                        // as a subgraph.
                        val subSelectionGraph = buildGraphBySelectionSet(subSelectionSet, unwrappedType)
                        g.addGraph(filteredHint, subSelectionGraph)
                    }
                }
            }
        }

        return g
    }
}
