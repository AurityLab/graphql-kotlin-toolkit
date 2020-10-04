package com.auritylab.graphql.kotlin.toolkit.spring

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import org.springframework.web.context.request.WebRequest
import java.util.concurrent.CompletableFuture

class SyncGQLInvocation(
    private val gql: GraphQL
) : GraphQLInvocation {
    override fun invoke(data: GraphQLInvocation.Data, request: WebRequest): CompletableFuture<ExecutionResult> {
        return CompletableFuture.completedFuture(
            gql.execute(
                ExecutionInput.newExecutionInput()
                    .query(data.query)
                    .operationName(data.operationName)
                    .variables(data.variables ?: mapOf())
                    .build()
            )
        )
    }
}
