package com.example.appforcooking.data.server

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class BooleanTypeAdapter : TypeAdapter<Boolean>() {
    override fun write(out: JsonWriter, value: Boolean?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(if (value) 1 else 0)
        }
    }

    override fun read(reader: JsonReader): Boolean {
        return when (reader.peek()) {
            JsonToken.BOOLEAN -> reader.nextBoolean()
            JsonToken.NUMBER -> reader.nextInt() == 1
            JsonToken.STRING -> reader.nextString().toIntOrNull() == 1
            else -> false
        }
    }
}