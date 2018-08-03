[![Build Status](https://travis-ci.org/xinthink/auto-data-class.svg?branch=master)](https://travis-ci.org/xinthink/auto-data-class)
[ ![Download](https://api.bintray.com/packages/xinthink/maven/auto-data-class-processor/images/download.svg) ](https://bintray.com/xinthink/maven/auto-data-class-processor/_latestVersion)

# auto-data-class
An annotation processor generates [Kotlin Data Classes] and the boilerplates for Parcelable & GSON TypeAdapter. Inspired by [AutoValue] and its popular extensions [auto-value-parcel] & [auto-value-gson].

## Usage
Declare your data model as an interface/abstract class, and annotate it with `@DataClass`

```kotlin
@DataClass interface Address : Parcelable {
    val street: String?
    val city: String
}
```

Since version `0.6.0`, you can make things even simpler with [`@Parcelize`][kt-1.14-release-note]. You can now define data classes directly, to make the most out of Data Class, and leave the `Parcelable` stuff to the Kotlin compiler. The `@DataClass` processor will just generate a Gson `TypeAdapter` for such a class.

```kotlin
@Parcelize @DataClass data class Address(
    val street: String?,
    val city: String
) : Parcelable
```

Now build the project, a data class will be generated, with all the boilerplates needed to implement `Parcelable` & Gson `TypeAdapter`.

```kotlin
internal data class DC_Address(
    override val street: String?,
    override val city: String
) : Address {

    override fun writeToParcel(dest: Parcel, flags: Int)
    ...

    class GsonTypeAdapter(gson: Gson) : TypeAdapter<Address>()
    ...
    companion object {
        val CREATOR: Parcelable.Creator<DC_Address>
        ...
    }
}
```

Just like how you'll use [AutoValue], it's convenient to write factory methods or derived properties to access the generated code.

```kotlin
@DataClass interface Address {
...
    /** derived properties */
    val fullAddress: String
        get() = if (street != null) "$street, $city" else city

    companion object {
        /** factory method */
        fun create(street: String?, city: String): Address = DC_Address(street, city)

        /** Gson TypeAdapter factory method */
        fun typeAdapter(gson: Gson): TypeAdapter<Address> =
            DC_Address.GsonTypeAdapter(gson)
                .apply {
                    // if needed, you can set default values for the omission of the json fields
                    defaultCity = "Beijing"
                    defaultStreet = "Unknown"
                }
    }
}
```

Furthermore, you can customize the generated code with the `@DataProp` annotation.

```kotlin
@DataClass interface Address {
    @get:DataProp("street",
        jsonFieldAlternate = arrayOf("street1", "street2"),
        defaultValueLiteral = """"string literal""""
    )
    val street: String?
    ...
}
```

A `TypeAdapterFactory` can also be generated, which can be used to setup the `Gson` instance. All you have to do is annotating an object/interface/class with `@GsonTypeAdapterFactory`.

```kotlin
// Using objects, you can also use interfaces or abstract classes.
@GsonTypeAdapterFactory object MyTypeAdapterFactory {
    fun create(): TypeAdapterFactory = DC_MyTypeAdapterFactory()
}
```

So that you can build a `Gson` instance like this:

```kotlin
GsonBuilder()
    .registerTypeAdapterFactory(MyTypeAdapterFactory.create())
    .create()
```

See the [test cases][example-tests] for more details.

## Integration
Using the [kotlin-kapt] plugin

```gradle
kapt 'com.fivemiles.auto:auto-data-class-processor:0.6.1'
compile 'com.fivemiles.auto:auto-data-class-lib:0.6.1'

# for testing, optional
kaptTest 'com.fivemiles.auto:auto-data-class-processor:0.6.1'
kaptAndroidTest 'com.fivemiles.auto:auto-data-class-processor:0.6.1'
```

## Limitations
The lib is still at its early stage, there's some limitations you should know.

### Unsupported Data Types
The following data types are not supported:
- `kotlin.Array`, please consider using `List` or `Set` instead
- Nullable type parameters, such as `List<String?>`

Not all parcelable data types are supported, for example, `android.util.SparseArray`, `android.os.Bundle` has no built-in support for now, please use `ParcelAdapter` if these unsupported types are mandatory. You can also shoot me a PR, of cause. :beer:

See this [test case][example-parcel-types] for more details.

### Overriding Built-in Methods
If you're using interfaces or abstract classes, because of the nature of Kotlin [interface][Kotlin Interfaces] and [data class][Kotlin Data Classes], it will be a little difficult to override built-in methods such as `toString`, `hashCode`.

There's several ways to handle such a situation.

First, avoid using `Parcelable` if it's not required or resort to `@Parcelize`, so that you can define data classes directly, in which method overriding will not be a problem.

Otherwise, prefer [extension functions][Kotlin Extensions] whenever possible.

```kotlin
@DataClass interface Address {
...
    companion object {
        fun Address.alternateToString(): String = ...
    }
}
```

Or you will have to create a wrapper class which overrides the built-in methods, and [delegates][Kotlin Delegation] all the others to the generated data class.

```kotlin
/** The data class definition, internal usage only. */
@DataClass internal interface PersonInternal {
    val name: String
    val age: Int
}

/** The public wrapper of the data class, in which you can rewrite the built-in methods. */
class Person
private constructor(p: PersonInternal) : PersonInternal by p {

    override fun toString(): String {
        return "My name is $name, I'm $age years old."
    }

    companion object {
        fun create(name: String, age: Int): Person = Person(DC_PersonInternal(name, age))
    }
}
```

See this [test case][example-overriding] for more details.

## License

    Copyright 2017 yingxinwu.g@gmail.com.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[Kotlin Data Classes]: https://kotlinlang.org/docs/reference/data-classes.html
[Kotlin Interfaces]: https://kotlinlang.org/docs/reference/interfaces.html
[Kotlin Delegation]: https://kotlinlang.org/docs/reference/delegation.html
[Kotlin Extensions]: https://kotlinlang.org/docs/reference/extensions.html
[kotlin-kapt]: https://kotlinlang.org/docs/reference/kapt.html
[AutoValue]: https://github.com/google/auto
[auto-value-parcel]: https://github.com/rharter/auto-value-parcel
[auto-value-gson]: https://github.com/rharter/auto-value-gson
[example-tests]: https://github.com/xinthink/auto-data-class/tree/master/example/src/test/java/com/fivemiles/auto/dataclass
[example-parcel-types]: https://github.com/xinthink/auto-data-class/blob/master/example/src/test/java/com/fivemiles/auto/dataclass/parcel/ParcelableTypesTest.kt
[example-overriding]: https://github.com/xinthink/auto-data-class/blob/master/example/src/test/java/com/fivemiles/auto/dataclass/OverridingTest.kt
[kt-1.14-release-note]: https://blog.jetbrains.com/kotlin/2017/08/kotlin-1-1-4-is-out/
