package com.auritylab.graphql.kotlin.toolkit.codegen.helper

/**
 * A global helper object which provides some common used methods to work with strings.
 */
internal object NamingHelper {
    /**
     * Will uppercase the first letter of the given [String]. To be more precise, if [i] is "getUser", this method
     * will return "GetUser".
     *
     * @param i The string which shall be transformed.
     * @return The transformed string.
     */
    fun uppercaseFirstLetter(i: String): String = (i.substring(0, 1).toUpperCase()) + i.substring(1)

    /**
     * Will lowercase the first letter for the given [String]. To be more precise, if [i] is "GetUser", this method
     * will return "getUser"
     * @param i The string which shall be transformed.
     * @return The transformed string.
     */
    fun lowercaseFirstLetter(i: String): String = (i.substring(0, 1).toLowerCase()) + i.substring(1)
}

/**
 * Extension function which delegates to [NamingHelper.uppercaseFirstLetter].
 *
 * @see NamingHelper.uppercaseFirstLetter
 */
internal fun String.uppercaseFirst(): String = NamingHelper.uppercaseFirstLetter(this)

/**
 * Extension function which delegates to [NamingHelper.lowercaseFirstLetter].
 *
 * @see NamingHelper.lowercaseFirstLetter
 */
internal fun String.lowercaseFirst(): String = NamingHelper.lowercaseFirstLetter(this)
