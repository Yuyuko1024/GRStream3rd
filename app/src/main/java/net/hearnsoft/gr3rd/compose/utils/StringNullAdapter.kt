package net.hearnsoft.gr3rd.compose.utils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class StringNullAdapter : TypeAdapter<String>() {

    override fun read(reader: JsonReader): String =
        reader.takeIf { it.peek() != JsonToken.NULL }
            ?.nextString()
            ?: run {
                reader.nextNull()
                ""
            }

    override fun write(writer: JsonWriter, value: String?) {
        writer.apply {
            if (value != null) value(value) else nullValue()
        }
    }
}