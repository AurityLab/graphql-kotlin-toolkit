package com.auritylab.graphql.kotlin.toolkit.codegenbinding.types

import kotlin.reflect.KClass

/**
 * @param R Type of the runtime type of this fields container.
 */
@Suppress("unused")
interface MetaFieldsContainer<R : Any> {
    /**
     * Returns the runtime type of this ObjectType as [KClass].
     */
    val runtimeType: KClass<R>

    /**
     * Returns all available fields on this ObjectType.
     */
    val fields: Set<MetaField<*>>
}
