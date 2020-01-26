package com.auritylab.graphql.kotlin.toolkit.codegen.helper

internal object NamingHelper {
    /**
     * Will uppercase the first letter of the given [string].
     * If the given [string] is `getUser` this method will return `GetUser`.
     */
    fun uppercaseFirstLetter(string: String): String =
            (string.substring(0, 1).toUpperCase()) + string.substring(1)
}
