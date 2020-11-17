package com.dariopellegrini.storagedone

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.couchbase.lite.Expression
import com.couchbase.lite.Ordering
import com.dariopellegrini.storagedone.query.AdvancedQuery
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

class Wrapper<T>(val base: T)

val StorageDoneDatabase.suspending: Wrapper<StorageDoneDatabase>
    get() = Wrapper(this)

suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insert(element: T) {
    withContext(Dispatchers.IO) {
        base.insert(element)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insert(elements: List<T>) {
    withContext(Dispatchers.IO) {
        base.insert(elements)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insertOrUpdate(element: T) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(element)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insertOrUpdate(elements: List<T>) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(elements)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insertOrUpdate(elements: Array<T>) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(elements)
    }
}
suspend inline fun <reified T: PrimaryKey>Wrapper<StorageDoneDatabase>.insertOrUpdate(element: T, expression: Expression) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(element, expression)
    }
}
suspend inline fun <reified T: PrimaryKey>Wrapper<StorageDoneDatabase>.insertOrUpdate(element: T, crossinline onlyIf: (T) -> Expression) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(element, onlyIf)
    }
}
suspend inline fun <reified T: PrimaryKey>Wrapper<StorageDoneDatabase>.insertOrUpdate(elements: List<T>, expression: Expression) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(elements, expression)
    }
}
suspend inline fun <reified T: PrimaryKey>Wrapper<StorageDoneDatabase>.insertOrUpdate(elements: List<T>, crossinline onlyIf: (T) -> Expression) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(elements, onlyIf)
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
suspend inline fun Wrapper<StorageDoneDatabase>.clear() {
    withContext(Dispatchers.IO) {
        base.clear()
    }
}

// Flow
@ExperimentalCoroutinesApi
@FlowPreview
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.live() = callbackFlow {
        val query = base.live<T> {
            offer(it)
        }
        awaitClose {
            query.cancel()
        }
    }

@ExperimentalCoroutinesApi
@FlowPreview
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.live(vararg orderings: Ordering) = callbackFlow {
    val query = base.live<T>(*orderings) {
        offer(it)
    }
    awaitClose {
        query.cancel()
    }
}


@ExperimentalCoroutinesApi
@FlowPreview
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.live(expression: Expression, vararg orderings: Ordering) = callbackFlow {
    val query = base.live<T>(expression, *orderings) {
        offer(it)
    }
    awaitClose {
        query.cancel()
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.live(crossinline buildQuery: AdvancedQuery.() -> Unit) = callbackFlow {
    val query = base.live<T>(buildQuery) {
        offer(it)
    }
    awaitClose {
        query.cancel()
    }
}
