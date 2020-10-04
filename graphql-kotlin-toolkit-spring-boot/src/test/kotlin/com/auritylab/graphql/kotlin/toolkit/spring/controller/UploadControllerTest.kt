package com.auritylab.graphql.kotlin.toolkit.spring.controller

import com.auritylab.graphql.kotlin.toolkit.spring.TestOperations
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.multipart
import org.springframework.web.multipart.MultipartFile

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class UploadControllerTest : AbstractInvocationControllerTest() {
    @Test
    fun `(post multipart) should call invocation correctly`() {
        val inputFileZero = file()
        val inputQuery = TestOperations.createUserMutation_withUpload
        val inputOperation = body(inputQuery, null, mapOf(Pair("upload", null)))
        val inputMap = objectMapper.writeValueAsString(mapOf(Pair("0", listOf("variables.upload"))))

        mvc.multipart("/graphql") {
            param("operations", inputOperation)
            param("map", inputMap)
            file("0", inputFileZero)
        }.andExpect { status { isOk } }

        verify(invocation, times(1)).invoke(
            check {
                assertEquals(inputQuery, it.query)
                assertEquals(inputFileZero, (it.variables!!["upload"] as MultipartFile).bytes)
            },
            any()
        )
    }
}
