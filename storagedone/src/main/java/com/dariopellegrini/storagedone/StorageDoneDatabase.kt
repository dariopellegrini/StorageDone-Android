package com.dariopellegrini.storagedone

import android.content.Context
import com.couchbase.lite.*
import com.couchbase.lite.SelectResult
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.MutableDocument
import com.google.gson.GsonBuilder
import java.util.*
import com.dariopellegrini.storagedone.live.LiveQuery
import com.dariopellegrini.storagedone.query.AdvancedQuery
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource.database




open class StorageDoneDatabase(val context: Context, val name: String = "StorageDone") {

    var config = DatabaseConfiguration(context)
    var database = Database(name, config)

    val type = "StorageDoneType"
    val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, ser)
        .registerTypeAdapter(Date::class.java, deser).create()

    inline fun <reified T>insertOrUpdate(element: T) {
        val classType = T::class.java
        android.util.Log.i("StorageDone", "Mapping begin")
        val map = gson.toJSONMap(element).toMutableMap()
        android.util.Log.i("StorageDone", "Mapping finihed")
        map[type] = classType.simpleName

        val mutableDoc = if (element is PrimaryKey) {
            val primaryLabel = element.primaryKey()
            try {
                val field = classType.getDeclaredField(primaryLabel)
                field.isAccessible = true
                val elementId = field.get(element)
                MutableDocument("$elementId-${classType.simpleName}")
            } catch (e: Exception) {
                MutableDocument()
            }
        } else {
            MutableDocument()
        }.setData(map)

        android.util.Log.i("StorageDone", "Insert begin")
        database.save(mutableDoc)
        android.util.Log.i("StorageDone", "Insert finished")
    }

    fun saveByteArray(id: String, collectionName: String, byteArray: ByteArray) {
        val mutableDocument = MutableDocument(id)
        val blob = Blob("image/jpeg", byteArray)
        mutableDocument.setString(type, collectionName)
        mutableDocument.setBlob("blob", blob)
        database.save(mutableDocument)
    }

    fun blobsCount(collectionName: String): Int {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(type).equalTo(Expression.string(collectionName)))
        val rs = query.execute()
        return rs.allResults().size
    }

    fun readReadByteArray(id: String): ByteArray {
        val mutableDocument = database.getDocument(id)
        return mutableDocument.getBlob("blob").content
    }

    inline fun <reified T>insertOrUpdate(elements: List<T>) {
        elements.forEach {
            insertOrUpdate(it)
        }
    }

    inline fun <reified T>insert(element: T) {
        val classType = T::class.java
        val map = gson.toJSONMap(element).toMutableMap()
        map[type] = classType.simpleName

        database.save(MutableDocument().setData(map))
    }

    inline fun <reified T>insert(elements: List<T>) {
        elements.forEach {
            insert(it)
        }
    }

    inline fun <reified T>get(): List<T> {
        val classType = T::class.java
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(type).equalTo(Expression.string(classType.simpleName)))

        val list = mutableListOf<T>()
        val rs = query.execute()
        rs.map {
            result ->
            val map = result.toMap()[name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                mutableMap.remove(type)
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)
                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(vararg orderings: Ordering): List<T> {
        val classType = T::class.java
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(type).equalTo(Expression.string(classType.simpleName)))
            .orderBy(*orderings)

        val list = mutableListOf<T>()
        val rs = query.execute()
        rs.map {
                result ->
            val map = result.toMap()[name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                mutableMap.remove(type)
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)
                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(filter: Map<String, Any>): List<T> {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(classType.simpleName))
        val whereExpression = filter.whereExpression(startingExpression)
        val query = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(database))
                .where(whereExpression)

        val list = mutableListOf<T>()
        val rs = query.execute()
        rs.map {
            result ->
            val map = result.toMap()[name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                mutableMap.remove(type)
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)
                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(expression: Expression): List<T> {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(classType.simpleName))
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(startingExpression.and(expression))

        val list = mutableListOf<T>()
        val rs = query.execute()
        rs.map {
                result ->
            val map = result.toMap()[name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                mutableMap.remove(type)
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)
                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(expression: Expression, vararg orderings: Ordering): List<T> {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(classType.simpleName))
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(startingExpression.and(expression))
            .orderBy(*orderings)

        val list = mutableListOf<T>()
        val rs = query.execute()
        rs.map {
                result ->
            val map = result.toMap()[name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                mutableMap.remove(type)
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)
                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(buildQuery: AdvancedQuery.() -> Unit): List<T> {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(classType.simpleName))
        val advancedQuery = AdvancedQuery()
        advancedQuery.buildQuery()
        var query: Query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))

        query = if (advancedQuery.expression != null) {
            (query as From).where(startingExpression.and(advancedQuery.expression!!))
        } else {
            (query as From).where(startingExpression)
        }

        if (advancedQuery.orderings != null) {
            query = query.orderBy(*advancedQuery.orderings!!)
        }

        if (advancedQuery.limit != null && advancedQuery.skip != null) {
            if (query is Where) {
                query = query.limit(Expression.intValue(advancedQuery.limit!!), Expression.intValue(advancedQuery.skip!!))
            } else if (query is OrderBy) {
                query = query.limit(Expression.intValue(advancedQuery.limit!!), Expression.intValue(advancedQuery.skip!!))
            }
        } else if (advancedQuery.limit != null) {
            if (query is Where) {
                query = query.limit(Expression.intValue(advancedQuery.limit!!))
            } else if (query is OrderBy) {
                query = query.limit(Expression.intValue(advancedQuery.limit!!))
            }
        }

        val list = mutableListOf<T>()
        val rs = query.execute()
        rs.map {
                result ->
            val map = result.toMap()[name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                mutableMap.remove(type)
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)
                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>delete() {
        val classType = T::class.java
        val query = QueryBuilder.select(
                SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(Expression.property(type).equalTo(Expression.string(classType.simpleName)))

        query.execute().map {
            result ->
            if (result.contains("id")) {
                val id = result.getString("id")
                val doc = database.getDocument(id)
                database.delete(doc)
            }
        }
    }

    inline fun <reified T>delete(filter: Map<String, Any>) {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(classType.simpleName))
        val whereExpression = filter.whereExpression(startingExpression)
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(whereExpression)

        query.execute().map {
            result ->
            if (result.contains("id")) {
                val id = result.getString("id")
                val doc = database.getDocument(id)
                database.delete(doc)
            }
        }
    }

    inline fun <reified T: PrimaryKey>delete(element: T) {
        val classType = T::class.java
        val primaryLabel = element.primaryKey()
        val field = classType.getDeclaredField(primaryLabel)
        field.isAccessible = true
        val elementId = field.get(element)
        val document = database.getDocument("$elementId-${classType.simpleName}")
        if (document != null && document.getString(type) == classType.simpleName) {
            database.delete(document)
        }
    }

    inline fun <reified T: PrimaryKey>delete(elements: List<T>) {
        elements.forEach { delete(it) }
    }

    inline fun <reified T>delete(expression: Expression) {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(classType.simpleName))
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(startingExpression.and(expression))

        query.execute().map {
                result ->
            if (result.contains("id")) {
                val id = result.getString("id")
                val doc = database.getDocument(id)
                database.delete(doc)
            }
        }
    }

    // Live
    inline fun <reified T>live(crossinline closure: (List<T>) -> Unit): LiveQuery {
        val classType = T::class.java
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(type).equalTo(Expression.string(classType.simpleName)))

        val token = query.addChangeListener { change ->
            try {
                val list = mutableListOf<T>()
                change.results.map {
                        result ->
                    val map = result.toMap()[name] as? Map<*, *>
                    if (map != null) {
                        val mutableMap = map.toMutableMap()
                        mutableMap.remove(type)
                        val json = gson.toJson(mutableMap)
                        val element = gson.fromJson<T>(json)
                        list.add(element)
                    }
                }
                closure(list)
            } catch (e: Exception) {
                android.util.Log.e("LiveQuery", "$e")
            }
        }
        query.execute()
        return LiveQuery(query, token)
    }

    inline fun <reified T>live(vararg orderings: Ordering, crossinline closure: (List<T>) -> Unit): LiveQuery {
        val classType = T::class.java
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(type).equalTo(Expression.string(classType.simpleName)))
            .orderBy(*orderings)

        val token = query.addChangeListener { change ->
            try {
                val list = mutableListOf<T>()
                change.results.map {
                        result ->
                    val map = result.toMap()[name] as? Map<*, *>
                    if (map != null) {
                        val mutableMap = map.toMutableMap()
                        mutableMap.remove(type)
                        val json = gson.toJson(mutableMap)
                        val element = gson.fromJson<T>(json)
                        list.add(element)
                    }
                }
                closure(list)
            } catch (e: Exception) {
                android.util.Log.e("LiveQuery", "$e")
            }
        }
        query.execute()
        return LiveQuery(query, token)
    }

    inline fun <reified T>live(expression: Expression, vararg orderings: Ordering, crossinline closure: (List<T>) -> Unit): LiveQuery {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(classType.simpleName))
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(startingExpression.and(expression))
            .orderBy(*orderings)

        val token = query.addChangeListener { change ->
            try {
                val list = mutableListOf<T>()
                change.results.map {
                        result ->
                    val map = result.toMap()[name] as? Map<*, *>
                    if (map != null) {
                        val mutableMap = map.toMutableMap()
                        mutableMap.remove(type)
                        val json = gson.toJson(mutableMap)
                        val element = gson.fromJson<T>(json)
                        list.add(element)
                    }
                }
                closure(list)
            } catch (e: Exception) {
                android.util.Log.e("LiveQuery", "$e")
            }
        }
        query.execute()
        return LiveQuery(query, token)
    }

    inline fun <reified T>live(buildQuery: AdvancedQuery.() -> Unit, crossinline closure: (List<T>) -> Unit): LiveQuery {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(classType.simpleName))

        val advancedQuery = AdvancedQuery()
        advancedQuery.buildQuery()
        var query: Query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))

        query = if (advancedQuery.expression != null) {
            (query as From).where(startingExpression.and(advancedQuery.expression!!))
        } else {
            (query as From).where(startingExpression)
        }

        if (advancedQuery.orderings != null) {
            query = query.orderBy(*advancedQuery.orderings!!)
        }

        if (advancedQuery.limit != null && advancedQuery.skip != null) {
            if (query is Where) {
                query = query.limit(Expression.intValue(advancedQuery.limit!!), Expression.intValue(advancedQuery.skip!!))
            } else if (query is OrderBy) {
                query = query.limit(Expression.intValue(advancedQuery.limit!!), Expression.intValue(advancedQuery.skip!!))
            }
        } else if (advancedQuery.limit != null) {
            if (query is Where) {
                query = query.limit(Expression.intValue(advancedQuery.limit!!))
            } else if (query is OrderBy) {
                query = query.limit(Expression.intValue(advancedQuery.limit!!))
            }
        }

        val token = query.addChangeListener { change ->
            try {
                val list = mutableListOf<T>()
                change.results.map {
                    result ->
                    val map = result.toMap()[name] as? Map<*, *>
                    if (map != null) {
                        val mutableMap = map.toMutableMap()
                        mutableMap.remove(type)
                        val json = gson.toJson(mutableMap)
                        val element = gson.fromJson<T>(json)
                        list.add(element)
                    }
                }
                closure(list)
            } catch (e: Exception) {
                android.util.Log.e("LiveQuery", "$e")
            }
        }
        query.execute()
        return LiveQuery(query, token)
    }
}
