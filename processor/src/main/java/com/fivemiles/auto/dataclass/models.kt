package com.fivemiles.auto.dataclass

import com.google.auto.common.AnnotationMirrors
import com.google.auto.common.MoreElements
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import java.beans.Introspector
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

private const val CLASS_NAME_PREFIX = "DC"
private const val CLASS_NAME_SEPARATOR = "_"
private const val DATA_CLASS_NAME_SUFFIX = ""

private val ktComponentFunRegex = Regex("""^component\d+$""")

/**
 * Methods defined in Object or generated by kotlin
 */
private enum class DefaultMethod {
    NONE,
    // java methods
    TO_STRING,
    EQUALS, HASH_CODE, CLONE, CLASS,
    // kotlin methods
    COMPONENT,
    // android
    PARCELABLE,
}

/**
 * Represents a data class which should be generated.
 *
 * @property element the backing [TypeElement] of the data class definition
 */
internal data class DataClassDef(
    private val processingEnv: ProcessingEnvironment,
    val errorReporter: ErrorReporter,
    val element: TypeElement
) {

    val typeUtils: Types by lazy { processingEnv.typeUtils }
    private val elementUtils: Elements = processingEnv.elementUtils

    /**
     * [ClassName] of the data class to be generated
     */
    val className: ClassName by lazy {
        if (element.isInterfaceOrAbstractClass) {
            val simpleName = generatedClassName(element, DATA_CLASS_NAME_SUFFIX)
            element.findRootTypeElement().asClassName().peerClass(simpleName)
        } else {
            // it's a concrete class, there's no subclass to be generated
            element.asClassName()
        }
    }

    val dataClassAnnotation: AnnotationMirror? by lazy { annotation(element, DataClass::class.java) }

    val isGenerateGsonTypeAdapter: Boolean by lazy {
        dataClassAnnotation?.let {
            AnnotationMirrors.getAnnotationValue(it,
                DataClass::generateGsonTypeAdapter.name).value as Boolean
        } ?: false
    }

    /**
     * The set of [ExecutableElement]s (getters) represents the data properties
     */
    val propertyElements: Set<ExecutableElement> by lazy {
        propertyMethodsIn(element, MoreElements.getLocalAndInheritedMethods(element, typeUtils, elementUtils))
    }

    /**
     * The set of properties of the data class
     */
    val properties: Set<DataPropDef> by lazy {
        val allPrefixed = gettersAllPrefixed(propertyElements)
        val names = mutableSetOf<String>()
        propertyElements.map {
            val methodName = "${it.simpleName}"
            val name = if (allPrefixed) nameWithoutPrefix(methodName) else methodName
            if (name in names) {
                errorReporter.reportError("More than one @${DataClass::class.simpleName} property called $name", it)
            }
            names.add(name)
            DataPropDef(this@DataClassDef, name, it)
        }.toSet()
    }

    /**
     * The set of non-transient properties of the data class
     */
    val persistentProperties: Set<DataPropDef> by lazy {
        properties.filterNot(DataPropDef::isTransient).toSet()
    }

    private fun TypeElement.findRootTypeElement(): TypeElement =
        if (isNestedType) (enclosingElement as TypeElement).findRootTypeElement() else this

    private fun propertyMethodsIn(
        element: TypeElement,
        methods: Set<ExecutableElement>?
    ): Set<ExecutableElement> = methods?.filter {
        !it.isAnnotationPresent(NonDataProp::class.java.name) && // ignore non-property
            it.parameters.isEmpty() &&
            (element.isConcreteClass || Modifier.ABSTRACT in it.modifiers) && // allow concrete properties in Parcelized class
            it.returnType?.kind != TypeKind.VOID &&
            !hasDefaultImplement(element, it, typeUtils) &&
            objectMethodToOverride(it) === com.fivemiles.auto.dataclass.DefaultMethod.NONE
    }?.toSet() ?: setOf()

    private fun objectMethodToOverride(method: ExecutableElement): DefaultMethod {
        val name = method.simpleName.toString()
        val noParams = method.parameters.isEmpty()
        val isEqualsMethod = name == "equals" &&
            method.parameters.size == 1 &&
            "${method.parameters[0].asType()}" == "java.lang.Object"
        return when {
            noParams && name == "toString" -> DefaultMethod.TO_STRING
            noParams && name == "hashCode" -> DefaultMethod.HASH_CODE
            noParams && name == "clone" -> DefaultMethod.CLONE
            noParams && name == "getClass" -> DefaultMethod.CLASS
            noParams && name.matches(ktComponentFunRegex) -> DefaultMethod.COMPONENT
            noParams && name == "describeContents" -> DefaultMethod.PARCELABLE
            isEqualsMethod -> DefaultMethod.EQUALS
            else -> DefaultMethod.NONE
        }
    }
}

