package com.dariopellegrini.storagedone

import android.util.Log
import com.couchbase.lite.Expression
import com.dariopellegrini.storagedone.query.AdvancedQuery

inline operator fun <reified T>StorageDoneDatabase.plusAssign(elements: List<T>) {
    try {
        this.insertOrUpdate(elements)
    } catch (e: Exception) {
        Log.e("StorageDone", e.localizedMessage ?: "")
    }
}
inline infix fun <reified T>StorageDoneDatabase.insertInto(list: MutableList<T>){
    try {
        list.addAll(this.get())
    } catch (e: Exception) {
        Log.e("StorageDone", e.localizedMessage ?: "")
    }
}
inline infix fun <reified T>StorageDoneDatabase.filter(filter: Map<String, Any>): List<T> {
    return try {
        this.get(filter)
    } catch (e: Exception) {
        Log.e("StorageDone", e.localizedMessage ?: "")
        emptyList()
    }
}
inline infix fun <reified T>StorageDoneDatabase.filter(expression: Expression): List<T> {
    return try {
        this.get(expression)
    } catch (e: Exception) {
        Log.e("StorageDone", e.localizedMessage ?: "")
        emptyList()
    }
}
inline infix fun <reified T>StorageDoneDatabase.filter(buildQuery: AdvancedQuery.() -> Unit): List<T> {
    return try {
        this.get(buildQuery)
    } catch (e: Exception) {
        Log.e("StorageDone", e.localizedMessage ?: "")
        emptyList()
    }
}
inline fun <reified T>StorageDoneDatabase.all(): List<T> {
    return try {
        this.get()
    } catch (e: Exception) {
        Log.e("StorageDone", e.localizedMessage ?: "")
        emptyList()
    }
}
inline operator fun <reified T: PrimaryKey>StorageDoneDatabase.minusAssign(elements: List<T>) {
    try {
        this.delete(elements)
    } catch (e: Exception) {
        Log.e("StorageDone", e.localizedMessage ?: "")
    }
}

inline infix fun <reified T>StorageDoneDatabase.remove(expression: Expression) {
    try {
        this.delete<T>(expression)
    } catch (e: Exception) {
        Log.e("StorageDone", e.localizedMessage ?: "")
    }
}
