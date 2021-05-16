package com.auritylab.graphql.kotlin.toolkit.util.jpa.hint

import com.auritylab.graphql.kotlin.toolkit.util.jpa.directive.EntityHintDirective
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLSchema
import graphql.schema.GraphQLSchemaElement
import graphql.schema.GraphQLTypeVisitorStub
import graphql.schema.SchemaTraverser
import graphql.util.TraversalControl
import graphql.util.TraverserContext

/**
 * Implements a "graph" which indexes the given [GraphQLSchema]. The actual graph is represented by a mapping between
 * the parent container and the hinted field definitions. The graph, once created, is immutable.
 */
class FullHintGraph(
    private val schemaInput: GraphQLSchema
) {
    private val mapping: MutableMap<GraphQLFieldsContainer, MutableList<HintedFieldDefinition>> = mutableMapOf()

    init {
        // Start the traversing process.
        SchemaTraverser().depthFirst(
            HintTraverser(this::putField),
            listOfNotNull(schemaInput.queryType, schemaInput.mutationType)
        )
    }

    /**
     * Will return the hint graph. It's represented by a map, which has the parent field container as key and a
     * list of hinted field definitions as value.
     */
    val graph: Map<GraphQLFieldsContainer, List<HintedFieldDefinition>>
        get() = mapping

    /**
     * Will return the [GraphQLSchema] for which this [FullHintGraph] has been created.
     */
    val schema: GraphQLSchema
        get() = schemaInput

    /**
     * Will save the given [field] in association to the given [type].
     */
    private fun putField(type: GraphQLFieldsContainer, field: HintedFieldDefinition) {
        // Create the map entry if it does not yet exist on the mapping.
        if (!mapping.containsKey(type))
            mapping[type] = mutableListOf()

        // Add the field to the mapping.
        mapping[type]?.add(field)
    }

    /**
     * Implements a travers which is just interested in FieldDefinitions, as we have to check if they have the
     * entity hint directive and if so save it via the [adder]. A field definition is only valid if it has entity
     * hints present, therefore an empty hints array will be skipped
     */
    private class HintTraverser(
        val adder: (type: GraphQLFieldsContainer, field: HintedFieldDefinition) -> Unit
    ) : GraphQLTypeVisitorStub() {
        override fun visitGraphQLFieldDefinition(
            node: GraphQLFieldDefinition,
            context: TraverserContext<GraphQLSchemaElement>
        ): TraversalControl {
            // If the parent is not a FieldsContainer, we can just continue here.
            val parent = context.parentNode
            if (parent !is GraphQLFieldsContainer)
                return TraversalControl.CONTINUE

            // If the entity hint directive is present on the node, we can mark it.
            if (EntityHintDirective[node]) {
                // Load the model for the hint directive. Double check to satisfy the compiler.
                val directiveModel = EntityHintDirective.getArguments(node)

                // Only add if there are any hints set on the directive.
                if (directiveModel?.hints != null && directiveModel.hints.isNotEmpty())
                    adder(parent, HintedFieldDefinition(node, directiveModel.hints.toList()))
            }

            // Always continue the traversal.
            return TraversalControl.CONTINUE
        }
    }
}

/**
 * Holder class, which combines the [GraphQLFieldDefinition] and the entity hints on the field definition.
 */
data class HintedFieldDefinition(
    val field: GraphQLFieldDefinition,
    val hints: List<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HintedFieldDefinition

        if (field != other.field) return false
        if (hints != other.hints) return false

        return true
    }

    override fun hashCode(): Int {
        var result = field.hashCode()
        result = 31 * result + hints.hashCode()
        return result
    }
}
