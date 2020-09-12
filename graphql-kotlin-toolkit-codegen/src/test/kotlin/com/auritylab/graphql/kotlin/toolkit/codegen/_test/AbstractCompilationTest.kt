package com.auritylab.graphql.kotlin.toolkit.codegen._test

import com.auritylab.graphql.kotlin.toolkit.codegen.generator.FileGenerator
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.reflect.KClass

abstract class AbstractCompilationTest(
    private val createInterfaceMocks: Boolean = false
) {
    protected open fun compile(generator: FileGenerator) = internalCompile(generator.generate())
    protected open fun compile(fileSpec: FileSpec) = internalCompile(fileSpec)
    protected open fun compile(main: FileSpec, vararg dependencies: FileSpec) = internalCompile(main, *dependencies)
    protected open fun compile(main: FileGenerator, vararg dependencies: FileGenerator) =
        internalCompile(main.generate(), *dependencies.map { it.generate() }.toTypedArray())

    /**
     * Will compile the given [main] and the given [dependencies]. This function will return the runtime reflection
     * reference to the [main] class. The [dependencies] will just be added to the compilation.
     */
    private fun internalCompile(main: FileSpec, vararg dependencies: FileSpec): Result {
        // Generate source files for the given file specs.
        val mainSource = buildSourceFile(main, 0)
        val dependencySources = dependencies.mapIndexed() { index, dep -> buildSourceFile(dep, index + 1) }

        // Configure the compilation and run the compiler.
        val result = KotlinCompilation().apply {
            // / Add the generated sources to the compilation.
            sources = listOf(mainSource, *dependencySources.toTypedArray())
            inheritClassPath = true
        }.compile()

        // Throw an exception if the compilation exited with an unsuccessful exit code.
        if (result.exitCode != KotlinCompilation.ExitCode.OK)
            throw IllegalStateException("Kotlin compilation not successful!")

        // Load the given main class using the ClassLoader.
        return Result(result.classLoader.loadClass(main.packageName + "." + main.name).kotlin, result.classLoader)
    }

    /**
     * Will build the [SourceFile] based on the given [spec].
     * This will use the name of the class and append the ".kt" extension.
     */
    private fun buildSourceFile(spec: FileSpec, counter: Int): SourceFile {
        val file: FileSpec = if (createInterfaceMocks) {
            // Search on the members of the given file spec for types of kind interface.
            val possibleInterfaces = spec.members
                .filterIsInstance<TypeSpec>()
                .filter { it.kind == TypeSpec.Kind.INTERFACE }

            val builder = spec.toBuilder()

            possibleInterfaces
                .forEach { type -> builder.addType(buildMockImplementation(spec, type)) }

            builder.build()
        } else
            spec

        return SourceFile.kotlin(spec.name + counter + ".kt", file.toString())
    }

    private fun buildMockImplementation(file: FileSpec, spec: TypeSpec): TypeSpec {
        val classToMock = ClassName(file.packageName, spec.name!!)

        return TypeSpec.classBuilder(spec.name!! + "Mock")
            .addSuperinterface(classToMock)
            .addFunctions(
                spec.funSpecs
                    .filter { KModifier.ABSTRACT in it.modifiers }
                    .map {
                        it.toBuilder()
                            .addModifiers(KModifier.OVERRIDE)
                            .addCode("TODO()")
                            .also { t -> t.modifiers.remove(KModifier.ABSTRACT) }
                            .build()
                    }
            ).build()
    }

    data class Result(
        val main: KClass<*>,
        val classLoader: ClassLoader
    )
}
