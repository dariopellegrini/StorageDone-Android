package com.dariopellegrini.storagedone

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.couchbase.lite.Expression
import com.couchbase.lite.Ordering
import com.dariopellegrini.storagedone.query.AdvancedQuery
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

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
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insertOrUpdate(element: T, useExistingValuesAsFallback: Boolean = false) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(element, useExistingValuesAsFallback)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insertOrUpdate(elements: List<T>, useExistingValuesAsFallback: Boolean = false) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(elements, useExistingValuesAsFallback)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.insertOrUpdate(elements: Array<T>, useExistingValuesAsFallback: Boolean = false) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(elements, useExistingValuesAsFallback)
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
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.upsert(element: T, useExistingValuesAsFallback: Boolean = false) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(element, useExistingValuesAsFallback)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.upsert(elements: List<T>, useExistingValuesAsFallback: Boolean = false) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(elements, useExistingValuesAsFallback)
    }
}
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.upsert(elements: Array<T>, useExistingValuesAsFallback: Boolean = false) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(elements, useExistingValuesAsFallback)
    }
}
suspend inline fun <reified T: PrimaryKey>Wrapper<StorageDoneDatabase>.upsert(element: T, expression: Expression) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(element, expression)
    }
}
suspend inline fun <reified T: PrimaryKey>Wrapper<StorageDoneDatabase>.upsert(element: T, crossinline onlyIf: (T) -> Expression) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(element, onlyIf)
    }
}
suspend inline fun <reified T: PrimaryKey>Wrapper<StorageDoneDatabase>.upsert(elements: List<T>, expression: Expression) {
    withContext(Dispatchers.IO) {
        base.insertOrUpdate(elements, expression)
    }
}
suspend inline fun <reified T: PrimaryKey>Wrapper<StorageDoneDatabase>.upsert(elements: List<T>, crossinline onlyIf: (T) -> Expression) {
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
suspend fun Wrapper<StorageDoneDatabase>.purgeDeletedDocuments() {
    withContext(Dispatchers.IO) {
        base.purgeDeletedDocuments()
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
inline fun <reified T>Wrapper<StorageDoneDatabase>.live() = callbackFlow {
        val query = base.live<T> {
            trySend(it)
        }
        awaitClose {
            query.cancel()
        }
    }

@ExperimentalCoroutinesApi
@FlowPreview
inline fun <reified T>Wrapper<StorageDoneDatabase>.live(vararg orderings: Ordering) = callbackFlow {
    val query = base.live<T>(*orderings) {
        trySend(it)
    }
    awaitClose {
        query.cancel()
    }
}


@ExperimentalCoroutinesApi
@FlowPreview
inline fun <reified T>Wrapper<StorageDoneDatabase>.live(expression: Expression, vararg orderings: Ordering) = callbackFlow {
    val query = base.live<T>(expression, *orderings) {
        trySend(it)
    }
    awaitClose {
        query.cancel()
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
inline fun <reified T>Wrapper<StorageDoneDatabase>.live(crossinline buildQuery: AdvancedQuery.() -> Unit) = callbackFlow {
    val query = base.live<T>(buildQuery) {
        trySend(it)
    }
    awaitClose {
        query.cancel()
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
inline fun <reified T>Wrapper<StorageDoneDatabase>.bind(channel: ConflatedBroadcastChannel<List<T>>): Flow<List<T>> {
    return live<T>().onEach {
        channel.send(it)
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.bindCollect(channel: ConflatedBroadcastChannel<List<T>>) {
    live<T>().collect {
        channel.send(it)
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
inline fun <reified T>Wrapper<StorageDoneDatabase>.bind(channel: ConflatedBroadcastChannel<List<T>>,
                                                        crossinline buildQuery: AdvancedQuery.() -> Unit): Flow<List<T>> {
    return live<T>().onEach {
        channel.send(it)
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
suspend inline fun <reified T>Wrapper<StorageDoneDatabase>.bindCollect(channel: ConflatedBroadcastChannel<List<T>>,
                                                                       crossinline buildQuery: AdvancedQuery.() -> Unit) {
    live<T>(buildQuery).collect {
        channel.send(it)
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
suspend inline fun <reified T>StorageDoneDatabase.channel(): Pair<ConflatedBroadcastChannel<List<T>>, Flow<List<T>>> {
    val channel = ConflatedBroadcastChannel<List<T>>()
    val flow = suspending.live<T>().onEach {
        channel.send(it)
    }
    return channel to flow
}
