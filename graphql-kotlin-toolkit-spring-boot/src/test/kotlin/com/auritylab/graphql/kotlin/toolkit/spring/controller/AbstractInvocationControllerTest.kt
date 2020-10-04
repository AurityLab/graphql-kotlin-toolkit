package com.auritylab.graphql.kotlin.toolkit.spring.controller

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.whenever
import graphql.ExecutionResultImpl
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import org.mockito.internal.util.MockUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CompletableFuture

/**
 * Abstract implementation of a controller test which verifies against the invocation. This will create a mocked
 * [GraphQLInvocation] bean. The mocked instance can be obtained with [invocation]. The invocations will rest
 * before each test.
 */
@ActiveProfiles("invocation-test")
internal abstract class AbstractInvocationControllerTest : AbstractControllerTest() {
    @TestConfiguration
    class Configuration {
        @Bean
        @Primary
        @Profile("invocation-test")
        fun mockedInvocation(): GraphQLInvocation {
            val m = Mockito.mock(GraphQLInvocation::class.java)

            whenever(
                m.invoke(
                    any(),
                    any()
                )
            ).thenReturn(CompletableFuture.completedFuture(ExecutionResultImpl(listOf())))

            return m
        }
    }

    @Autowired
    protected lateinit var invocation: GraphQLInvocation

    @BeforeEach
    fun resetMock() {
        // Just to be sure the instance is the mock.
        if (MockUtil.isMock(invocation))
            clearInvocations(invocation)
    }
}
