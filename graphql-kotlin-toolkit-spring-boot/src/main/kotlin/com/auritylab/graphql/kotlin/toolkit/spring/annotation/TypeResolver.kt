package com.auritylab.graphql.kotlin.toolkit.spring.annotation

import graphql.schema.TypeResolver
import org.springframework.stereotype.Component

/**
 * Describes a annotation which shall only be used on [TypeResolver].
 * This can either describe a type resolver for a Interface or a Union.
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TypeResolver(
    /**
     * The actual name of the type.
     */
    val type: String,
    /**
     * If the type is a Interface or a Union.
     */
    val scope: Scope
) {
    /**
     * Describes the scope for the type resolver.
     */
    enum class Scope {
        INTERFACE,
        UNION
    }
}
