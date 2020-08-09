package com.auritylab.graphql.kotlin.toolkit.jpa

import javax.persistence.EntityGraph
import javax.persistence.Subgraph

object EntityGraphBuilder {
    fun populateGraph(path: Array<String>, graph: EntityGraph<*>) {
        graph.addAttributeNodes(path.first())

        if (path.size > 1) {
            val subgraph = graph.addSubgraph<Any>(path.first())
            populateGraph(path.copyOfRange(1, path.size), subgraph)
        }
    }

    fun populateGraph(path: Array<String>, graph: Subgraph<*>) {
        graph.addAttributeNodes(path.first())

        if (path.size > 1) {
            val subgraph = graph.addSubgraph<Any>(path.first())
            populateGraph(path.copyOfRange(1, path.size), subgraph)
        }
    }
}
