package com.auritylab.graphql.kotlin.toolkit.codegen.generator.meta

import com.auritylab.graphql.kotlin.toolkit.codegen._test.AbstractCompilationTest
import com.auritylab.graphql.kotlin.toolkit.codegen._test.TestObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.reflect.full.memberProperties

internal class MetaObjectTypeFieldGeneratorTest : AbstractCompilationTest() {
    @Test
    fun `should generate compilable code`() {
        // Create the generator to test.
        val generator =
            MetaObjectTypeFieldGenerator(TestObject.options, TestObject.kotlinTypeMapper, TestObject.generatedMapper)

        // Compile the generator
        val generated = compile(generator).main

        // As a simple assertion we just assert against the count of properties
        assertEquals(4, generated.memberProperties.size)
    }
}
