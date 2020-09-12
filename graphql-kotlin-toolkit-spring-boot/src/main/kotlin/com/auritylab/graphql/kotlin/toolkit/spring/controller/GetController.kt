package com.auritylab.graphql.kotlin.toolkit.spring.controller

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import java.util.concurrent.CompletableFuture

@RestController
class GetController(
    objectMapper: ObjectMapper,
    invocation: GraphQLInvocation
) : AbstractController(objectMapper, invocation) {
    /**
     * Will accept GET requests. The [query] has to be preset, [operationName] and [variables] are optional.
     *
     * See:
     * - https://graphql.org/learn/serving-over-http/#get-request
     * - https://github.com/APIs-guru/graphql-over-http#get
     */
    @RequestMapping(
        value = ["\${graphql-kotlin-toolkit.spring.endpoint:graphql}"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun get(
        @RequestParam(value = "query") query: String,
        @RequestParam(value = "operationName", required = false) operationName: String?,
        @RequestParam(value = "variables", required = false) variables: String?,
        request: WebRequest
    ): CompletableFuture<out Any> =
        execute(
            Operation(
                query,
                operationName,
                variables?.let { parse<Map<String, Any>>(it) }
            ),
            request
        )
}
