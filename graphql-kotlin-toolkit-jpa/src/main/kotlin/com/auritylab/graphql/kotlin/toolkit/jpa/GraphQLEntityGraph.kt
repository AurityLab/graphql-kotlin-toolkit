package com.auritylab.graphql.kotlin.toolkit.jpa

import com.auritylab.graphql.kotlin.toolkit.jpa.directive.EntityHintDirective
import graphql.schema.DataFetchingFieldSelectionSet
import graphql.schema.SelectedField
import javax.persistence.EntityGraph
import javax.persistence.EntityManager
import kotlin.reflect.KClass

class GraphQLEntityGraph<T : Any>(
    private val klass: KClass<T>,
    private val entityManager: EntityManager,
    private val rootSelection: DataFetchingFieldSelectionSet
) {
    private val directive = EntityHintDirective

    fun getEntityGraph(): EntityGraph<T> {
        val rootGraph = entityManager.createEntityGraph(klass.java)

        // Get all root nodes which have entity hints.
        val hintedFields = getHintedSelections(rootSelection)

        TODO()
    }

    /**
     * Will return all [SelectedField]s which are from the root type.
     */
    private fun getRootSelectedFields(selection: DataFetchingFieldSelectionSet): List<SelectedField> {
        return selection.fields.filter { it.qualifiedName.contains("/") }
    }

    /**
     * Will check if the given [selection] has any fields, which are marked with the entity hint directive. This will
     * only mind root selections, and no sub-selections.
     */
    private fun hasHintedSelections(selection: DataFetchingFieldSelectionSet): Boolean {
        return getRootSelectedFields(selection).any { directive[it.fieldDefinition] }
    }

    /**
     * Will return all fields which are hinted from the given [selection]. A field within a
     * [DataFetchingFieldSelectionSet] is hinted when it's on the root and has the entity hint directive present.
     */
    private fun getHintedSelections(selection: DataFetchingFieldSelectionSet): List<HintedSelectedField> {
        return getRootSelectedFields(selection).mapNotNull {
            getEntityHint(it)?.let { h -> HintedSelectedField(it, h) }
        }
    }

    /**
     * Will return the [EntityHintDirective.Model] for the given [field]. This might be null if there is no entity hint
     * directive present on the field.
     */
    private fun getEntityHint(field: SelectedField): EntityHintDirective.Model? {
        val definition = field.fieldDefinition

        // Return null if no entity hint is present on the field.
        if (!directive[definition])
            return null

        return directive.getArguments(definition)
    }
}

/**
 * Holder class for a [SelectedField] and a [EntityHintDirective.Model].
 */
internal data class HintedSelectedField(
    val selectedField: SelectedField,
    val model: EntityHintDirective.Model
)
