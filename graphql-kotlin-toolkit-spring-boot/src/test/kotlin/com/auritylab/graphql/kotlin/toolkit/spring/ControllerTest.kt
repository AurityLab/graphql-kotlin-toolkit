package com.auritylab.graphql.kotlin.toolkit.spring

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import me.lazmaid.kraph.Kraph
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.UUID

@AutoConfigureMockMvc
@ActiveProfiles("graphql-invocation-mock")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = [TestConfiguration::class])
@DirtiesContext
class ControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var invocation: GraphQLInvocation

    @BeforeEach
    fun resetMock() {
        reset(invocation)
    }

    @Test
    fun `(get) should call invocation correctly`() {
        val inputQuery = simpleQuery()

        mvc.get("/graphql") {
            param("query", inputQuery)
        }.andExpect { status { isOk } }

        // Invocation shall be called exactly once with the input query.
        verify(invocation, times(1))
            .invoke(argThat { query == inputQuery }, any())
    }

    @Test
    fun `(get) should call invocation with variables correctly`() {
        val inputQuery = simpleQuery()
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
        val inputQuery = simpleQuery()
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
    fun `(post) should call invocation on application-json body correctly`() {
        mvc.post("/graphql") {
            content = createBody(simpleQuery(), null, null)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect { status { isOk } }
    }

    private fun simpleQuery(): String {
        return Kraph {
            query {
                fieldObject("getUser") {
                    field("id")
                    field("name")
                    field("surname")
                }
            }
        }.toGraphQueryString()
    }

    private fun createBody(query: String, operationName: String?, variables: Map<String, String>?): String {
        val map = mutableMapOf<String, Any>()

        map["query"] = query

        operationName?.let {
            map["operationName"] = it
        }

        variables?.let {
            map["variables"] = it
        }

        return objectMapper.writeValueAsString(map)
    }
}
