package com.auritylab.graphql.kotlin.toolkit.spring

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import java.util.concurrent.CompletableFuture
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component
import org.springframework.web.context.request.WebRequest

@Component
@ConditionalOnMissingBean(GraphQLInvocation::class)
internal class InternalGQLInvocation(
    private val gql: GraphQL
) : GraphQLInvocation {
    override fun invoke(data: GraphQLInvocation.Data, request: WebRequest): CompletableFuture<ExecutionResult> {
        val executionInput = ExecutionInput.newExecutionInput()
            .query(data.query)
            .operationName(data.operationName)
            .variables(data.variables)
            .build()
        return gql.executeAsync(executionInput)
    }
}