/**
 * Represents a property of the data class to be generated.
 *
 * @property dataClassDef the enclosing data class definition
 * @property name name of the property
 * @property element the backing [ExecutableElement] (getter method) of the property
 */
internal data class DataPropDef(
    val dataClassDef: DataClassDef,
    val name: String,
    val element: ExecutableElement
) {

    private val typeUtils: Types by lazy { dataClassDef.typeUtils }

    private val errorReporter: ErrorReporter by lazy { dataClassDef.errorReporter }

    private val enclosingElement: TypeElement by lazy { dataClassDef.element }

    /** Whether the property is mutable (has any setter method) */
    val isMutable: Boolean by lazy {
        enclosingElement.enclosedElements.any {
            it is ExecutableElement &&
                Modifier.ABSTRACT in it.modifiers &&
                it.parameters.size == 1 &&
                typeUtils.isSameType(it.parameters[0].asType(), element.returnType) &&
                it.simpleName.matches("""^(set)?$name$""".toRegex(RegexOption.IGNORE_CASE))
        }
    }

    /** Kotlin [TypeName] of the property */
    val typeKt: TypeName by lazy { parsePropertyType(element) }

    val typeMirror: TypeMirror by lazy { element.returnType }

    val dataPropAnnotation: AnnotationMirror? by lazy { annotation(element, DataProp::class.java) }

    val isTransient: Boolean by lazy {
        dataPropAnnotation?.let {
            AnnotationMirrors.getAnnotationValue(it,
                DataProp::isTransient.name).value as Boolean
        } ?: false
    }

    val defaultValueLiteral: String by lazy {
        dataPropAnnotation?.let {
            AnnotationMirrors.getAnnotationValue(it,
                DataProp::defaultValueLiteral.name).value as String
        } ?: ""
    }
}

/** Whether the given method has a default implement */
internal fun hasDefaultImplement(
    element: TypeElement,
    method: ExecutableElement,
    typeUtils: Types
): Boolean {
    val implCls = findDefaultImplement(element) ?: return false
    return implCls.enclosedElements.any {
        it is ExecutableElement &&
            Modifier.ABSTRACT !in it.modifiers &&
            it.simpleName == method.simpleName &&
            typeUtils.isSameType(it.returnType, method.returnType)
    }
}

internal fun annotation(element: Element, annotationClass: Class<out Annotation>): AnnotationMirror? =
    MoreElements.getAnnotationMirror(element, annotationClass).orNull()

/** Get the generated class name for the given element */
internal fun generatedClassName(element: TypeElement, suffix: String = ""): String =
    generatedClassName(generatedClassSimpleName(element), suffix = suffix)

private fun generatedClassSimpleName(element: Element): String =
    if (element.isNestedType)
        "${generatedClassSimpleName(element.enclosingElement)}_${element.simpleName}"
    else element.simpleName.toString()

private fun generatedClassName(
    baseName: String,
    prefix: String = CLASS_NAME_PREFIX,
    suffix: String = "",
    separator: String = CLASS_NAME_SEPARATOR
) = arrayOf(prefix, baseName, suffix)
    .filter(String::isNotEmpty)
    .joinToString(separator = separator)

private fun findDefaultImplement(element: TypeElement): TypeElement? =
    element.enclosedElements.find {
        it is TypeElement && "${it.simpleName}" == "DefaultImpls"
    } as TypeElement?

/** whether all the getters start with `get`, `is` */
private fun gettersAllPrefixed(methods: Set<ExecutableElement>) = prefixedGettersIn(methods).size == methods.size

private fun prefixedGettersIn(methods: Iterable<ExecutableElement>): Set<ExecutableElement> {
    return methods.filter {
        val name = "${it.simpleName}"
        // `getfoo` or `isfoo` (without a capital) is a getter currently
        name.matches("""^get.+""".toRegex()) ||
            (name.matches("""^is.+""".toRegex()) && it.returnType.kind == TypeKind.BOOLEAN)
    }.toSet()
}

/**
 * Returns the name of the property defined by the given getter. A getter called `getFoo()`
 * or `isFoo()` defines a property called `foo`. For consistency with JavaBeans, a
 * getter called `getHTMLPage()` defines a property called `HTMLPage`. The
 * [rule](https://docs.oracle.com/javase/8/docs/api/java/beans/Introspector.html#decapitalize-java.lang.String-) is: the name of the property is the part after `get` or `is`, with the
 * first letter lowercased *unless* the first two letters are uppercase. This works well
 * for the `HTMLPage` example, but in these more enlightened times we use `HtmlPage`
 * anyway, so the special behaviour is not useful, and of course it behaves poorly with examples like `OAuth`.
 */
private fun nameWithoutPrefix(methodName: String): String {
    val name = when {
        methodName.startsWith("get") -> methodName.substring(3)
        methodName.startsWith("is") -> methodName.substring(2)
        else -> throw IllegalArgumentException("")
    }
    return Introspector.decapitalize(name)
}
