package com.auritylab.graphql.kotlin.toolkit.spring.provided

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import org.springframework.web.multipart.MultipartFile

object ProvidedScalars {
    /**
     * The "Upload" scalar with the according [Coercing] implementation. The coercing will always convert into an
     * empty string, as the uploaded file will not be transferred through the variables.
     */
    val upload = GraphQLScalarType.newScalar()
        .name("Upload")
        .coercing(
            object : Coercing<MultipartFile, MultipartFile?> {
                override fun parseValue(input: Any?): MultipartFile? {
                    // We can only parse if we got a MultipartFile.
                    if (input is MultipartFile) {
                        return input
                    }

                    // By default return null.
                    return null
                }

                override fun parseLiteral(input: Any?): MultipartFile? = null
                override fun serialize(dataFetcherResult: Any?): MultipartFile? = null
            }
        ).build()
}
