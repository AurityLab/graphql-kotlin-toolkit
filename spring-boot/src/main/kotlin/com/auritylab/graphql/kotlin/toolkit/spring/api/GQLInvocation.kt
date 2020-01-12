package com.auritylab.graphql.kotlin.toolkit.spring.api

import graphql.ExecutionResult
import org.springframework.web.context.request.WebRequest
import java.util.concurrent.CompletableFuture

interface GQLInvocation {
    fun invoke(data: Data, request: WebRequest): CompletableFuture<ExecutionResult>

    data class Data(
        val query: String,
        val operationName: String?,
        val variables: Map<String, Any>?
    )
}
