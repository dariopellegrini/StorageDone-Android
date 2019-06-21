package com.dariopellegrini.storagedone

import com.google.gson.Gson
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.util.*

@Throws(Exception::class)
// reified: pass to have that class type
inline fun <reified T> Gson.fromJson(jsonString: String): T {
    return this.fromJson<T>(jsonString, object: TypeToken<T>() {}.type)
}

@Throws(Exception::class)
fun <T>Gson.toJSONMap(o: T): Map<String, Any> {
    val jsonString = this.toJson(o)
    return this.fromJson(jsonString)
}

var ser: JsonSerializer<Date> =
    JsonSerializer { src, typeOfSrc, context -> if (src == null) null else JsonPrimitive(src.time) }

var deser: JsonDeserializer<Date> = JsonDeserializer<Date> { json, typeOfT, context ->
    if (json == null) null else Date(json.asLong)
}