package com.auritylab.graphql.kotlin.toolkit.util.selection.steps

import com.auritylab.graphql.kotlin.toolkit.codegenbinding.types.MetaField
import com.auritylab.graphql.kotlin.toolkit.codegenbinding.types.MetaFieldWithReference
import com.auritylab.graphql.kotlin.toolkit.codegenbinding.types.MetaFieldsContainer

/**
 * Describes a steps for a selection set. The step basically just represents a string.
 * A step may have a parent step to represent a chain of steps. A step must be immutable.
 *
 */
interface SelectionSetStep {
    /**
     * The parent step. If this step is the root step, then null will be returned.
     */
    val parent: SelectionSetStep?

    /**
     * String presentation of this step.
     */
    val string: String?

    companion object {
        /**
         * Will start a new selection set with a [SelectionSetRootStep].
         */
        fun <T : MetaFieldsContainer<*>> start(objectType: T): SelectionSetRootStep<T> {
            return SelectionSetRootStepImpl(objectType)
        }
    }
}

/**
 * Describes a step with which other fields can be accessed.
 * @param T Type of the ObjectType which which defines the available fields.
 */
interface SelectionSetFieldsContainerStep<T : MetaFieldsContainer<*>> : SelectionSetStep {
    /**
     * Will access the field with reference on type [T]. This will return a [SelectionSetFieldStep] which can be
     * used to access fields in its type.
     */
    fun <R : MetaFieldsContainer<*>> fieldRef(resolver: T.() -> MetaFieldWithReference<R, *>): SelectionSetFieldStep<R>

    /**
     * Will access the field on type [T]. This will return a [SelectionSetOutputStep].
     */
    fun field(resolver: T.() -> MetaField<*>): SelectionSetSingleOutputStep

    /**
     * Will return a [SelectionSetWildcardStep].
     */
    fun wildcard(): SelectionSetWildcardStep
}

/**
 * Describes a root step with no parent. The root step has multiple fields.
 */
interface SelectionSetRootStep<T : MetaFieldsContainer<*>> : SelectionSetStep,
    SelectionSetFieldsContainerStep<T> {
    override val parent: SelectionSetStep?
        get() = null
    override val string: String?
        get() = null
}

/**
 * Describes a final step in the builder. This is capable of building the full pattern through [buildPattern].
 */
interface SelectionSetOutputStep : SelectionSetStep {
    /**
     * Will build the pattern using all previous steps.
     */
    fun buildPattern(): String
}

/**
 * Describes an [SelectionSetOutputStep] which may provide multiple fields.
 */
interface SelectionSetMultiOutputStep : SelectionSetOutputStep

/**
 * Describes an [SelectionSetOutputStep] which may provide a single field.
 */
interface SelectionSetSingleOutputStep : SelectionSetOutputStep

/**
 * Describes a step which represents a wildcard. It may provide multiple fields and therefore implements
 * [SelectionSetMultiOutputStep].
 */
interface SelectionSetWildcardStep : SelectionSetMultiOutputStep, SelectionSetOutputStep

/**
 * Describes step which represents a single field without a reference. It may provide a single field and therefore
 * implements [SelectionSetSingleOutputStep].
 */
interface SelectionSetFieldStep<T : MetaFieldsContainer<*>> : SelectionSetSingleOutputStep,
    SelectionSetFieldsContainerStep<T>
