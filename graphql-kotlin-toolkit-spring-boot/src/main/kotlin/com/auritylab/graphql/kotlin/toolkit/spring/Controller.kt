package com.auritylab.graphql.kotlin.toolkit.spring

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import com.auritylab.kotlin.object_path.KObjectPath
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import graphql.ExecutionResult
import java.util.concurrent.CompletableFuture
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
        @RequestParam(value = "query") query: String,
        @RequestParam(value = "operationName", required = false) operationName: String?,
        @RequestParam(value = "variables", required = false) variables: String?,
        request: WebRequest
    ): CompletableFuture<ExecutionResult> =
        execute(query, operationName, variables, request)

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
    ): CompletableFuture<ExecutionResult> {
        // Parse the given contentType into a MediaType.
        val parsedMediaType = MediaType.parseMediaType(contentType)

        // If thee body is given and the contentType is application/json just parse the body and execute the data.
        if (body != null && parsedMediaType.equalsTypeAndSubtype(MediaType.APPLICATION_JSON)) {
            val operation = parseOperation(body)
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to parse operation")
            return execute(operation, request)
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

    /**
     * Will accept POST (multipart/form-data) requests.
     *
     * See:
     * - https://github.com/jaydenseric/graphql-multipart-request-spec
     */
    @RequestMapping(
        value = ["\${graphql-kotlin-toolkit.spring.endpoint:graphql}"],
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun postMultipart(
        @RequestParam(value = "operations") operations: String,
        @RequestParam(value = "map") map: String,
        multipartRequest: MultipartRequest,
        request: WebRequest
    ): CompletableFuture<ExecutionResult> {
        val parsedOperation = parseOperation(operations)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to parse operation")
        val parsedMap = parseMap(map)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to parse map")

        parsedMap.forEach { (mapKey, mapValue) ->
            // Check if the file exists.
            if (!multipartRequest.fileMap.containsKey(mapKey))
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "File '$mapKey' could not be found")

            mapValue.forEach { path ->
                try {
                    KObjectPath(parsedOperation).path(path).set(multipartRequest.fileMap[mapKey])
                } catch (ex: Exception) {
                }
            }
        }

        return execute(parsedOperation, request)
    }

    /**
     * Will parse the given [variables] input into a [Map].
     * This method utilizes the Jackson [ObjectMapper].
     */
    private fun parseVariables(variables: String): Map<String, Any> = objectMapper.readValue(variables)

    /**
     * Will parse the given [operation] input into a [Operation] instance.
     * This method utilizes the Jackson [ObjectMapper].
     */
    private fun parseOperation(operation: String): Operation? =
        try {
            objectMapper.readValue(operation)
        } catch (ex: Exception) {
            null
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
     * Will execute the given [operation] using the [invocation].
     */
    private fun execute(
        operation: Operation,
        request: WebRequest
    ): CompletableFuture<ExecutionResult> =
        invocation.invoke(
            GraphQLInvocation.Data(operation.query, operation.operationName, operation.variables),
            request
        )

    /**
     * Represents a GraphQL operation with a [query], [operationName] (optional) and [variables] (optional).
     */
    private data class Operation(
        val query: String = "",
        val operationName: String?,
        val variables: Map<String, String>?
    )
}
