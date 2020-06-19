package com.auritylab.graphql.kotlin.toolkit.spring.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.multipart
import org.springframework.web.multipart.MultipartFile

@ActiveProfiles("graphql-invocation-mock")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class UploadControllerTest : AbstractControllerTest() {
    @Test
    fun `(post multipart) should call invocation correctly`() {
        val inputFileZero = file()
        val inputQuery = query()
        val inputOperation = body(inputQuery, null, mapOf(Pair("file", null)))
        val inputMap = objectMapper.writeValueAsString(mapOf(Pair("0", listOf("variables.file"))))

        mvc.multipart("/graphql") {
            param("operations", inputOperation)
            param("map", inputMap)
            file("0", inputFileZero)
        }.andExpect { status { isOk } }

        verify(invocation, times(1)).invoke(check {
            assertEquals(inputQuery, it.query)
            assertEquals(inputFileZero, (it.variables!!["file"] as MultipartFile).bytes)
        }, any())
    }
}
