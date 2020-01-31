package com.auritylab.graphql.kotlin.toolkit.spring.annotation

import graphql.schema.Coercing
import org.springframework.stereotype.Component

/**
 * Describes a annotation which shall only be used on [Coercing].
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Scalar(
    /**
     * The actual name of the scalar.
     */
    val name: String
)
