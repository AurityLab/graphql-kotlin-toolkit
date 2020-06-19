package com.auritylab.graphql.kotlin.toolkit.spring.provided

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType

object ProvidedScalars {
    /**
     * The "Upload" scalar with the according [Coercing] implementation. The coercing will always convert into an
     * empty string, as the uploaded file will not be transferred through the variables.
     */
    val upload = GraphQLScalarType.newScalar()
        .name("Upload")
        .coercing(object : Coercing<String, String> {
            override fun parseValue(input: Any?): String = ""
            override fun parseLiteral(input: Any?): String = ""
            override fun serialize(dataFetcherResult: Any?): String = ""
        }).build()
}
