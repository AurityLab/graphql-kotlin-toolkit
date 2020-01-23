package com.auritylab.graphql.kotlin.toolkit.spring

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("graphql-kotlin-toolkit.spring")
open class GQLProperties {
    /**
     * Represents the endpoint for the GraphQL controller.
     * Defaults to "graphql"
     */
    var endpoint: String = "graphql"

    /**
     * If the tracing extension should be enabled.
     * Defaults to "false"
     */
    var enableTracingInstrumentation: Boolean = false
}
