package com.fivemiles.auto.dataclass

/* ktlint-disable no-wildcard-imports */
import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.nio.charset.Charset
import javax.tools.JavaFileObject
import javax.tools.StandardLocation.SOURCE_OUTPUT

/**
 * Data class generation tests
 *
 * Created by ywu on 2017/7/6.
 */
@Suppress("UNUSED_VARIABLE")
class DataClassStepTest {

    private lateinit var dataClassSource: JavaFileObject

    @Before
    fun setup() {
        dataClassSource = JavaFileObjects.forSourceString("com.fivemiles.auto.dataclass.DataClass", """
            |package com.fivemiles.auto.dataclass;
            |@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.SOURCE)
            |@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE)
            |public @interface DataClass { }
            |""".trimMargin())
    }

    @Ignore("Google compile-testing doesn't work with kotlin")
    @Test fun simpleJavaSource() {
        val nullableStringType = String::class.asClassName().asNullable()
//        val source = KotlinFile.builder("test", "Test")
//                .addType(TypeSpec.interfaceBuilder("Test")
//                        .addAnnotation(DataClass::class)
//                        .addProperty("street", nullableStringType)
//                        .addProperty("city", String::class)
//                        .build())
//                .build()
//                .toJavaFileObject()
        val source = JavaFileObjects.forSourceString("test.Test", """
            |package test;
            |import com.fivemiles.auto.dataclass.DataClass;
            |import org.jetbrains.annotations.Nullable;
            |
            |@DataClass public interface Test {
            |   @Nullable String getStreet();
            |   String getCity();
            |}
            |""".trimMargin())

        val expected = FileSpec.builder("test", "DC_Test")
            .addType(TypeSpec.classBuilder("DC_Test")
                .addModifiers(KModifier.INTERNAL, KModifier.DATA)
                .addProperty(PropertySpec.builder("street", nullableStringType)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("street")
                    .build())
                .addProperty(PropertySpec.builder("city", String::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("city")
                    .build())
                .primaryConstructor(FunSpec.constructorBuilder()
                    .addParameter("street", nullableStringType)
                    .addParameter("city", String::class)
                    .build())
                .build())
            .build()
            .toJavaFileObject()

        assertAbout(javaSources())
            .that(listOf(source))
            .withCompilerOptions()
            .processedWith(DataClassAnnotationProcessor())
            .compilesWithoutError()
            .and()
//                .generatesSources(expected)
            .generatesFileNamed(SOURCE_OUTPUT, "test", "DC_Test.kt")
            .withStringContents(Charset.forName("UTF-8"), "hello")
    }
}
