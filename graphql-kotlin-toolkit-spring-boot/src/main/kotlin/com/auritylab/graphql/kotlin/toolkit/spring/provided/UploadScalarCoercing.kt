package com.auritylab.graphql.kotlin.toolkit.spring.provided

import graphql.schema.Coercing

class UploadScalarCoercing : Coercing<String, String> {
    override fun parseValue(input: Any?): String {
        return ""
    }

    override fun parseLiteral(input: Any?): String {
        return ""
    }

    override fun serialize(dataFetcherResult: Any?): String {
        return ""
    }
}
