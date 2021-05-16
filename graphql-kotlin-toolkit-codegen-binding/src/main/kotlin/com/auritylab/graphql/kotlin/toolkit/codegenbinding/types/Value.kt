package com.auritylab.graphql.kotlin.toolkit.codegenbinding.types

/**
 * Implementation of a simple data class which holds just one value of type [T].
 * This is required by the double-nullability feature.
 *
 * @param T Type of the [value]
 * @param value Object which is wrapped by this.
 */
@Suppress("unused")
data class Value<T>(
    val value: T
)
