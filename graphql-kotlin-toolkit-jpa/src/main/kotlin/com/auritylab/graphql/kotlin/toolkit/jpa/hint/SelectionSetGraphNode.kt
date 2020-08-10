package com.auritylab.graphql.kotlin.toolkit.jpa.hint

class SelectionSetGraphNode constructor() {
    private val nodes: MutableList<HintedFieldDefinition> = mutableListOf()
    private val graphs: MutableMap<HintedFieldDefinition, SelectionSetGraphNode> = mutableMapOf()

    private constructor(
        nNodes: List<HintedFieldDefinition>,
        nGraphs: Map<HintedFieldDefinition, SelectionSetGraphNode>
    ) : this() {
        nodes.addAll(nNodes)
        graphs.putAll(nGraphs)
    }

    val allNodes: List<HintedFieldDefinition>
        get() = nodes.toList()

    val allGraphs: Map<HintedFieldDefinition, SelectionSetGraphNode>
        get() = graphs.toMap()

    /**
     * Will return if this [SelectionSetGraphNode] is empty.
     * It's empty if [allNodes] and [allGraphs] will return empty lists.
     */
    fun isEmpty(): Boolean = nodes.isEmpty() && graphs.isEmpty()

    fun addNode(field: HintedFieldDefinition) {
        if (!nodes.contains(field))
            nodes.add(field)
    }

    fun addGraph(field: HintedFieldDefinition, graphNode: SelectionSetGraphNode) {
        val existing = graphs[field]

        if (existing != null)
            graphs[field] = existing.merge(graphNode)
        else
            graphs[field] = graphNode
    }

    fun merge(other: SelectionSetGraphNode): SelectionSetGraphNode {
        val mergedNodes = mutableListOf<HintedFieldDefinition>().also {
            it.addAll(nodes)
            it.addAll(other.nodes)
        }

        val mergedGraphs = mutableMapOf<HintedFieldDefinition, SelectionSetGraphNode>().also { collector ->
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

        return SelectionSetGraphNode(
            mergedNodes,
            mergedGraphs
        )
    }
}
