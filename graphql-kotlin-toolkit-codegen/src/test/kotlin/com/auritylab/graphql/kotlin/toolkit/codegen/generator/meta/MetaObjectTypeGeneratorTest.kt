package com.auritylab.graphql.kotlin.toolkit.codegen.generator.meta

import com.auritylab.graphql.kotlin.toolkit.codegen._test.AbstractCompilationTest
import com.auritylab.graphql.kotlin.toolkit.codegen._test.TestObject
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.uppercaseFirst
import graphql.Scalars
import graphql.schema.GraphQLObjectType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

internal class MetaObjectTypeGeneratorTest : AbstractCompilationTest() {
    @Test
    fun `should generate compilable code`() {
        // Create a simple ObjectType for this basic test.
        val testObjectType = GraphQLObjectType.newObject()
            .name("TestObject")
            .field {
                it.name("id")
                it.type(Scalars.GraphQLString)
            }
            .build()

        // Compile the generated class.
        val generated = compile(createDefaultGenerator(testObjectType), metaObjectTypeFieldGenerator).main

        // Assert that the generated type is an object.
        assertNotNull(generated.objectInstance)
    }

    @Test
    fun `should generate field meta for scalar properly`() {
        // Create a simple ObjectType with a scalar as type.
        val testObjectType = GraphQLObjectType.newObject()
            .name("TestObject")
            .field {
                it.name("id")
                it.type(Scalars.GraphQLID)
            }
            .build()

        val generated = compile(createDefaultGenerator(testObjectType), metaObjectTypeFieldGenerator).main

        // Load the meta object of the "id" field.
        val idField = getFieldInstance(generated, "id")

        // Assert against the properties.
        assertEquals("id", getFieldMetaValue<String>(idField, "name"))
        assertEquals("ID", getFieldMetaValue<String>(idField, "type"))
        assertEquals(String::class, getFieldMetaValue<KClass<*>>(idField, "runtimeType"))

        // There is no ref available as its just a scalar type...
        assertThrows<IllegalStateException> {
            getFieldMetaValue<Any>(idField, "ref")
        }
    }

    @Test
    fun `should generate field meta for referenced type properly`() {
        val referencedObjectType = GraphQLObjectType.newObject()
            .name("Referenced")
            .field {
                it.name("id")
                it.type(Scalars.GraphQLID)
            }
            .build()

        val testObjectType = GraphQLObjectType.newObject()
            .name("TestObject")
            .field {
                it.name("referenced")
                it.type(referencedObjectType)
            }
            .build()

        // Compile the generator outputs. There are two generators involved because of the reference.
        // 'testObjectType' is used as the main.
        val generated = compile(
            createDefaultGenerator(testObjectType),
            createDefaultGenerator(referencedObjectType),
            metaObjectTypeFieldGenerator
        )

        // Load the field.
        val referencedField = getFieldInstance(generated.main, "referenced")

        // Assert against the basic properties.
        assertEquals("referenced", getFieldMetaValue<String>(referencedField, "name"))
        assertEquals("Referenced", getFieldMetaValue<String>(referencedField, "type"))
        assertEquals(Any::class, getFieldMetaValue<KClass<*>>(referencedField, "runtimeType"))

        // Load the referenced object from the class loader. We don't assert against it as it *should* work and
        // would otherwise throw an exception anyway.
        val metaReferenced =
            generated.classLoader.loadClass("graphql.kotlin.toolkit.codegen.meta.objectType.MetaReferenced").kotlin

        // Load the ref object from the field meta.
        val referencedObject = getFieldMetaValue<Any>(referencedField, "ref")

        // Assert the objects are equal...
        assertEquals(metaReferenced.objectInstance!!, referencedObject)
    }

    @Test
    fun `should provide all fields through allFields property`() {
        // Create the test ObjectType with exactly 3 fields.
        val testObjectType = GraphQLObjectType.newObject()
            .name("TestObject")
            .field {
                it.name("id")
                it.type(Scalars.GraphQLID)
            }
            .field {
                it.name("name")
                it.type(Scalars.GraphQLString)
            }
            .field {
                it.name("surname")
                it.type(Scalars.GraphQLString)
            }
            .build()

        // Compile it...
        val generated = compile(
            createDefaultGenerator(testObjectType),
            metaObjectTypeFieldGenerator
        ).main

        // Load the allFields Set instance...
        val allFieldsInstance = getFieldMetaValue<Set<*>>(generated.objectInstance!!, "allFields")
        assertNotNull(allFieldsInstance)

        // Assert against the size...
        assertEquals(3, allFieldsInstance!!.size)
    }

    /**
     * Will try to resolve the field with the given [name] on the given [clazz]. This internally asserts that the
     * field must exist. The return value is [Any] because the return type is generated and is therefore not on
     * this classpath.
     */
    @SuppressWarnings("UNCHECKED_CAST")
    private fun getFieldInstance(clazz: KClass<*>, name: String): Any {
        // Search for the property by the given name on the given class..
        val found = clazz.memberProperties
            .firstOrNull { it.name == "field" + name.uppercaseFirst() }
            as? KProperty1<Any, Any>

        // Assert that there the property exists.
        assertNotNull(found)

        // Resolve the actual instance and return it.
        return found!!.get(clazz.objectInstance!!)
    }

    /**
     * Will try to resolve the property with name [name] on the given [fieldInstance]. The type will also be asserted
     * against [T] and then casted.
     */
    private inline fun <reified T : Any> getFieldMetaValue(fieldInstance: Any, name: String): T? {
        // Search for the property on the class of the given field instance.
        val nameProperty = fieldInstance::class.memberProperties
            .firstOrNull { it.name == name } as KProperty1<Any, Any>

        // Assert that the property exists...
        assertNotNull(nameProperty)

        val instance = try {
            // Load the instance on the given field instance.
            nameProperty.get(fieldInstance)
        } catch (ex: InvocationTargetException) {
            // Throw the wrapped exception to properly assert against.
            throw ex.targetException
        }

        // Assert against the given type.
        assertTrue(instance is T, "Got invalid type")

        // Return the casted value.
        return instance as? T
    }

    /**
     * Will create a generator with default settings with the given [objectType].
     */
    private fun createDefaultGenerator(objectType: GraphQLObjectType) =
        MetaObjectTypeGenerator(
            objectType, TestObject.options,
            TestObject.kotlinTypeMapper,
            TestObject.generatedMapper,
        )

    /**
     * Provides the default MetaObjectTypeField generator.
     */
    private val metaObjectTypeFieldGenerator =
        MetaObjectTypeFieldGenerator(TestObject.options, TestObject.kotlinTypeMapper, TestObject.generatedMapper)
}
