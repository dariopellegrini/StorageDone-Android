package com.dariopellegrini.storagedone

import com.couchbase.lite.Expression
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Wrapper<T>(val base: T)

val StorageDoneDatabase.suspending: Wrapper<StorageDoneDatabase>
    get() = Wrapper(this)

suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insert(element: T) {
    withContext(Dispatchers.IO) {
        this@insert.base.insert(element)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insert(elements: List<T>) {
    withContext(Dispatchers.Main) {
        this@insert.base.insert(elements)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.get(): List<T> {
    return withContext(Dispatchers.IO) {
        return@withContext this@get.base.get<T>()
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.get(filter: Map<String, Any>): List<T> {
    return withContext(Dispatchers.IO) {
        return@withContext this@get.base.get<T>(filter)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.delete() {
    withContext(Dispatchers.IO) {
        this@delete.base.delete<T>()
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.delete(filter: Map<String, Any>) {
    withContext(Dispatchers.IO) {
        this@delete.base.delete<T>(filter)
    }
}
suspend inline fun <reified T: PrimaryKey>Wrapper<StorageDoneDatabase>.delete(element: T) {
    withContext(Dispatchers.IO) {
        this@delete.base.delete(element)
    }
}
suspend inline fun <reified T: PrimaryKey>Wrapper<StorageDoneDatabase>.delete(elements: List<T>) {
    withContext(Dispatchers.IO) {
        this@delete.base.delete(elements)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.delete(expression: Expression) {
    withContext(Dispatchers.IO) {
        this@delete.base.delete<T>(expression)
    }
}
