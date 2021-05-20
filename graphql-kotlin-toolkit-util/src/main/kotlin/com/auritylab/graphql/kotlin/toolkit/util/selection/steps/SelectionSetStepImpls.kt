package com.auritylab.graphql.kotlin.toolkit.util.selection.steps

import com.auritylab.graphql.kotlin.toolkit.codegenbinding.types.MetaField
import com.auritylab.graphql.kotlin.toolkit.codegenbinding.types.MetaFieldWithReference
import com.auritylab.graphql.kotlin.toolkit.codegenbinding.types.MetaFieldsContainer

internal abstract class AbstractSelectionSetFinalizeStep : SelectionSetOutputStep {
    override fun buildPattern(): String {
        val parts = mutableListOf<String>()

        var current: SelectionSetStep? = this
        while (current != null) {
            val stringRep = current.string
            if (stringRep != null)
                parts.add(stringRep)

            current = current.parent
        }

        return parts.asReversed().joinToString("/")
    }
}

internal abstract class AbstractSelectionSetFieldsContainerStep<T : MetaFieldsContainer<*>>(
    private val objectType: T,
    private val field: MetaField<*>?,
) : AbstractSelectionSetFinalizeStep(), SelectionSetFieldsContainerStep<T> {
    override fun <R : MetaFieldsContainer<*>> fieldRef(resolver: T.() -> MetaFieldWithReference<R, *>): SelectionSetFieldStep<R> {
        val resolved = resolver(objectType)

        return SelectionSetFieldStepImpl(this, resolved.ref, resolved)
    }

    override fun field(resolver: T.() -> MetaField<*>): SelectionSetSingleOutputStep {
        val resolved = resolver(objectType)

        return SelectionSetOutputFieldStepImpl(this, resolved)
    }

    override fun wildcard(): SelectionSetWildcardStep {
        return SelectionSetWildcardStepImpl(this)
    }
}

internal class SelectionSetRootStepImpl<T : MetaFieldsContainer<*>>(objectType: T) :
    AbstractSelectionSetFieldsContainerStep<T>(objectType, null), SelectionSetRootStep<T>

internal class SelectionSetWildcardStepImpl(
    override val parent: SelectionSetStep?
) : AbstractSelectionSetFinalizeStep(), SelectionSetWildcardStep {
    override val string: String = "*"
}

internal class SelectionSetFieldStepImpl<T : MetaFieldsContainer<*>>(
    override val parent: SelectionSetStep?,
    objectType: T,
    field: MetaField<*>?
) : AbstractSelectionSetFieldsContainerStep<T>(objectType, field), SelectionSetFieldStep<T> {
    override val string: String? = field?.name
}

internal class SelectionSetOutputFieldStepImpl(
    override val parent: SelectionSetStep?,
    field: MetaField<*>,
) : AbstractSelectionSetFinalizeStep(), SelectionSetSingleOutputStep {
    override val string: String = field.name
}
