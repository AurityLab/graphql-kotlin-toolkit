package com.auritylab.graphql.kotlin.codegen.generator

import com.auritylab.graphql.kotlin.codegen.PoetOptions
import com.auritylab.graphql.kotlin.codegen._TestObjects
import graphql.schema.GraphQLEnumType
import io.kotlintest.specs.StringSpec

internal class EnumGeneratorTest : StringSpec({
    "should generate file spec correctly" {
        val generator = EnumGenerator(PoetOptions(), _TestObjects.kotlinTypeMapper, _TestObjects.nameMapper)

        val schema = _TestObjects.schema


        val spec = generator.getEnum(schema.getType("UserType") as GraphQLEnumType)

        println(spec.toString())
    }
})
