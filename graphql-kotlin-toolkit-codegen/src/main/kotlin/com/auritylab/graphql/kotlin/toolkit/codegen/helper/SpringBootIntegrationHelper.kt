package com.auritylab.graphql.kotlin.toolkit.codegen.helper

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

/**
 * Represents a helper which provides additional code the spring boot integration.
 */
object SpringBootIntegrationHelper {
    private val directiveAnnotation = ClassName(
        "com.auritylab.graphql.kotlin.toolkit.spring.annotation",
        "GQLDirective"
    )
    private val resolverAnnotation = ClassName(
        "com.auritylab.graphql.kotlin.toolkit.spring.annotation",
        "GQLResolver"
    )
    private val resolversAnnotation = ClassName(
        "com.auritylab.graphql.kotlin.toolkit.spring.annotation",
        "GQLResolvers"
    )
    private val scalarAnnotation = ClassName(
        "com.auritylab.graphql.kotlin.toolkit.spring.annotation",
        "GQLScalar"
    )
    private val typeResolverAnnotation = ClassName(
        "com.auritylab.graphql.kotlin.toolkit.spring.annotation",
        "GQLTypeResolver"
    )

    /**
     * Will create a annotation which points to the GQLResolver annotation from the spring boot integration.
     * The given [container] and [field] will be added to the annotation.
     */
    fun createResolverAnnotation(container: MemberName, field: MemberName): AnnotationSpec {
        return AnnotationSpec.builder(resolverAnnotation)
            .addMember("%M, %M", container, field)
            .build()
    }

    /**
     * Will create a annotation which points to the GQLResolvers annotation from the spring boot integration.
     * The given [resolvers] is a collection of pairs where [Pair.first] is the container and [Pair.second] is the field.
     */
    fun createMultiResolverAnnotation(resolvers: Collection<Pair<String, String>>): AnnotationSpec {
        return AnnotationSpec.builder(resolversAnnotation)
            .also {
                resolvers.forEach { resolver ->
                    it.addMember("%T(\"%L\", \"%L\")", resolverAnnotation, resolver.first, resolver.second)
                }
            }
            .build()
    }
}
