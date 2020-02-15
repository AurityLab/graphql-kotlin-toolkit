package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen._TestObjects
import graphql.schema.GraphQLEnumType
import io.kotlintest.specs.StringSpec

internal class EnumGeneratorTest : StringSpec({
    "should generate file spec correctly" {
        val generator = EnumGenerator(
            _TestObjects.schema.getType("UserType") as GraphQLEnumType,
            _TestObjects.mockOptions,
            _TestObjects.kotlinTypeMapper,
            _TestObjects.nameMapper
        )
    }
})
