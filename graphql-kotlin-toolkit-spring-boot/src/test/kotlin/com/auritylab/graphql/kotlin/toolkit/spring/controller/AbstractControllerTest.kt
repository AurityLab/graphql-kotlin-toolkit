package com.auritylab.graphql.kotlin.toolkit.spring.controller

import com.auritylab.graphql.kotlin.toolkit.spring.TestConfiguration
import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.clearInvocations
import me.lazmaid.kraph.Kraph
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc

@DirtiesContext
@AutoConfigureMockMvc
@ContextConfiguration(classes = [TestConfiguration::class])
internal abstract class AbstractControllerTest {
    @Autowired
    protected lateinit var mvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var invocation: GraphQLInvocation

    @BeforeEach
    fun resetMock() {
        clearInvocations(invocation)
    }
    /**
     * Will create a GraphQL Query for testing purpose.
     */
    protected fun query(): String =
        Kraph {
            query {
                fieldObject("getUser") {
                    field("id")
                    field("name")
                    field("surname")
                }
            }
        }.toGraphQueryString()

    /**
     * Will create a JSON encoded GraphQL body. The [query] must be given, [operationName] and [variables] are optional.
     */
    protected fun body(query: String, operationName: String?, variables: Map<String, Any?>?): String {
        val map = mutableMapOf<String, Any>()

        map["query"] = query

        operationName
            ?.let { map["operationName"] = it }

        variables
            ?.let { map["variables"] = it }

        return objectMapper.writeValueAsString(map)
    }

    /**
     * Will return a [ByteArray] which contains a file for testing purpose.
     */
    protected fun file(): ByteArray =
        javaClass.classLoader.getResourceAsStream("test_file.png").readAllBytes()
}
