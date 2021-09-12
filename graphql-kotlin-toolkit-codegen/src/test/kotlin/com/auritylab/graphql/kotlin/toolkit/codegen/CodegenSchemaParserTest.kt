package com.auritylab.graphql.kotlin.toolkit.codegen

import com.auritylab.graphql.kotlin.toolkit.codegen._test.TestObject
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class CodegenSchemaParserTest {
    @Test
    fun `should parse single schema properly`() {
        val schema = CodegenSchemaParser(TestObject.options).parseSchemas(setOf(testSchemaPath))

        // Assert that the User type is available...
        assertNotNull(schema.getObjectType("User"))
    }

    private val testSchemaPath = Path.of(
        Thread.currentThread().contextClassLoader.getResource("testschema.graphqls")!!.toURI()
    )
}


