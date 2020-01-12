package com.auritylab.graphql.kotlin.toolkit.spring.annotation

import graphql.schema.DataFetcher
import org.springframework.stereotype.Component

/**
 * Describes a annotation which shall only be used on [DataFetcher].
 *
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GQLResolver(
    /**
     * The container in which the field is located.
     */
    val container: String,

    /**
     * The actual name of the field for which this resolver shall be registered.s
     */
    val field: String
)
