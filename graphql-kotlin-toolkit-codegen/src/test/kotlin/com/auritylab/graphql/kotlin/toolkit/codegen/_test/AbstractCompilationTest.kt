package com.auritylab.graphql.kotlin.toolkit.codegen._test

import com.auritylab.graphql.kotlin.toolkit.codegen.generator.FileGenerator
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.reflect.KClass

abstract class AbstractCompilationTest {

    protected open fun compile(generator: FileGenerator): KClass<*> {
        // Generate the code of the generator.
        val fileSpec = generator.generate()

        // Create the source files
        val source = SourceFile.kotlin("Generated.kt", fileSpec.toString())

        val compilation = KotlinCompilation()

        compilation.sources = listOf(source)
        compilation.inheritClassPath = false

        val compileResult = compilation.compile()

        return compileResult.classLoader.loadClass(fileSpec.packageName + "." + fileSpec.name).kotlin
    }
}
