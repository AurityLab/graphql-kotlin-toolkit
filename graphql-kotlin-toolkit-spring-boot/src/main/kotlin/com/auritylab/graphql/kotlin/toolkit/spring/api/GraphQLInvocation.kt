package com.auritylab.graphql.kotlin.toolkit.spring.api

import graphql.ExecutionResult
import java.util.concurrent.CompletableFuture
import org.springframework.web.context.request.WebRequest

interface GraphQLInvocation {
    /**
     * Will execute the GraphQL Query (described in [data]) and return a [CompletableFuture]
     * which contains the [ExecutionResult]. The corresponding [WebRequest] is also supplied
     * to access additional information
     */
    fun invoke(data: Data, request: WebRequest): CompletableFuture<ExecutionResult>

    /**
     * Describes the GraphQL Query.
     */
    data class Data(
        val query: String,
        val operationName: String?,
        val variables: Map<String, Any>?
    )
}
