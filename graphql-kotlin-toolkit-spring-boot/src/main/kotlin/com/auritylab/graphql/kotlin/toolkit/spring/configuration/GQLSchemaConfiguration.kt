package com.auritylab.graphql.kotlin.toolkit.spring.configuration

import com.auritylab.graphql.kotlin.toolkit.spring.GQLAnnotationResolver
import com.auritylab.graphql.kotlin.toolkit.spring.GQLWiringFactory
import com.auritylab.graphql.kotlin.toolkit.spring.api.GQLSchemaSupplier
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import graphql.schema.idl.WiringFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.util.Optional

@Configuration
@Import(GQLWiringFactory::class)
class GQLSchemaConfiguration(
    private val schemaSupplier: Optional<GQLSchemaSupplier>,
    private val annotationResolver: GQLAnnotationResolver,
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
            schemaSupplier.isPresent -> parseSchema(schemaSupplier.get().schemaStrings, wiringFactory)
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
}
