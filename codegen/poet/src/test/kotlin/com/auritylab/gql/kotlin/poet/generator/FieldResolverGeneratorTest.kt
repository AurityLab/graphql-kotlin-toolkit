package com.auritylab.gql.kotlin.poet.generator

import com.auritylab.gql.kotlin.poet.PoetOptions
import com.auritylab.gql.kotlin.poet._TestObjects
import io.kotlintest.specs.StringSpec

internal class FieldResolverGeneratorTest : StringSpec({
    "should generate file spec correctly" {
        val fieldResolverGenerator = FieldResolverGenerator(PoetOptions(), _TestObjects.kotlinTypeMapper, _TestObjects.nameMapper)

        val schema = _TestObjects.schema


        val spec = fieldResolverGenerator.getFieldResolver(schema.queryType, schema.queryType.getFieldDefinition("getUser"))

        println(spec.toString())
    }
})
