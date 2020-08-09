package com.auritylab.graphql.kotlin.toolkit.jpa.hint

import com.auritylab.graphql.kotlin.toolkit.common.helper.GraphQLTypeHelper
import com.auritylab.graphql.kotlin.toolkit.jpa.hint.graph.SelectionSetGraph
import graphql.schema.DataFetchingFieldSelectionSet
import graphql.schema.GraphQLFieldsContainer

class SelectionSetHints(
    private val fullHintGraph: FullHintGraph,
    private val selectionSet: DataFetchingFieldSelectionSet,
    private val fieldContainer: GraphQLFieldsContainer
) {
    val selectionSetGraph: SelectionSetGraph? = buildGraphBySelectionSet(selectionSet, fieldContainer)

    private fun buildGraphBySelectionSet(
        selectionSet: DataFetchingFieldSelectionSet,
        fieldContainer: GraphQLFieldsContainer
    ): SelectionSetGraph? {
        // Load the hinted field definitions for the given field container
        val hintedFields = fullHintGraph.graph[fieldContainer]
            ?: return null

        // Create a new empty selection set graph
        val g = SelectionSetGraph()

        // Go through each hinted field and add it as nodes primarily.
        selectionSet.fields.forEach { field ->
            val filteredHint = hintedFields.firstOrNull { it.field == field.fieldDefinition }
            if (filteredHint != null) {
                g.addNode(filteredHint)

                val unwrappedType = GraphQLTypeHelper.unwrapType(filteredHint.field.type)
                if (unwrappedType is GraphQLFieldsContainer) {
                    val subSelectionSet = field.selectionSet
                    if (subSelectionSet != null) {
                        val subSelectionGraph = buildGraphBySelectionSet(subSelectionSet, unwrappedType)

                        if (subSelectionGraph != null)
                            g.addGraph(filteredHint, subSelectionGraph)
                    }
                }
            }
        }

        return g
    }
}
