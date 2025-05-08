package com.dariopellegrini.storagedone

import com.couchbase.lite.DataSource
import com.couchbase.lite.Dictionary
import com.couchbase.lite.Expression
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import kotlin.reflect.typeOf

inline fun <reified T>StorageDoneDatabase.migrateToRelease(deleteAfterMigration: Boolean = false) {
    val classType = T::class.java
    val typeName = typeOf<T>().simpleName

    val query = QueryBuilder.select(SelectResult.all())
        .from(DataSource.database(database))
        .where(Expression.property(type).equalTo(Expression.string(typeName)))

    val list = mutableListOf<T>()
    val rs = query.execute()
    val deleteIds = mutableListOf<String>()
    rs.forEach {
            result ->
        val map = result.toMap()[name] as? Map<*, *>
        if (map != null) {
            val mutableMap = map.toMutableMap()
            mutableMap.remove(type)
            val json = gson.toJson(mutableMap)
            val element = gson.fromJson<T>(json)

            classType.declaredFields.filter {
                it.type == ByteArray::class.java
            }.forEach {
                    field ->
                val fieldName = field.name
                field.isAccessible = true
                (result.getValue(name) as? Dictionary)?.getBlob(fieldName)?.let {
                    field.set(element, it.content)
                }
            }
            result.getString("id")?.let {
                deleteIds.add(it)
            }
            list.add(element)
        }
    }
    insertOrUpdate(list)

    if (deleteAfterMigration) {
        database.inBatch<Exception> {
            deleteIds.forEach {
                database.purge(it)
            }
        }
    }
}