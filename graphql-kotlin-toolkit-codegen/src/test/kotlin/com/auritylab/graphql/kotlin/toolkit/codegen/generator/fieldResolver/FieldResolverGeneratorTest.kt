package com.auritylab.graphql.kotlin.toolkit.codegen.generator.fieldResolver

import com.auritylab.graphql.kotlin.toolkit.codegen._test.AbstractMockCompilationTest
import com.auritylab.graphql.kotlin.toolkit.codegen._test.TestObject
import graphql.Scalars
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FieldResolverGeneratorTest : AbstractMockCompilationTest() {
    @Nested
    @DisplayName("Simple field resolver")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SimpleFieldResolver {
        val generator = FieldResolverGenerator(
            testObjectType,
            testSimpleFieldDefinition,
            TestObject.implementerMapper,
            TestObject.argumentCodeBlockGenerator,
            TestObject.options,
            TestObject.kotlinTypeMapper,
            TestObject.generatedMapper
        )

        lateinit var generatedClass: KClass<*>

        @BeforeAll
        fun compileCode() {
            // Compile the code of the generator
            generatedClass = compile(generator)
        }

        @Test
        @Suppress("UNCHECKED_CAST")
        fun `should generate meta properties correctly`() {
            val companionObject = generatedClass.companionObject
            val companionObjectInstance = generatedClass.companionObjectInstance
            Assertions.assertNotNull(companionObject)
            Assertions.assertNotNull(companionObjectInstance)

            // Cast to not null.
            companionObject!!
            companionObjectInstance!!

            // Assert against the meta properties.
            val companionProperties = companionObject.memberProperties
            Assertions.assertEquals(2, companionProperties.size)

            // Assert against each property in the companion object.
            val metaContainerProperty =
                companionProperties.firstOrNull { it.name == "META_CONTAINER" } as? KProperty1<Any?, String>
            val metaFieldProperty =
                companionProperties.firstOrNull { it.name == "META_FIELD" } as? KProperty1<Any?, String>
            Assertions.assertNotNull(metaContainerProperty)
            Assertions.assertNotNull(metaFieldProperty)
            metaContainerProperty!!
            metaFieldProperty!!

            Assertions.assertEquals(testObjectType.name, metaContainerProperty.call())
            Assertions.assertEquals(testSimpleFieldDefinition.name, metaFieldProperty.call())
        }

        @Test
        fun `should generate environment holder correctly`() {
            // Search for a nested class with the name "Env".
            val firstEnvClass = getEnvClass(generatedClass)

            // Assert against the member properties.
            val memberProperties = firstEnvClass.memberProperties
            Assertions.assertEquals(3, memberProperties.size)

            Assertions.assertNotNull(memberProperties.firstOrNull { it.name == "original" })
            Assertions.assertNotNull(memberProperties.firstOrNull { it.name == "parent" })
            Assertions.assertNotNull(memberProperties.firstOrNull { it.name == "context" })
        }

        @Test
        fun `should generate 'resolve' method correctly`() {
            val resolveFunction = getResolveFunction(generatedClass)

            // Assert against the parameters of the resolve function.
            val functionParameters = resolveFunction.parameters
            Assertions.assertEquals(2, functionParameters.size) // 2 as the first parameter is the receiver.

            // Assert that the type of the env parameter is the Env class of this resolver.
            val envParameter = functionParameters[1]
            Assertions.assertEquals(getEnvClass(generatedClass).starProjectedType, envParameter.type)

            // Assert that the resolve function will return a 'String?'.
            Assertions.assertEquals(String::class.createType(nullable = true), resolveFunction.returnType)
        }

        @Test
        fun `should override and implement the 'get' method correctly`() {
            val getFunction = generatedClass.memberFunctions.firstOrNull { it.name == "get" }
            Assertions.assertNotNull(getFunction)
            getFunction!!

            // Assert against the parameters of the get function.
            val functionParameters = getFunction.parameters
            Assertions.assertEquals(2, functionParameters.size) // 2 as the first parameter is the receiver.

            // Assert that the get function will return a 'String?'.
            Assertions.assertEquals(String::class.createType(nullable = true), getFunction.returnType)
        }
    }

    @Nested
    @DisplayName("Argument field resolver")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ArgumentFieldResolver {
        val generator = FieldResolverGenerator(
            testObjectType,
            testArgumentFieldDefinition,
            TestObject.implementerMapper,
            TestObject.argumentCodeBlockGenerator,
            TestObject.options,
            TestObject.kotlinTypeMapper,
            TestObject.generatedMapper
        )

        lateinit var generatedClass: KClass<*>

        @BeforeAll
        fun compileCode() {
            // Compile the code of the generator
            generatedClass = compile(generator)
        }

        @Test
        fun `should generate parameter correctly`() {
            val resolveFunction = getResolveFunction(generatedClass)

            // Assert against the function parameters.
            val functionParameters = resolveFunction.parameters
            Assertions.assertEquals(4, functionParameters.size)

            // Assert against the additional parameters.
            val boolParameter = functionParameters.firstOrNull { it.name == "bool" }
            val intParameter = functionParameters.firstOrNull { it.name == "int" }
            Assertions.assertNotNull(boolParameter)
            Assertions.assertNotNull(intParameter)
            boolParameter!!
            intParameter!!

            Assertions.assertEquals(Boolean::class.starProjectedType, boolParameter.type)
            Assertions.assertEquals(Int::class.starProjectedType, intParameter.type)
        }

        @Test
        fun `should generate return type correctly`() {
            val resolveFunction = getResolveFunction(generatedClass)

            val returnType = resolveFunction.returnType

            Assertions.assertEquals(String::class.starProjectedType, returnType)
        }
    }

    @Nested
    @DisplayName("Global context options")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GlobalContextOptions {
        val generator = FieldResolverGenerator(
            testObjectType,
            testArgumentFieldDefinition,
            TestObject.implementerMapper,
            TestObject.argumentCodeBlockGenerator,
            TestObject.options.copy(globalContext = "kotlin.String"),
            TestObject.kotlinTypeMapper,
            TestObject.generatedMapper
        )

        lateinit var generatedClass: KClass<*>

        @BeforeAll
        fun compileCode() {
            // Compile the code of the generator
            generatedClass = compile(generator)
        }

        @Test
        fun `should apply custom global context correctly`() {
            val envClass = getEnvClass(generatedClass)

            // Assert against the properties one more time
            val memberProperties = envClass.memberProperties
            Assertions.assertEquals(3, memberProperties.size)

            // Search for the "context" property.
            val contextProperty = memberProperties.firstOrNull { it.name == "context" }
            Assertions.assertNotNull(contextProperty)
            contextProperty!!

            Assertions.assertEquals(String::class.starProjectedType, contextProperty.returnType)
        }
    }

    /**
     * Will return the "Env" class from the current [generatedClass].
     */
    private fun getEnvClass(generated: KClass<*>): KClass<*> {
        val first = generated.nestedClasses.firstOrNull { it.simpleName == "Env" }
        Assertions.assertNotNull(first)
        return first!!
    }

    /**
     * Will return the "resolve" function from the current [generatedClass].
     */
    private fun getResolveFunction(generated: KClass<*>): KFunction<*> {
        val first = generated.memberFunctions.firstOrNull { it.name == "resolve" }
        Assertions.assertNotNull(first)
        return first!!
    }
}

val testSimpleFieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
    .name("simple")
    .type(Scalars.GraphQLString)
    .build()

val testArgumentFieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
    .name("arguments")
    .argument(GraphQLArgument.newArgument().name("bool").type(GraphQLNonNull(Scalars.GraphQLBoolean)).build())
    .argument(GraphQLArgument.newArgument().name("int").type(GraphQLNonNull(Scalars.GraphQLInt)).build())
    .type(GraphQLNonNull(Scalars.GraphQLString))
    .build()

val testObjectType = GraphQLObjectType.newObject()
    .name("TestObjectType")
    .field(testSimpleFieldDefinition)
    .field(testArgumentFieldDefinition)
    .build()
