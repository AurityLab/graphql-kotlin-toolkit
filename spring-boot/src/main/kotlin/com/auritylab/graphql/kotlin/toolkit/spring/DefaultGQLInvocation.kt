package com.auritylab.graphql.kotlin.toolkit.spring

import com.auritylab.graphql.kotlin.toolkit.spring.api.GQLInvocation
import graphql.ExecutionResult
import graphql.GraphQL
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component
import org.springframework.web.context.request.WebRequest
import java.util.concurrent.CompletableFuture

@Component
@ConditionalOnMissingBean(GQLInvocation::class)
internal class DefaultGQLInvocation(
    private val gql: GraphQL
) : GQLInvocation {
    override fun invoke(data: GQLInvocation.Data, request: WebRequest): CompletableFuture<ExecutionResult> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
