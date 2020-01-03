package com.auritylab.gql.kotlin.poet.generator

import com.auritylab.gql.kotlin.poet.PoetOptions
import com.auritylab.gql.kotlin.poet._TestObjects
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
