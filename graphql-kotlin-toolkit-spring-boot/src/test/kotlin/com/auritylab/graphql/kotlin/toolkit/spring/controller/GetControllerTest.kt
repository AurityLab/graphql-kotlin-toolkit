package com.auritylab.graphql.kotlin.toolkit.spring.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.get

@ActiveProfiles("graphql-invocation-mock")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class GetControllerTest : AbstractControllerTest() {
    @Test
    fun `(get) should call invocation correctly`() {
        val inputQuery = query()

        mvc.get("/graphql") {
            param("query", inputQuery)
        }.andExpect { status { isOk } }

        // Invocation shall be called exactly once with the input query.
        verify(invocation, times(1))
            .invoke(argThat { query == inputQuery }, any())
    }

    @Test
    fun `(get) should call invocation with variables correctly`() {
        val inputQuery = query()
        val inputVariables = mapOf(Pair("name", "test"), Pair("surname", "test"))

        mvc.get("/graphql") {
            param("query", inputQuery)
            param("variables", objectMapper.writeValueAsString(inputVariables))
        }.andExpect { status { isOk } }

        // Invocation shall be called exactly once with the input variables.
        verify(invocation, times(1))
            .invoke(argThat { variables == inputVariables }, any())
    }

    @Test
    fun `(get) should call invocation with operation name correctly`() {
        val inputQuery = query()
        val inputOperationName = UUID.randomUUID().toString()

        mvc.get("/graphql") {
            param("query", inputQuery)
            param("operationName", inputOperationName)
        }.andExpect { status { isOk } }

        // Invocation shall be called exactly once with the input operation name.
        verify(invocation, times(1))
            .invoke(argThat { operationName == inputOperationName }, any())
    }

    @Test
    fun `(get) should throw error when query param is not given`() {
        mvc.get("/graphql")
            .andExpect { status { `is`(400) } }
    }

    @Test
    fun `(get) should handle nested variables correctly`() {
        val inputQuery = query()
        val inputVariables = mapOf(
            Pair("name", "test"),
            Pair("surname", "test"),
            Pair("meta", mapOf(Pair("surname", "true"), Pair("name", "false")))
        )

        mvc.get("/graphql") {
            param("query", inputQuery)
            param("variables", objectMapper.writeValueAsString(inputVariables))
        }.andExpect { status { isOk } }

        // Invocation shall be called exactly once with the input variables.
        verify(invocation, times(1))
            .invoke(argThat { variables == inputVariables }, any())
    }
}
