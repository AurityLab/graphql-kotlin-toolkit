package com.auritylab.graphql.kotlin.toolkit.codegenbinding.types

import kotlin.reflect.KClass

/**
 * @param R Type of the runtime type of this field.
 */
@Suppress("unused")
interface MetaField<R : Any> {
    /**
     * Returns the actual name of the field.
     */
    val name: String

    /**
     * Returns the type of the field as a string.
     */
    val type: String

    /**
     * Returns the type of the field as a [KClass]. This might be [Any] if the type could not be determined.
     */
    val runtimeType: KClass<R>
}
