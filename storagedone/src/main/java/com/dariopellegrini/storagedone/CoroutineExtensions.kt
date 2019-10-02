package com.dariopellegrini.storagedone

import com.couchbase.lite.Expression
import com.couchbase.lite.Ordering
import com.dariopellegrini.storagedone.query.AdvancedQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Wrapper<T>(val base: T)

val StorageDoneDatabase.suspending: Wrapper<StorageDoneDatabase>
    get() = Wrapper(this)

suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insert(element: T) {
    withContext(Dispatchers.IO) {
        base.insert(element)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insert(elements: List<T>) {
    withContext(Dispatchers.Main) {
        base.insert(elements)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insertOrUpdate(element: T) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(element)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insertOrUpdate(elements: List<T>) {
    withContext(Dispatchers.Main) {
        base.insertOrUpdate(elements)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insertOrUpdate(elements: Array<T>) {
    withContext(Dispatchers.Main) {
        base.insertOrUpdate(elements)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.get(): List<T> {
    return withContext(Dispatchers.IO) {
        base.get<T>()
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.get(vararg orderings: Ordering): List<T> {
    return withContext(Dispatchers.IO) {
        base.get<T>(*orderings)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.get(filter: Map<String, Any>): List<T> {
    return withContext(Dispatchers.IO) {
        base.get<T>(filter)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.get(expression: Expression, vararg orderings: Ordering): List<T> {
    return withContext(Dispatchers.IO) {
        base.get<T>(expression, *orderings)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.get(crossinline buildQuery: AdvancedQuery.() -> Unit): List<T> {
    return withContext(Dispatchers.IO) {
        base.get<T>(buildQuery)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.delete() {
    withContext(Dispatchers.IO) {
        base.delete<T>()
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.delete(filter: Map<String, Any>) {
    withContext(Dispatchers.IO) {
        base.delete<T>(filter)
    }
}
suspend inline fun <reified T: PrimaryKey>Wrapper<StorageDoneDatabase>.delete(element: T) {
    withContext(Dispatchers.IO) {
        base.delete(element)
    }
}
suspend inline fun <reified T: PrimaryKey>Wrapper<StorageDoneDatabase>.delete(elements: List<T>) {
    withContext(Dispatchers.IO) {
        base.delete(elements)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.delete(expression: Expression) {
    withContext(Dispatchers.IO) {
        base.delete<T>(expression)
    }
}
