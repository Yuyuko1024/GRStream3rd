package net.hearnsoft.gr3rd.compose.utils

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken

class NullStringToEmptyAdapterFactory : TypeAdapterFactory {

    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? =
        if (type.rawType == String::class.java) {
            @Suppress("UNCHECKED_CAST")
            StringNullAdapter() as TypeAdapter<T>
        } else null
}