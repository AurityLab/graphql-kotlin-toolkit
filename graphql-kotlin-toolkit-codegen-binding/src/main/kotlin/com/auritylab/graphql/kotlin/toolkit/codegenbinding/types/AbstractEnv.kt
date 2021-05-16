package com.auritylab.graphql.kotlin.toolkit.codegenbinding.types

import graphql.schema.DataFetchingEnvironment

/**
 * Abstract environment for resolvers. This just requires the original [DataFetchingEnvironment] to resolve other values.
 *
 * @param P Type of the parent of this resolver.
 * @param C Type of the global context.
 * @param original The original [DataFetchingEnvironment] provided by graphql-java.
 */
@Suppress("unused")
abstract class AbstractEnv<P : Any, C : Any>(@Suppress("CanBeParameter") val original: DataFetchingEnvironment) {
    /**
     * Provides the parent of this resolver.
     */
    val parent: P
        get() = original.getSource()

    /**
     * Provides the global context.
     */
    val context: C
        get() = original.getContext()
}
