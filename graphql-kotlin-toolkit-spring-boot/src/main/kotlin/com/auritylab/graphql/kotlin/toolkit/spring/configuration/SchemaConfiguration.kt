package com.auritylab.graphql.kotlin.toolkit.spring.configuration

import com.auritylab.graphql.kotlin.toolkit.spring.AnnotationResolver
import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLSchemaSupplier
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import graphql.schema.idl.WiringFactory
import org.springframework.beans.factory.getBeansOfType
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SchemaConfiguration(
    private val context: ApplicationContext,
    private val annotationResolver: AnnotationResolver,
    private val wiringFactory: WiringFactory
) {
    @Bean
    @ConditionalOnMissingBean(GraphQLSchema::class)
    fun configureSchema(): GraphQLSchema = buildSchema()

    /**
     * Will build the [GraphQLSchema].
     */
    private fun buildSchema(): GraphQLSchema {
        return when {
            hasSchemaSuppliers() -> parseSchema(fetchSchemaSuppliers(), wiringFactory)
            else -> throw IllegalStateException("No GraphQLSchema instance, nor a GQLSchemaSupplier instance was found.")
        }
    }

    /**
     * Will parse the given [schemas] and create [GraphQLSchema].
     */
    private fun parseSchema(schemas: Collection<String>, wiringFactory: WiringFactory): GraphQLSchema {
        val parser = SchemaParser()
        val generator = SchemaGenerator()

        // Parse the schema and join them into one registry.
        val registry = schemas.map { parser.parse(it) }.reduce(TypeDefinitionRegistry::merge)

        // Create a executable schema.
        return generator.makeExecutableSchema(registry, buildRuntimeWiring(wiringFactory))
    }

    /**
     * Will build a [RuntimeWiring] with the current [wiringFactory] and all directives from the[annotationResolver].
     */
    private fun buildRuntimeWiring(wiringFactory: WiringFactory): RuntimeWiring {
        val wiring = RuntimeWiring.newRuntimeWiring()

        // Register the wiring factory.
        wiring.wiringFactory(wiringFactory)

        // Register each directive.
        annotationResolver.directives.forEach { wiring.directive(it.key.directive, it.value) }

        return wiring.build()
    }

    /**
     * Will check if there are any beans of type [GraphQLSchemaSupplier].
     */
    private fun hasSchemaSuppliers(): Boolean =
        context.getBeansOfType<GraphQLSchemaSupplier>().isNotEmpty()

    /**
     * Will fetch all beans of type [GraphQLSchemaSupplier] and merge all schemas into a single [Collection].
     */
    private fun fetchSchemaSuppliers(): Collection<String> =
        context.getBeansOfType<GraphQLSchemaSupplier>().values
            .fold(mutableSetOf(), { acc, supplier ->
                acc.addAll(supplier.schemas)
                acc
            })
}
