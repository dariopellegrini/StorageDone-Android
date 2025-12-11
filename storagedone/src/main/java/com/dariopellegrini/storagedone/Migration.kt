package com.dariopellegrini.storagedone

import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import com.couchbase.lite.Dictionary
import com.couchbase.lite.Expression
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import kotlin.reflect.typeOf

inline fun <reified T>StorageDoneDatabase.migrateToRelease(oldDatabaseName: String, deleteAfterMigration: Boolean = false) {
    val classType = T::class.java
    val typeName = typeOf<T>().simpleName

    val oldDatabase = Database(oldDatabaseName, DatabaseConfiguration())
    val query = QueryBuilder.select(SelectResult.all())
        .from(DataSource.database(oldDatabase))
        .where(Expression.property(type).equalTo(Expression.string(typeName)))

    val list = mutableListOf<T>()
    val rs = query.execute()
    val deleteIds = mutableListOf<String>()
    rs.forEach {
            result ->
        val map = result.toMap()[oldDatabaseName] as? Map<*, *>
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
                (result.getValue(oldDatabaseName) as? Dictionary)?.getBlob(fieldName)?.let {
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
        oldDatabase.inBatch<Exception> {
            deleteIds.forEach {
                oldDatabase.purge(it)
            }
        }
    }

    oldDatabase.close()
}