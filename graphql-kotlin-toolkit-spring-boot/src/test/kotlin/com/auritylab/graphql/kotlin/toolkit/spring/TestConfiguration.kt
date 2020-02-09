package com.auritylab.graphql.kotlin.toolkit.spring

import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import com.auritylab.graphql.kotlin.toolkit.spring.api.schemaOfResourceFiles
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
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
    fun objectMapper() = ObjectMapper().registerModule(KotlinModule())

    @Bean
    fun schema() = schemaOfResourceFiles("schemas/schema.graphqls")

    @Bean
    @Profile("graphql-invocation-mock")
    fun invocation(): GraphQLInvocation = Mockito.mock(GraphQLInvocation::class.java)
}
