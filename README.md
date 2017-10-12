[![Build Status](https://travis-ci.org/xinthink/auto-data-class.svg?branch=master)](https://travis-ci.org/xinthink/auto-data-class)

# auto-data-class
An annotation processor generates [Kotlin Data Classes] and the boilerplates for Parcelable & GSON TypeAdapter. Inspired by [AutoValue] and its popular extensions [auto-value-parcel] & [auto-value-gson].

## Usage
Declare your data model as an interface, and annotate it with `@DataClass`

```kotlin
@DataClass interface Address {
    val street: String?
    val city: String
}
```

Now build the project, a data class implementing this interface will be generated, also the parcelable & TypeAdapter codes.

```kotlin
internal data class DC_Address(
    override val street: String?, 
    override val city: String
) : Address, Parcelable {
    
    class GsonTypeAdapter(gson: Gson) : TypeAdapter<Address>() {
        ...
    }
    ...
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
                    // setting default values for the omission of the json fields (it's not required)
                    defaultCity = "Beijing"
                    defaultStreet = "Unknown"
                }
    }
}
```

Furthermore, you can customize the generated code with the `@DataProp` annotation.

```kotlin
@DataClass interface Address {
    val street: String?
        @DataProp(
                jsonField = "street", 
                jsonFieldAlternate = arrayOf("street1", "street2"), 
                defaultValueLiteral = """"string literal""""
        )
        get
    
    ...
}
```

See test cases of the `example` module for more details.

## Integration
The work is still in progress, so it's not published to any repository yet, you have to build it from source.
- checkout the repo
- setup gradle scripts
- kapt

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
[AutoValue]: https://github.com/google/auto
[auto-value-parcel]: https://github.com/rharter/auto-value-parcel
[auto-value-gson]: https://github.com/rharter/auto-value-gson
