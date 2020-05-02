package com.auritylab.graphql.kotlin.toolkit.codegen._test

import com.auritylab.graphql.kotlin.toolkit.codegen.generator.FileGenerator
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.reflect.KClass

abstract class AbstractMockCompilationTest : AbstractCompilationTest() {
    override fun compile(generator: FileGenerator): KClass<*> {
        val baseFileSpec = generator.generate()

        val firstTypeSpec = baseFileSpec.members.filterIsInstance<TypeSpec>().firstOrNull()
            ?: return super.compile(generator)

        val mockImplementation = createImplementation(baseFileSpec, firstTypeSpec)

        val generatedSource = SourceFile.kotlin("Generated.kt", baseFileSpec.toString())
        val mockSource = SourceFile.kotlin("Mock.kt", mockImplementation.toString())

        val compilation = KotlinCompilation()

        compilation.sources = listOf(generatedSource, mockSource)
        compilation.inheritClassPath = true

        val compileResult = compilation.compile()

        return compileResult.classLoader.loadClass(baseFileSpec.packageName + "." + baseFileSpec.name).kotlin
    }

    private fun createImplementation(file: FileSpec, type: TypeSpec): FileSpec {
        val classToMock = ClassName(file.packageName, type.name!!)

        return FileSpec.builder(TestObject.options.generatedBasePackage, "MockImplementation")
            .addType(
                TypeSpec.classBuilder("MockImplementation")
                    .addSuperinterface(classToMock)
                    .addFunctions(type.funSpecs
                        .filter { KModifier.ABSTRACT in it.modifiers }
                        .map {
                            it.toBuilder()
                                .addModifiers(KModifier.OVERRIDE)
                                .addCode("TODO()")
                                .also { t -> t.modifiers.remove(KModifier.ABSTRACT) }
                                .build()
                        })
                    .build()
            )
            .build()
    }
}
