package com.dariopellegrini.storagedone

import android.content.Context
import com.google.gson.Gson
import com.couchbase.lite.*
import com.couchbase.lite.SelectResult
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.MutableDocument
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.*


open class StorageDoneDatabase(val context: Context, val name: String = "StorageDone") {

    var config = DatabaseConfiguration(context)
    var database = Database(name, config)

    val type = "StorageDoneType"
    val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, ser)
        .registerTypeAdapter(Date::class.java, deser).create()

    inline fun <reified T>insertOrUpdate(element: T) {
        val classType = T::class.java
        val map = gson.toJSONMap(element).toMutableMap()
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

        database.save(mutableDoc)
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
        if (document.getString(type) == classType.simpleName) {
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
}