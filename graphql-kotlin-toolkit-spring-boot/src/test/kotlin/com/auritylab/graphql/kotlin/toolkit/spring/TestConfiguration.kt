package com.auritylab.graphql.kotlin.toolkit.spring

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import com.auritylab.graphql.kotlin.toolkit.spring.api.schemaOfResourceFiles
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import graphql.ExecutionResultImpl
import java.util.concurrent.CompletableFuture
import org.mockito.Mockito
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Configuration
@EnableWebMvc
@EnableAutoConfiguration
@Import(AutoConfiguration::class)
internal class TestConfiguration {
    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    @Bean
    fun schema() = schemaOfResourceFiles("schemas/schema.graphqls")

    @Bean
    @Profile("graphql-invocation-mock")
    fun invocation(): GraphQLInvocation {
        val m = Mockito.mock(GraphQLInvocation::class.java)

        whenever(m.invoke(any(), any())).thenReturn(CompletableFuture.completedFuture(ExecutionResultImpl(listOf())))

        return m
    }
}
