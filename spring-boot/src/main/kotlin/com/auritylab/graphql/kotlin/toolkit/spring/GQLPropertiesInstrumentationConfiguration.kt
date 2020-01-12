package com.auritylab.graphql.kotlin.toolkit.spring

import graphql.execution.instrumentation.Instrumentation
import graphql.execution.instrumentation.tracing.TracingInstrumentation
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**
 * Takes care about registering [Instrumentation] beans based on the properties ([GQLProperties]).
 */
@Component
class GQLPropertiesInstrumentationConfiguration {
    /**
     * Will conditionally create a bean of type [TracingInstrumentation] if the
     * "graphql-kotlin-toolkit.spring.enableTracing" property is set to "true".
     */
    @Bean
    @ConditionalOnProperty(name = ["graphql-kotlin-toolkit.spring.enableTracing"], havingValue = "true")
    fun tracingInstrumentation(): Instrumentation =
        TracingInstrumentation()
}
