package com.auritylab.graphql.kotlin.toolkit.spring

import com.auritylab.graphql.kotlin.toolkit.spring.api.GQLInvocation
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import java.util.concurrent.CompletableFuture
import org.springframework.stereotype.Component
import org.springframework.web.context.request.WebRequest

@Component
internal class InternalGQLInvocation(
    private val gql: GraphQL
) : GQLInvocation {
    override fun invoke(data: GQLInvocation.Data, request: WebRequest): CompletableFuture<ExecutionResult> {
        val executionInput = ExecutionInput.newExecutionInput()
            .query(data.query)
            .operationName(data.operationName)
            .variables(data.variables)
            .build()
        return gql.executeAsync(executionInput)
    }
}
