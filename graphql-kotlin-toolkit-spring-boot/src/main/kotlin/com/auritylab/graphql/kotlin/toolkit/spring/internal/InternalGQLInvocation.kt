package com.auritylab.graphql.kotlin.toolkit.spring.internal

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import java.util.concurrent.CompletableFuture
import org.springframework.stereotype.Component
import org.springframework.web.context.request.WebRequest

@Component
internal class InternalGQLInvocation(
    private val gql: GraphQL
) : GraphQLInvocation {
    override fun invoke(data: GraphQLInvocation.Data, request: WebRequest): CompletableFuture<ExecutionResult> =
        gql.executeAsync(
            ExecutionInput.newExecutionInput()
                .query(data.query)
                .operationName(data.operationName)
                .variables(data.variables ?: mapOf())
                .build()
        )
}
