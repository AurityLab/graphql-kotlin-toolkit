package com.auritylab.graphql.kotlin.codegen.helper

import com.squareup.kotlinpoet.ClassName
import graphql.schema.GraphQLDirectiveContainer

internal object KotlinRepresentationHelper {
    private const val DIRECTIVE_NAME = "kotlinRepresentation"

    /**
     * Will search for a directive with the name of [DIRECTIVE_NAME]. If the directive exists it will try to form a
     * [ClassName]. If not it will just return null.o
     */
    fun getClassName(directivesContainer: GraphQLDirectiveContainer): ClassName? {
        // Get the directive or return null if not available.
        val possibleDirective = directivesContainer.getDirective(DIRECTIVE_NAME)
                ?: return null

        // Get the "class" argument. This should never return as the argument must be available.
        val classArgument = possibleDirective.getArgument("class")
                ?: return null

        // Cast the argument value to a string.
        val stringValue = (classArgument.value as String)

        return ClassName.bestGuess(stringValue)
    }
}
