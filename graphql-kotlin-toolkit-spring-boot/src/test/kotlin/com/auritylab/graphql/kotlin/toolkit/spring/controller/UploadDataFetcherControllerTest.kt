package com.auritylab.graphql.kotlin.toolkit.spring.controller

import com.auritylab.graphql.kotlin.toolkit.spring.TestOperations
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.multipart
import org.springframework.web.multipart.MultipartFile

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class UploadDataFetcherControllerTest : AbstractDataFetcherControllerTest() {
    @Test
    fun `(post multipart) should pass file upload to resolver correctly`() {
        val inputFileZero = file()
        val inputMutation = TestOperations.createUserMutation_withUpload
        val inputOperation = body(inputMutation, null, mapOf(Pair("upload", null)))
        val inputMap = objectMapper.writeValueAsString(mapOf(Pair("0", listOf("variables.upload"))))

        mvc.multipart("/graphql") {
            param("operations", inputOperation)
            param("map", inputMap)
            file("0", inputFileZero)
        }.andExpect { status { isOk } }

        verify(dataFetcher, times(1)).get(
            com.nhaarman.mockitokotlin2.check {
                assertTrue(it.containsArgument("name"))
                assertTrue(it.containsArgument("surname"))
                assertTrue(it.containsArgument("upload"))

                assertNotNull(it.getArgument("name"))
                assertNotNull(it.getArgument("surname"))
                assertNotNull(it.getArgument("upload"))

                assertTrue(it.getArgument<Any>("name") is String)
                assertTrue(it.getArgument<Any>("surname") is String)
                assertTrue(it.getArgument<Any>("upload") is MultipartFile)
            }
        )
    }
}
