package com.dariopellegrini.storagedone

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Wrapper<T>(val base: T)

val StorageDoneDatabase.suspending: Wrapper<StorageDoneDatabase>
    get() = Wrapper(this)

inline suspend fun <reified T>Wrapper<StorageDoneDatabase>.insert(element: T) {
    withContext(Dispatchers.IO) {
        this@insert.base.insert(element)
    }
}
inline suspend fun <reified T>Wrapper<StorageDoneDatabase>.insert(elements: List<T>) {
    withContext(Dispatchers.Main) {
        this@insert.base.insert(elements)
    }
}
inline suspend fun <reified T>Wrapper<StorageDoneDatabase>.get(): List<T> {
    return withContext(Dispatchers.IO) {
        return@withContext this@get.base.get<T>()
    }
}
inline suspend fun <reified T>Wrapper<StorageDoneDatabase>.get(filter: Map<String, Any>): List<T> {
    return withContext(Dispatchers.IO) {
        return@withContext this@get.base.get<T>(filter)
    }
}
inline suspend fun <reified T>Wrapper<StorageDoneDatabase>.delete() {
    withContext(Dispatchers.IO) {
        this@delete.base.delete<T>()
    }
}

inline suspend fun <reified T>Wrapper<StorageDoneDatabase>.delete(filter: Map<String, Any>) {
    withContext(Dispatchers.IO) {
        this@delete.base.delete<T>(filter)
    }
}