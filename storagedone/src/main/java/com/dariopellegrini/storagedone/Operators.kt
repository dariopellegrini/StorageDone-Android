package com.dariopellegrini.storagedone

import android.util.Log

inline operator fun <reified T>StorageDoneDatabase.plusAssign(elements: List<T>) {
    try {
        this.insertOrUpdate(elements)
    } catch (e: Exception) {
        Log.e("StorageDone", e.localizedMessage)
    }
}
inline infix fun <reified T>StorageDoneDatabase.insertInto(list: MutableList<T>){
    try {
        list.addAll(this.get())
    } catch (e: Exception) {
        Log.e("StorageDone", e.localizedMessage)
    }
}
inline infix fun <reified T>StorageDoneDatabase.filter(filter: Map<String, Any>): List<T> {
    return try {
        this.get(filter)
    } catch (e: Exception) {
        Log.e("StorageDone", e.localizedMessage)
        emptyList()
    }
}
inline fun <reified T>StorageDoneDatabase.all(): List<T> {
    return try {
        this.get()
    } catch (e: Exception) {
        Log.e("StorageDone", e.localizedMessage)
        emptyList()
    }
}
