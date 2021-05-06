package com.auritylab.graphql.kotlin.toolkit.spring.controller

import com.auritylab.graphql.kotlin.toolkit.spring.SyncGQLInvocation
import com.auritylab.graphql.kotlin.toolkit.spring.api.GraphQLInvocation
import com.auritylab.graphql.kotlin.toolkit.spring.provided.ProvidedScalars
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import graphql.GraphQL
import graphql.schema.DataFetcher
import graphql.schema.GraphQLScalarType
import graphql.schema.idl.FieldWiringEnvironment
import graphql.schema.idl.ScalarWiringEnvironment
import graphql.schema.idl.WiringFactory
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles

/**
 * Abstract implementation of a controller test which verifies against a data fetcher. This will create a single
 * mocked [DataFetcher] bean with the according [WiringFactory]. The mocked [DataFetcher] can be obtained through
 * [dataFetcher].
 */
@ActiveProfiles("data-fetcher-test")
internal abstract class AbstractDataFetcherControllerTest : AbstractControllerTest() {
    @TestConfiguration
    class Configuration {
        @Bean
        @Primary
        @Profile("data-fetcher-test")
        fun syncInvocation(gql: GraphQL): GraphQLInvocation = SyncGQLInvocation(gql)

        @Bean()
        fun dataFetcher(): DataFetcher<*> {
            val m = Mockito.mock(DataFetcher::class.java)

            // Always return null.
            whenever(m.get(any())).then {
                null
            }

            return m
        }

        @Bean
        @Primary
        fun customWiringFactory(@Qualifier("dataFetcher") df: DataFetcher<*>): WiringFactory {
            return object : WiringFactory {
                override fun providesScalar(environment: ScalarWiringEnvironment): Boolean {
                    // For testing purpose we need to add the Upload scalar.
                    return environment.scalarTypeDefinition.name == "Upload"
                }

                override fun getScalar(environment: ScalarWiringEnvironment): GraphQLScalarType {
                    if (environment.scalarTypeDefinition.name == "Upload")
                        return ProvidedScalars.upload

                    throw IllegalStateException()
                }

                override fun providesDataFetcher(environment: FieldWiringEnvironment): Boolean {
                    // As we cover all tests with the createUser mutation, we just simply the DataFetcher for this field.
                    return environment.fieldDefinition.name == "createUser"
                }

                override fun getDataFetcher(environment: FieldWiringEnvironment): DataFetcher<*> {
                    if (environment.fieldDefinition.name == "createUser")
                        return df

                    throw IllegalStateException()
                }
            }
        }
    }

    @Autowired
    @Qualifier("dataFetcher")
    lateinit var dataFetcher: DataFetcher<*>
}
