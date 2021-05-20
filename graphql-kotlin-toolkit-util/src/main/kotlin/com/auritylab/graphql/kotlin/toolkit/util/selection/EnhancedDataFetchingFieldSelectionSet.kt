package com.auritylab.graphql.kotlin.toolkit.util.selection

import com.auritylab.graphql.kotlin.toolkit.codegenbinding.types.MetaFieldsContainer
import com.auritylab.graphql.kotlin.toolkit.common.markers.Experimental
import graphql.execution.MergedSelectionSet
import graphql.schema.DataFetchingFieldSelectionSet
import graphql.schema.GraphQLFieldDefinition
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
) :
    DataFetchingFieldSelectionSet {
    override fun get(): MergedSelectionSet {
        return delegate.get()
    }

    override fun getArguments(): MutableMap<String, MutableMap<String, Any>> {
        return delegate.arguments
    }

    override fun getDefinitions(): MutableMap<String, GraphQLFieldDefinition> {
        return delegate.definitions
    }

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

    override fun getFields(fieldGlobPattern: String?): MutableList<SelectedField> {
        return delegate.getFields(fieldGlobPattern)
    }

    override fun getField(fqFieldName: String?): SelectedField {
        return delegate.getField(fqFieldName)
    }
}
