package com.auritylab.graphql.kotlin.toolkit.spring

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Validated
@Configuration
@ConfigurationProperties("graphql-kotlin-toolkit.spring")
open class GQLProperties {
    /**
     * Represents the endpoint for the GraphQL controller.
     * Defaults to "graphql".
     */
    @Value("graphql")
    lateinit var endpoint: String

    /**
     * Represents the property to access the instrumentation properties.
     */
    var instrumentation: GQLProperties.Instrumentation = Instrumentation()

    /**
     * Represents all available options for instrumentations.
     */
    open class Instrumentation {
        /**
         * If the tracing extension should be enabled.
         * Defaults to "false".
         */
        var enableTracingInstrumentation: Boolean = false
    }
}
