package com.auritylab.graphql.kotlin.toolkit.spring.annotation

import graphql.schema.idl.SchemaDirectiveWiring
import org.springframework.stereotype.Component

/**
 * Describes a annotation which shall only be used on [SchemaDirectiveWiring].
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GQLDirective(
    /**
     * The name of the directive.
     */
    val directive: String
)
