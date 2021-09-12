package com.auritylab.graphql.kotlin.toolkit.spring.api

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GraphQLSchemaSupplierExtensionsTest {
    @Test
    fun `should create supplier based on string collection`() {
        val supplier = schemaOfStrings(setOf("test1", "test2"))

        assertEquals(2, supplier.schemas.size)
    }

    @Test
    fun `should create supplier based on string varargs`() {
        val supplier = schemaOfStrings("test1", "test2")

        assertEquals(2, supplier.schemas.size)
    }

    @Test
    fun `should load schema from resource files by collection`() {
        val supplier = schemaOfResourceFiles(setOf("schemas/schema.graphqls"))

        assertEquals(1, supplier.schemas.size)
    }

    @Test
    fun `should load schema from resource files by varargs`() {
        val supplier = schemaOfResourceFiles("schemas/schema.graphqls")

        assertEquals(1, supplier.schemas.size)
    }

    @Test
    fun `should throw exception if resource file could not be found by collection`() {
        assertThrows<IllegalArgumentException> {
            schemaOfResourceFiles(setOf("notfound.graphqls"))
        }
    }

    @Test
    fun `should throw exception if resource file could not be found by vararg`() {
        assertThrows<IllegalArgumentException> {
            schemaOfResourceFiles("notfound.graphqls")
        }
    }
}
