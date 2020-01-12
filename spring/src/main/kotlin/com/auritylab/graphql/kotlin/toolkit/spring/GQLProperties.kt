package com.auritylab.graphql.kotlin.toolkit.spring

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@Validated
@ConstructorBinding
@ConfigurationProperties("graphql-kotlin-toolkit.spring")
data class GQLProperties(
    /**
     * Represents the endpoint for the GraphQL controller.
     * Defaults to "graphql"
     */
    @Value("graphql")
    val endpoint: String,

    /**
     * If the tracing extension should be enabled.
     * Defaults to "false"
     */
    @Value("false")
    val enableTracingInstrumentation: Boolean
)
