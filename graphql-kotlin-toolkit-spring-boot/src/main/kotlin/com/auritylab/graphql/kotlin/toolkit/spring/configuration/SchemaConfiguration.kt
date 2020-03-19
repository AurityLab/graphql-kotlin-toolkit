package com.auritylab.graphql.kotlin.toolkit.spring.configuration

import com.auritylab.graphql.kotlin.toolkit.spring.annotation.AnnotationResolver
import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLSchemaSupplier
import com.auritylab.graphql.kotlin.toolkit.spring.schema.BaseSchemaAugmentation
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import graphql.schema.idl.WiringFactory
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SchemaConfiguration(
    private val context: ApplicationContext,
    private val annotationResolver: AnnotationResolver,
    private val wiringFactory: WiringFactory
) {
    private val augmentation = BaseSchemaAugmentation()

    @Bean
    fun configureSchema(): GraphQLSchema = buildSchema()

    /**
     * Will build the [GraphQLSchema].
     */
    private fun buildSchema(): GraphQLSchema {
        val suppliers = context.getBeansOfType<GraphQLSchemaSupplier>()

        return when {
            suppliers.isNotEmpty() -> parseSchema(fetchSchemaSuppliers(suppliers.values), wiringFactory)
            else -> throw IllegalStateException("No GQLSchemaSupplier instance was found.")
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
        val generatedSchema = generator.makeExecutableSchema(registry, buildRuntimeWiring(wiringFactory))

        return augmentation.augmentSchema(generatedSchema)
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
     * Will fetch all beans of type [GraphQLSchemaSupplier] and merge all schemas into a single [Collection].
     */
    private fun fetchSchemaSuppliers(
        suppliers: Collection<GraphQLSchemaSupplier>
    ): Collection<String> =
        suppliers.fold(mutableSetOf(), { acc, supplier ->
            acc.addAll(supplier.schemas)
            acc
        })
}
