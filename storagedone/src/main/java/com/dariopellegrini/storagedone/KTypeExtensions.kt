package com.dariopellegrini.storagedone

import kotlin.reflect.KType

val KType.simpleName get() = simpleName(this.toString())

fun simpleName(typeString: String): String {
    return if (typeString.contains("<") && typeString.contains(">")) {
        val res = typeString.substringAfter("<").substringBeforeLast(">")
        typeString.substringBefore("<").split(".").last() + "<" + simpleName(res) + ">"
    } else {
        typeString.split(".").last()
    }
}