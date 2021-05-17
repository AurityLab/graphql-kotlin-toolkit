package com.auritylab.graphql.kotlin.toolkit.codegenbinding.types

/**
 * @param T Type of the referenced type of this field.
 * @param R Type of the runtime type of this field.
 */
@Suppress("unused")
interface MetaFieldWithReference<T, R : Any> : MetaField<R> {
    /**
     * The reference to the meta information object of the type of this field.
     */
    val ref: T
}
