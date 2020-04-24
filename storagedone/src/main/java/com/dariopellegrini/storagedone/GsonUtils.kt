package com.dariopellegrini.storagedone

import com.google.gson.*
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
    JsonSerializer { src, _, _ -> if (src == null) null else JsonPrimitive(src.time) }

var deser: JsonDeserializer<Date> = JsonDeserializer<Date> { json, _, _ ->
    if (json == null) null else Date(json.asLong)
}

val byteArrayExclusionStrategy = object : ExclusionStrategy {

    override fun shouldSkipClass(arg0: Class<*>): Boolean {
        return false
    }

    override fun shouldSkipField(f: FieldAttributes): Boolean {
        return f.declaredClass == ByteArray::class.java // || f.name == "bytes"
    }

}