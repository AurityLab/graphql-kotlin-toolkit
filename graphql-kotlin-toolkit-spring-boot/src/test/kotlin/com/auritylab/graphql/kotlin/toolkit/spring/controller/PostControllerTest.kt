package com.auritylab.graphql.kotlin.toolkit.spring.controller

import com.auritylab.graphql.kotlin.toolkit.spring.TestOperations
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class PostControllerTest : AbstractInvocationControllerTest() {
    @Test
    fun `(post) should call invocation on application-json body correctly`() {
        val inputQuery = TestOperations.getUserQuery
        val inputOperation = UUID.randomUUID().toString()
        val inputVariables = mapOf(Pair("name", "test"), Pair("surname", "test"))
        val inputContent = body(inputQuery, inputOperation, inputVariables)

        mvc.post("/graphql") {
            content = inputContent
            contentType = MediaType.APPLICATION_JSON
        }.andExpect { status { isOk } }

        verify(invocation, times(1))
            .invoke(
                argThat {
                    query == inputQuery &&
                        operationName == inputOperation &&
                        variables == inputVariables
                },
                any()
            )
    }

    @Test
    fun `(post) should call invocation on application-graphql body correctly`() {
        val inputQuery = TestOperations.getUserQuery

        mvc.post("/graphql") {
            content = inputQuery
            contentType = MediaType.parseMediaType("application/graphql")
        }.andExpect { status { isOk } }

        verify(invocation, times(1))
            .invoke(
                argThat {
                    query == inputQuery
                },
                any()
            )
    }

    @Test
    fun `(post) should call invocation on query parameter correctly`() {
        val inputQuery = TestOperations.getUserQuery

        mvc.post("/graphql") {
            param("query", inputQuery)
            contentType = MediaType.TEXT_PLAIN
        }.andExpect { status { isOk } }

        verify(invocation, times(1))
            .invoke(
                argThat {
                    query == inputQuery
                },
                any()
            )
    }

    @Test
    fun `(post) should handle nested variables correctly`() {
        val inputQuery = TestOperations.getUserQuery
        val inputOperation = UUID.randomUUID().toString()
        val inputVariables = mapOf(
            Pair("name", "test"),
            Pair("surname", "test"),
            Pair("meta", mapOf(Pair("surname", "true"), Pair("name", "false")))
        )
        val inputContent = body(inputQuery, inputOperation, inputVariables)

        mvc.post("/graphql") {
            content = inputContent
            contentType = MediaType.APPLICATION_JSON
        }.andExpect { status { isOk } }

        verify(invocation, times(1))
            .invoke(
                argThat {
                    query == inputQuery &&
                        operationName == inputOperation &&
                        variables == inputVariables
                },
                any()
            )
    }

    @Test
    fun `(post) should handle invalid request body properly`() {
        mvc.post("/graphql") {
            content = "invalid json..."
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            // Expect an unprocessable entity status because the entity couldn't be parsed...
            status { isUnprocessableEntity }
        }
    }

    @Test
    fun `(post) should handle empty post request properly`() {
        mvc.post("/graphql").andExpect { status { isBadRequest } }
    }
}
