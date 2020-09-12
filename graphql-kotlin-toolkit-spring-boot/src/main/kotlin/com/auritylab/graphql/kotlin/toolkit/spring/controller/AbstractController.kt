package com.auritylab.graphql.kotlin.toolkit.spring.controller

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.web.context.request.WebRequest
import java.util.concurrent.CompletableFuture

abstract class AbstractController(
    protected val objectMapper: ObjectMapper,
    private val invocation: GraphQLInvocation
) {

    /**
     * Will execute the given [operation] using the [invocation].
     */
    protected fun execute(
        operation: Operation,
        request: WebRequest
    ): CompletableFuture<out Any> =
        invocation.invoke(
            GraphQLInvocation.Data(operation.query, operation.operationName, operation.variables),
            request
        ).thenApply { it.toSpecification() }

    /**
     * Represents a GraphQL operation with a [query], [operationName] (optional) and [variables] (optional).
     */
    protected data class Operation(
        val query: String = "",
        val operationName: String?,
        val variables: Map<String, Any>?
    )

    /**
     * Will parse the given [input] into [T]. If the given [input] can not be parsed into [T] `null` will be returned.
     */
    protected inline fun <reified T : Any> parse(input: String): T? =
        try {
            objectMapper.readValue<T>(input)
        } catch (ex: Exception) {
            null
        }
}
