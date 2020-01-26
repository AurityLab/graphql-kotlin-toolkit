package com.auritylab.graphql.kotlin.toolkit.spring.configuration

import graphql.execution.instrumentation.Instrumentation
import graphql.execution.instrumentation.tracing.TracingInstrumentation
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Takes care about registering [Instrumentation] beans based on the properties ([GQLProperties]).
 */
@Configuration
class GQLInstrumentationConfiguration {
    /**
     * Will conditionally create a bean of type [TracingInstrumentation] if the
     * "graphql-kotlin-toolkit.spring.enableTracing" property is set to "true".
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "graphql-kotlin-toolkit.spring.instrumentation",
        name = ["enable-tracing-instrumentation"],
        havingValue = "true"
    )
    fun tracingInstrumentation(): Instrumentation = TracingInstrumentation()
}
