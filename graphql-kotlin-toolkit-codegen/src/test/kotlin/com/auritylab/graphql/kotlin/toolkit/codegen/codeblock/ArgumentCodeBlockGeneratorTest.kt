package com.auritylab.graphql.kotlin.toolkit.codegen.codeblock

import com.auritylab.graphql.kotlin.toolkit.codegen._test.AbstractCompilationTest
import com.auritylab.graphql.kotlin.toolkit.codegen._test.TestObject
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.Scalars
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ArgumentCodeBlockGeneratorTest : AbstractCompilationTest() {

    @Test
    fun `should build code block for simple scalar correctly`() {
        val generator = ArgumentCodeBlockGenerator(
            TestObject.kotlinTypeMapper,
            TestObject.generatedMapper
        )

        val resolver = generator.buildResolver("argument", Scalars.GraphQLString, null)

        // Wrap the resolver and compile the code.
        val compiled = compile(getWrapperClass(resolver)).main

        // There has to be exactly one function.
        Assertions.assertEquals(1, compiled.declaredFunctions.size)

        // Assert against the resolver function.
        val function = compiled.functions.first()
        // To access the function using reflection.
        function.isAccessible = true

        Assertions.assertEquals("resolveArgument", function.name)

        // Call the generated function.
        val firstCallResult = function.call(compiled.objectInstance, mapOf("argument" to "test"))
        Assertions.assertNotNull(firstCallResult)
        Assertions.assertEquals(String::class, firstCallResult!!::class)
        Assertions.assertEquals("test", firstCallResult)
    }

    /**
     * Will create a wrapped class for the given [funSpec]. This will return the usable [FileSpec].
     *
     * @param funSpec The function to wrap into a class.
     * @return The FileSpec which contains a class which then contains the given [funSpec].
     */
    private fun getWrapperClass(funSpec: FunSpec): FileSpec {
        return FileSpec.get("com.auritylab.test", TypeSpec.objectBuilder("Generated").addFunction(funSpec).build())
    }
}
