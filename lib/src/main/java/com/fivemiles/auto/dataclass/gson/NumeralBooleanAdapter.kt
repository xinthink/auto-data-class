package com.fivemiles.auto.dataclass.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter


/**
 * A [TypeAdapter] for Boolean values serialized as Integers.
 */
class NumeralBooleanAdapter : TypeAdapter<Boolean>() {

    override fun write(writer: JsonWriter, value: Boolean?) {
        if (value == null) writer.nullValue()
        else writer.value(if (value) 1 else 0)
    }

    override fun read(reader: JsonReader): Boolean? {
        return when (reader.peek()) {
            JsonToken.NUMBER ->
                reader.nextInt() == 1
            JsonToken.BOOLEAN ->
                reader.nextBoolean()
            JsonToken.STRING ->
                reader.nextString().toBoolean()
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}
