package com.auritylab.graphql.kotlin.toolkit.spring

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import me.lazmaid.kraph.Kraph
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = [TestConfiguration::class])
class ControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var invocation: GraphQLInvocation

    @Test
    fun test() {
        mockMvc.get("/graphql") {
            param("query", simpleQuery())

        }.andExpect {
            status { isOk }
            request {
                asyncStarted()
            }
        }
    }

    @Test
    fun testPost() {
        mockMvc.get("/graphql") {
            param("query", simpleQuery())
        }.andExpect {
            status { isOk }
            request { asyncStarted() }
        }.andDo { print() }
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
}
