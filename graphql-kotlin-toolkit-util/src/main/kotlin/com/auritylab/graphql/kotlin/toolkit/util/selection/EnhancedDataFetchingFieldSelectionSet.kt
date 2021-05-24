package com.auritylab.graphql.kotlin.toolkit.util.selection

import com.auritylab.graphql.kotlin.toolkit.codegenbinding.types.MetaFieldsContainer
import com.auritylab.graphql.kotlin.toolkit.common.markers.Experimental
import graphql.schema.DataFetchingFieldSelectionSet
import graphql.schema.SelectedField

/**
 * Enhanced [DataFetchingFieldSelectionSet] which provides the [MetaFieldsContainer]. Basically it's just a proxy.
 *
 * @param delegate The object to which the calls will be delegated.
 * @param type Object of the [MetaFieldsContainer] -> [T].
 * @param T Type of the [MetaFieldsContainer] which is represented by this SelectionSet.
 */
@Experimental
class EnhancedDataFetchingFieldSelectionSet<T : MetaFieldsContainer<*>>(
    private val delegate: DataFetchingFieldSelectionSet,
    val type: T
) : DataFetchingFieldSelectionSet {

    override fun contains(fieldGlobPattern: String?): Boolean {
        return delegate.contains(fieldGlobPattern)
    }

    override fun containsAnyOf(fieldGlobPattern: String?, vararg fieldGlobPatterns: String?): Boolean {
        return delegate.containsAnyOf(fieldGlobPattern, *fieldGlobPatterns)
    }

    override fun containsAllOf(fieldGlobPattern: String?, vararg fieldGlobPatterns: String?): Boolean {
        return delegate.containsAllOf(fieldGlobPattern, *fieldGlobPatterns)
    }

    override fun getFields(): MutableList<SelectedField> {
        return delegate.fields
    }

    override fun getFields(fieldGlobPattern: String?, vararg fieldGlobPatterns: String?): MutableList<SelectedField> {
        return delegate.getFields(fieldGlobPattern)
    }

    override fun getImmediateFields(): MutableList<SelectedField> {
        return delegate.immediateFields
    }

    override fun getFieldsGroupedByResultKey(): MutableMap<String, MutableList<SelectedField>> {
        return delegate.fieldsGroupedByResultKey
    }

    override fun getFieldsGroupedByResultKey(
        fieldGlobPattern: String?,
        vararg fieldGlobPatterns: String?
    ): MutableMap<String, MutableList<SelectedField>> {
        return delegate.getFieldsGroupedByResultKey(fieldGlobPattern, *fieldGlobPatterns)
    }
}
