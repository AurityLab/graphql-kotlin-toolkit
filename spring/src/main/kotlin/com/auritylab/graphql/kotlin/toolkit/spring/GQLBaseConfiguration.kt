package com.auritylab.graphql.kotlin.toolkit.spring

import com.auritylab.graphql.kotlin.toolkit.spring.api.GQLSchemaSupplier
import graphql.GraphQL
import graphql.execution.AsyncExecutionStrategy
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.ExecutionStrategy
import graphql.execution.instrumentation.ChainedInstrumentation
import graphql.execution.instrumentation.Instrumentation
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import graphql.schema.idl.WiringFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.Optional

@Component
@ConditionalOnMissingBean(GraphQL::class)
class GQLBaseConfiguration(
    private val context: ApplicationContext,
    private val annotationWiring: GQLAnnotationWiring,
    private val wiringFactory: WiringFactory,
    private val schema: Optional<GraphQLSchema>,
    private val schemaSupplier: Optional<GQLSchemaSupplier>,
    private val exceptionHandler: Optional<DataFetcherExceptionHandler>
) {
    @Bean
    fun configureGraphQL(): GraphQL {
        val builder = GraphQL.newGraphQL(buildSchema())

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
     * Will build the [GraphQLSchema].
     */
    private fun buildSchema(): GraphQLSchema {
        return when {
            schema.isPresent -> schema.get()
            schemaSupplier.isPresent -> parseSchema(schemaSupplier.get().schemaStrings)
            else -> throw IllegalStateException("No GraphQLSchema instance, nor a GQLSchemaSupplier instance was found.")
        }
    }

    /**
     * Will parse the given [schemas] and create [GraphQLSchema].
     */
    private fun parseSchema(schemas: Collection<String>): GraphQLSchema {
        val parser = SchemaParser()
        val generator = SchemaGenerator()

        // Parse the schema and join them into one registry.
        val registry = schemas.map { parser.parse(it) }.reduce(TypeDefinitionRegistry::merge)

        // Create a executable schema.
        return generator.makeExecutableSchema(registry, buildRuntimeWiring())
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
     * Will build a [RuntimeWiring] with the current [wiringFactory] and all directives from the[annotationWiring].
     */
    private fun buildRuntimeWiring(): RuntimeWiring {
        val wiring = RuntimeWiring.newRuntimeWiring()

        // Register the wiring factory.
        wiring.wiringFactory(wiringFactory)

        // Register each directive.
        annotationWiring.directives.forEach { wiring.directive(it.key.directive, it.value) }

        return wiring.build()
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
