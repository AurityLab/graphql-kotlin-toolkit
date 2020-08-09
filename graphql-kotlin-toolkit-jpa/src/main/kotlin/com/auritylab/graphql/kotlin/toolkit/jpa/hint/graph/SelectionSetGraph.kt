package com.auritylab.graphql.kotlin.toolkit.jpa.hint.graph

import com.auritylab.graphql.kotlin.toolkit.jpa.hint.HintedFieldDefinition

class SelectionSetGraph constructor() {
    private val nodes: MutableList<HintedFieldDefinition> = mutableListOf()
    private val graphs: MutableMap<HintedFieldDefinition, SelectionSetGraph> = mutableMapOf()

    private constructor(
        nNodes: List<HintedFieldDefinition>,
        nGraphs: Map<HintedFieldDefinition, SelectionSetGraph>
    ) : this() {
        nodes.addAll(nNodes)
        graphs.putAll(nGraphs)
    }

    val allNodes: List<HintedFieldDefinition>
        get() = nodes.toList()

    val allGraphs: Map<HintedFieldDefinition, SelectionSetGraph>
        get() = graphs.toMap()

    fun addNode(field: HintedFieldDefinition) {
        if (!nodes.contains(field))
            nodes.add(field)
    }

    fun addGraph(field: HintedFieldDefinition, graph: SelectionSetGraph) {
        val existing = graphs[field]

        if (existing != null)
            graphs[field] = existing.merge(graph)
        else
            graphs[field] = graph
    }

    fun merge(other: SelectionSetGraph): SelectionSetGraph {
        val mergedNodes = mutableListOf<HintedFieldDefinition>().also {
            it.addAll(nodes)
            it.addAll(other.nodes)
        }

        val mergedGraphs = mutableMapOf<HintedFieldDefinition, SelectionSetGraph>().also { collector ->
            graphs.forEach { (key, value) ->
                val otherGraph = other.graphs[key]
                if (otherGraph != null)
                    collector[key] = value.merge(otherGraph)
                else
                    collector[key] = value
            }

            other.graphs.forEach { (key, value) ->
                if (!collector.containsKey(key))
                    collector[key] = value
            }
        }

        return SelectionSetGraph(mergedNodes, mergedGraphs)
    }
}
