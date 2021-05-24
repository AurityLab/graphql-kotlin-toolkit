package com.auritylab.graphql.kotlin.toolkit.util.selection

import com.auritylab.graphql.kotlin.toolkit.codegenbinding.types.AbstractEnv
import com.auritylab.graphql.kotlin.toolkit.codegenbinding.types.MetaObjectType
import com.auritylab.graphql.kotlin.toolkit.common.markers.Experimental
import com.auritylab.graphql.kotlin.toolkit.util.selection.steps.SelectionSetMultiOutputStep
import com.auritylab.graphql.kotlin.toolkit.util.selection.steps.SelectionSetOutputStep
import com.auritylab.graphql.kotlin.toolkit.util.selection.steps.SelectionSetRootStep
import com.auritylab.graphql.kotlin.toolkit.util.selection.steps.SelectionSetSingleOutputStep
import com.auritylab.graphql.kotlin.toolkit.util.selection.steps.SelectionSetStep
import graphql.schema.SelectedField

@Experimental
inline fun <reified T : MetaObjectType<*>> EnhancedDataFetchingFieldSelectionSet<T>.getFields(builder: SelectionSetRootStep<T>.() -> SelectionSetMultiOutputStep): List<SelectedField> =
    getFields(builder(SelectionSetStep.start(type)).buildPattern())

@Experimental
inline fun <reified T : MetaObjectType<*>> EnhancedDataFetchingFieldSelectionSet<T>.contains(builder: SelectionSetRootStep<T>.() -> SelectionSetOutputStep): Boolean =
    contains(builder(SelectionSetStep.start(type)).buildPattern())

@Experimental
val <T : MetaObjectType<*>> AbstractEnv<*, *, T>.selectionSet: EnhancedDataFetchingFieldSelectionSet<T>
    get() = EnhancedDataFetchingFieldSelectionSet(this.original.selectionSet, this.type)

@Experimental
inline fun <reified T : MetaObjectType<*>> AbstractEnv<*, *, T>.selectionFields(builder: SelectionSetRootStep<T>.() -> SelectionSetMultiOutputStep): List<SelectedField> =
    selectionSet.getFields(builder)

@Experimental
inline fun <reified T : MetaObjectType<*>> AbstractEnv<*, *, T>.selectionContains(builder: SelectionSetRootStep<T>.() -> SelectionSetOutputStep): Boolean =
    selectionSet.contains(builder)
