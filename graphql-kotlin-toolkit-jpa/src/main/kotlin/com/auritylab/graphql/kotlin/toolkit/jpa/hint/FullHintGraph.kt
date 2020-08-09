package com.auritylab.graphql.kotlin.toolkit.jpa.hint

import com.auritylab.graphql.kotlin.toolkit.jpa.directive.EntityHintDirective
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLSchema
import graphql.schema.GraphQLSchemaElement
import graphql.schema.GraphQLTypeVisitorStub
import graphql.schema.SchemaTraverser
import graphql.util.TraversalControl
import graphql.util.TraverserContext

class FullHintGraph(
    private val schemaInput: GraphQLSchema
) {
    private val mapping: MutableMap<GraphQLFieldsContainer, MutableList<HintedFieldDefinition>> = mutableMapOf()

    init {
        // Start the traversing process.
        traverse()
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
     * Will start the traversing process over the [schemaInput]. This will basically populate the [mapping] map with
     * hinted fields.
     */
    private fun traverse() {
        SchemaTraverser().depthFirst(
            HintTraverser(this::putField),
            listOfNotNull(schemaInput.queryType, schemaInput.mutationType)
        )
    }

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
     * directive and save it via the [adder].
     */
    class HintTraverser(
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
                if (directiveModel?.hints != null && directiveModel.hints.isNotEmpty())
                    adder(parent, HintedFieldDefinition(node, directiveModel))
            }

            // Always continue the traversal.
            return TraversalControl.CONTINUE
        }
    }
}

/**
 * Holder class, which combines the [GraphQLFieldDefinition] and the model which holds the hint.
 */
data class HintedFieldDefinition(
    val field: GraphQLFieldDefinition,
    val hint: EntityHintDirective.Model
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HintedFieldDefinition

        if (field != other.field) return false
        if (hint != other.hint) return false

        return true
    }

    override fun hashCode(): Int {
        var result = field.hashCode()
        result = 31 * result + hint.hashCode()
        return result
    }
}
