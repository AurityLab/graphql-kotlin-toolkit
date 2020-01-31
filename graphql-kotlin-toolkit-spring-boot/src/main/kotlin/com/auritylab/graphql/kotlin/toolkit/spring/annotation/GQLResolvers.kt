package com.auritylab.graphql.kotlin.toolkit.spring.annotation

import org.springframework.stereotype.Component

/**
 * Describes a annotation which contains multiple [Resolver].
 *
 * @see Resolver For further documentation.
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GQLResolvers(
    vararg val resolvers: Resolver
)
