package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.squareup.kotlinpoet.FileSpec

/**
 * Describes a generator which produces a [FileSpec].
 */
interface FileGenerator {
    /**
     * Will generate the code and output a [FileSpec].
     */
    fun generate(): FileSpec
}
