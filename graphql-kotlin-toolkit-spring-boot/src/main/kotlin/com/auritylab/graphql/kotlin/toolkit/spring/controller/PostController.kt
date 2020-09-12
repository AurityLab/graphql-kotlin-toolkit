package com.auritylab.graphql.kotlin.toolkit.spring.controller

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.CompletableFuture

@RestController
class PostController(
    objectMapper: ObjectMapper,
    invocation: GraphQLInvocation
) : AbstractController(objectMapper, invocation) {
    companion object {
        private const val GRAPHQL_CONTENT_TYPE_VALUE = "application/graphql"
        private val GRAPHQL_CONTENT_TYPE = MediaType.parseMediaType(GRAPHQL_CONTENT_TYPE_VALUE)
    }

    /**
     * Will accept POST requests.
     *
     * See:
     * - https://graphql.org/learn/serving-over-http/#post-request
     * - https://github.com/APIs-guru/graphql-over-http#post
     */
    @RequestMapping(
        value = ["\${graphql-kotlin-toolkit.spring.endpoint:graphql}"],
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun post(
        @RequestHeader(value = HttpHeaders.CONTENT_TYPE) contentType: String,
        @RequestParam(value = "query", required = false) query: String?,
        @RequestBody(required = false) body: String?,
        request: WebRequest
    ): CompletableFuture<out Any> {
        // Parse the given contentType into a MediaType.
        val parsedMediaType = MediaType.parseMediaType(contentType)

        // If thee body is given and the contentType is application/json just parse the body and execute the data.
        if (body != null && parsedMediaType.equalsTypeAndSubtype(MediaType.APPLICATION_JSON)) {
            val operation = parse<Operation>(body)
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to parse operation")
            return execute(operation, request)
        }

        // If a body is given and the contentType is application/graphql just use the body as query.
        if (body != null && parsedMediaType.equalsTypeAndSubtype(GRAPHQL_CONTENT_TYPE))
            return execute(Operation(body, null, null), request)

        // If the query parameter is give just is it as query.
        if (query != null)
            return execute(Operation(query, null, null), request)

        // Non of the conditions above matched, therefore an error will be thrown.ø
        throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Unable to process GraphQL request")
    }
}
