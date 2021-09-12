package com.auritylab.graphql.kotlin.toolkit.common.directive

import com.auritylab.graphql.kotlin.toolkit.common.directive.exception.DirectiveValidationException
import graphql.Scalars
import graphql.introspection.Introspection
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLDirective
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class AbstractDirectiveTest {
    @Nested
    inner class ValidateDefinition {
        @Test
        fun shouldThrowExceptionOnMissingArgument() {
            assertThrows<DirectiveValidationException> {
                // _directiveOne defines exactly one argument which is expected to be not available on _directiveEmpty,
                // because it does not define any argument.
                toInternal(_directiveOne).validateDefinition(_directiveEmpty)
            }
        }

        @Test
        fun shouldIgnoreTooManyArguments() {
            assertDoesNotThrow {
                // As a reference we have a directive with no arguments. We validate against a directive which defines
                // exactly one argument. Because additional directives do not matter here, we just assert that it does
                //  not throw.
                toInternal(_directiveEmpty).validateDefinition(_directiveOne)
            }
        }

        @Test
        fun shouldThrowExceptionOnMissingLocation() {
            assertThrows<DirectiveValidationException> {
                // _directiveTwo defines one valid location which is expected to be not available on _directiveEmpty.
                // Therefore, we assert that an exception will be thrown.
                toInternal(_directiveTwo).validateDefinition(_directiveEmpty)
            }
        }

        @Test
        fun shouldIgnoreTooManyLocations() {
            assertDoesNotThrow {
                // As a reference we have a directive with no valid locations. We validate against a directive which
                // defines exactly one valid location. Because additional locations do not matter here, we assert that
                // it does not throw any exception.
                toInternal(_directiveEmpty).validateDefinition(_directiveTwo)
            }
        }

        @Test
        fun shouldThrowExceptionOnDifferingArgumentTypes () {
            assertThrows<DirectiveValidationException> {
                // As a reference we have a directive with exactly one argument named 'name' of type 'String'. We try
                // to validate against a directive which also defines exactly one argument named 'name' but with a
                // differing type 'boolean'. Because those types differ we assert that an exception will be thrown.
                toInternal(_directiveOne).validateDefinition(_directiveThree)
            }
        }
    }

    private val _directiveEmpty = GraphQLDirective.newDirective()
        .name("DirectiveEmpty").build()

    private val _directiveOne = GraphQLDirective.newDirective()
        .name("Directive")
        .argument(
            GraphQLArgument.newArgument()
                .name("name")
                .type(Scalars.GraphQLString)
        ).build()

    private val _directiveTwo = GraphQLDirective.newDirective()
        .name("DirectiveTwo")
        .validLocations(Introspection.DirectiveLocation.FIELD)
        .build()

    private val _directiveThree = GraphQLDirective.newDirective()
        .name("DirectiveThree")
        .argument(GraphQLArgument.newArgument().name("name").type(Scalars.GraphQLBoolean))
        .build()

    /**
     * Creates a new [AbstractDirective] based on the given input parameters. The [input] will be used
     * as [AbstractDirective.reference]. All other parameters are optional and define default values.
     */
    private fun toInternal(
        input: GraphQLDirective,
        name: String = "Test",
        required: Boolean = false
    ): AbstractDirective {
        return object : AbstractDirective(name) {
            override val reference: GraphQLDirective = input
        }
    }
}
