package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.squareup.kotlinpoet.FileSpec

interface FileGenerator {
    fun generate(): FileSpec
}
