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

@RestController
internal class Controller(
    private val objectMapper: ObjectMapper,
    private val invocation: GraphQLInvocation
) {
    companion object {
        private const val GRAPHQL_CONTENT_TYPE = "application/graphql"
    }

    /**
     * Will execute the given GraphQL [query].
     *
     * Implements the specs: https://graphql.org/learn/serving-over-http/#get-request
     */
    @RequestMapping(
        value = ["\${graphql-kotlin-toolkit.spring.endpoint:graphql}"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun get(
        @RequestParam("query") query: String,
        @RequestParam(value = "operationName", required = false) operation: String?,
        @RequestParam(value = "variables", required = false) variables: String?,
        request: WebRequest
    ): CompletableFuture<ExecutionResult> =
        execute(query, operation, variables?.let { parseVariables(it) }, request)

    /**
     * Will execute a query in multiple ways:
     * - [body] is given and Content-Type is `application/json`
     * - [query] is given.
     * - [body] is given and Content-Type is `application/graphql`
     *
     * Implements the specs: https://graphql.org/learn/serving-over-http/#post-request
     */
    @RequestMapping(
        value = ["\${graphql-kotlin-toolkit.spring.endpoint:graphql}"],
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE, GRAPHQL_CONTENT_TYPE]
    )
    fun post(
        @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) contentType: String,
        @RequestParam(value = "query", required = false) query: String?,
        @RequestParam(value = "operationName", required = false) operationName: String?,
        @RequestParam(value = "variables", required = false) variables: String?,
        @RequestParam(value = "operations", required = false) operations: String?,
        @RequestParam(value = "map", required = false) map: String?,
        @RequestBody(required = false) body: String?,
        request: WebRequest
    ): CompletableFuture<ExecutionResult> {
        val parsedMediaType = MediaType.parseMediaType(contentType)

        if (body != null && contentType == MediaType.APPLICATION_JSON_VALUE) {
            val parsed = objectMapper.readValue<Body>(body)
            return execute(parsed.query, parsed.operationName, parsed.variables, request)
        }

        if (body != null && contentType == GRAPHQL_CONTENT_TYPE) {
            return execute(body, null, null, request)
        }

        if (query != null) {
            return execute(query, operationName, variables?.let { parseVariables(it) }, request)
        }

        throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Unable to process GraphQL request!")
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
        val parsedOperation = parseOperations(operations)
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

    private fun parseOperations(operations: String): List<Body>? {
        // Try to parse a single operation, if it does not succeed try to parse multiple.
        return parseSingleOperation(operations) ?: parseMultiOperation(operations)
    }

    private fun parseSingleOperation(operations: String): List<Body>? {
        return try {
            listOf(objectMapper.readValue<Body>(operations))
        } catch (ex: JsonMappingException) {
            null
        }
    }

    private fun parseMultiOperation(operations: String): List<Body>? {
        return try {
            objectMapper.readValue<List<Body>>(operations)
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

    private fun execute(
        query: String,
        operation: String?,
        variables: Map<String, Any>?,
        request: WebRequest
    ): CompletableFuture<ExecutionResult> {
        val result = invocation.invoke(GraphQLInvocation.Data(query, operation, variables), request)
        return result
    }

    /**
     * Represents the body for a post request.
     */
    private data class Body(
        val query: String = "",
        val operationName: String?,
        val variables: Map<String, Any>?
    )
}
