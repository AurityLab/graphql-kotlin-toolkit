package com.auritylab.graphql.kotlin.toolkit.spring

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import com.auritylab.kotlin.object_path.KObjectPath
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import graphql.ExecutionResult
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
import org.springframework.web.multipart.MultipartRequest
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.CompletableFuture

/**
 * Implements the controller, which handles all incoming requests.
 * This supports GET and POST requests.
 */
@RestController
internal class Controller(
    private val objectMapper: ObjectMapper,
    private val invocation: GraphQLInvocation
) {
    companion object {
        private const val GRAPHQL_CONTENT_TYPE_VALUE = "application/graphql"
        private val GRAPHQL_CONTENT_TYPE = MediaType.parseMediaType(GRAPHQL_CONTENT_TYPE_VALUE)
    }

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
        @RequestParam(value = "query", required = false) query: String?,
        @RequestParam(value = "operationName", required = false) operationName: String?,
        @RequestParam(value = "variables", required = false) variables: String?,
        request: WebRequest
    ): CompletableFuture<ExecutionResult> {
        // The query parameter must be present.
        if (query == null)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Query parameter not found")

        return execute(query, operationName, variables, request)
    }

    /**
     * Will accept POST requests.
     *
     * See:
     * - https://graphql.org/learn/serving-over-http/#post-request
     */
    @RequestMapping(
        value = ["\${graphql-kotlin-toolkit.spring.endpoint:graphql}"],
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE, GRAPHQL_CONTENT_TYPE_VALUE]
    )
    fun post(
        @RequestHeader(value = HttpHeaders.CONTENT_TYPE) contentType: String,
        @RequestParam(value = "query", required = false) query: String?,
        @RequestBody(required = false) body: String?,
        request: WebRequest
    ): CompletableFuture<ExecutionResult> {
        // Parse the given contentType into a MediaType.
        val parsedMediaType = MediaType.parseMediaType(contentType)

        // If thee body is given and the contentType is application/json just parse the body and execute the data.
        if (body != null && parsedMediaType.equalsTypeAndSubtype(MediaType.APPLICATION_JSON)) {
            val parsedBody = objectMapper.readValue<Body>(body)
            return execute(parsedBody, request)
        }

        // If a body is given and the contentType is application/graphql just use the body as query.
        if (body != null && parsedMediaType.equalsTypeAndSubtype(GRAPHQL_CONTENT_TYPE))
            return execute(body, null, null, request)

        // If the query parameter is give just is it as query.
        if (query != null)
            return execute(query, null, null, request)

        // Non of the conditions above matched, therefore an error will be thrown.ø
        throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Unable to process GraphQL request")
    }

    @RequestMapping(
        value = ["\${graphql-kotlin-toolkit.spring.endpoint:graphql}"],
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun postMultipart(
        @RequestParam(value = "operations") operations: String,
        @RequestParam(value = "map") map: String,
        request: MultipartRequest
    ): CompletableFuture<ExecutionResult> {
        val parsedOperation = parseSingleOperation(operations)
            ?: throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Unable to parse operations!")
        val parsedMap = parseMap(map)
            ?: throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Unable to parse map!")

        parsedMap.forEach { (key, value) ->
            value.forEach { path ->
                KObjectPath(parsedOperation).path(path).set(request.fileMap[key])
            }
        }

        println(request.fileMap)
        println(parsedOperation)
        println(parsedMap)

        throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Unable to process GraphQL request!")
    }

    /**
     * Will parse the given [variables] input into a [Map].
     * This method utilizes the Jackson [ObjectMapper].
     */
    private fun parseVariables(variables: String): Map<String, Any> {
        return objectMapper.readValue(variables)
    }

    private fun parseSingleOperation(operations: String): List<Body>? {
        return try {
            listOf(objectMapper.readValue<Body>(operations))
        } catch (ex: JsonMappingException) {
            null
        }
    }

    private fun parseMap(map: String): Map<String, List<String>>? {
        return try {
            objectMapper.readValue<Map<String, List<String>>>(map)
        } catch (ex: JsonMappingException) {
            null
        }
    }

    /**
     * Will execute the given [query], using the given additional parameters.
     */
    private fun execute(
        query: String?,
        operationName: String?,
        variables: String?,
        request: WebRequest
    ): CompletableFuture<ExecutionResult> =
        invocation.invoke(GraphQLInvocation.Data(query, operationName, variables?.let { parseVariables(it) }), request)

    /**
     * Will execute the given [body] using the [invocation].
     */
    private fun execute(
        body: Body, request: WebRequest
    ): CompletableFuture<ExecutionResult> =
        invocation.invoke(GraphQLInvocation.Data(body.query, body.operationName, body.variables), request)

    /**
     * Represents the body for a post request.
     */
    private data class Body(
        val query: String = "",
        val operationName: String?,
        val variables: Map<String, String>?
    )
}
