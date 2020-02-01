package com.auritylab.graphql.kotlin.toolkit.spring.configuration

import graphql.GraphQL
import graphql.execution.AsyncExecutionStrategy
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.ExecutionStrategy
import graphql.execution.instrumentation.ChainedInstrumentation
import graphql.execution.instrumentation.Instrumentation
import graphql.schema.GraphQLSchema
import java.util.Optional
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(SchemaConfiguration::class)
@ConditionalOnMissingBean(GraphQL::class)
class GraphQLConfiguration(
    private val context: ApplicationContext,
    private val schema: GraphQLSchema,
    private val exceptionHandler: Optional<DataFetcherExceptionHandler>
) {
    @Bean
    fun configureGraphQL(): GraphQL {
        val builder = GraphQL.newGraphQL(schema)

        // Build the Instrumentation and register it if found.
        buildInstrumentation()
            ?.also { builder.instrumentation(it) }

        // Build the execution strategy and apply it to the builder.
        buildExecutionStrategy().also {
            builder.queryExecutionStrategy(it)
            builder.mutationExecutionStrategy(it)
        }

        return builder.build()
    }

    /**
     * Will fetch all available instances of [Instrumentation] and join them if needed.
     * This will utilize the [context] to fetch all beans with type [Instrumentation].
     */
    private fun buildInstrumentation(): Instrumentation? {
        // Fetch the beans using the application context.
        val instrumentationBeans = context.getBeansOfType(Instrumentation::class.java)

        val allInstrumentation = mutableListOf<Instrumentation>()

        // Go through each found instrumentation bean and add it to the list.
        instrumentationBeans.values.forEach {
            // If it's a ChainedInstrumentation we need to add each of them.
            if (it is ChainedInstrumentation)
                allInstrumentation.addAll(it.instrumentations)
            else
                allInstrumentation.add(it)
        }

        return when {
            allInstrumentation.isEmpty() -> null
            allInstrumentation.size == 1 -> allInstrumentation[0]
            else -> ChainedInstrumentation(allInstrumentation)
        }
    }

    /**
     * Will build a [ExecutionStrategy]. By default this will always build a [AsyncExecutionStrategy],
     * optionally with the given [exceptionHandler].
     */
    private fun buildExecutionStrategy(): ExecutionStrategy =
        if (exceptionHandler.isPresent)
            AsyncExecutionStrategy(exceptionHandler.get())
        else
            AsyncExecutionStrategy()
}
