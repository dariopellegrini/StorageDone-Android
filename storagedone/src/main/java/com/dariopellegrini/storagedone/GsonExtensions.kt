package com.dariopellegrini.storagedone

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Throws(Exception::class)
// reified: pass to have that class type
inline fun <reified T> Gson.fromJson(jsonString: String): T {
    return this.fromJson<T>(jsonString, object: TypeToken<T>() {}.type)
}

@Throws(Exception::class)
fun <T>Gson.toJSONMap(o: T): Map<String, Any> {
    val jsonString = this.toJson(o)
    return Gson().fromJson(jsonString)
}