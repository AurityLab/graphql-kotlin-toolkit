package com.auritylab.graphql.kotlin.toolkit.spring.controller

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import com.auritylab.kotlin.object_path.KObjectPath
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.multipart.MultipartRequest
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.CompletableFuture

@RestController
class UploadController(
    objectMapper: ObjectMapper,
    invocation: GraphQLInvocation
) : AbstractController(objectMapper, invocation) {
    /**
     * Will accept POST (multipart/form-data) requests.
     * This implements the graphql-multipart-request-spec.
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
    ): CompletableFuture<out Any> {
        val parsedOperation = parse<Operation>(operations)
            ?: throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Unable to parse operation")
        val parsedMap = parse<Map<String, List<String>>>(map)
            ?: throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Unable to parse map")

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
}
