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
import com.couchbase.lite.Dictionary
import com.dariopellegrini.storagedone.query.and
import com.dariopellegrini.storagedone.query.equal
import kotlin.reflect.typeOf

@OptIn(ExperimentalStdlibApi::class)
open class StorageDoneDatabase(val name: String = "StorageDone") {

    companion object {
        fun configure(context: Context) {
            CouchbaseLite.init(context)
            Database.log.file.config = LogFileConfiguration(context.getCacheDir().toString())
            Database.log.file.level = LogLevel.ERROR
        }
    }

    var config = DatabaseConfiguration()
    var database = Database(name, config)

    val type = "StorageDoneType"
    val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, ser)
        .registerTypeAdapter(Date::class.java, deser)
        .setExclusionStrategies(byteArrayExclusionStrategy)
        .create()
    
    inline fun <reified T>insertOrUpdate(element: T) {
        val classType = T::class.java
        val typeName = typeOf<T>().simpleName
        val map = gson.toJSONMap(element).toMutableMap()
        map[type] = typeName

        val mutableDoc = when(element) {
            is PrimaryKey -> {
                val primaryLabel = element.primaryKey()
                try {
                    val field = classType.getDeclaredField(primaryLabel)
                    field.isAccessible = true
                    val elementId = field.get(element)
                    MutableDocument("$elementId-$typeName")
                } catch (e: Exception) {
                    MutableDocument()
                }
            }
            is MultiplePrimaryKey -> {
                try {
                    val elementId = element.primaryKeys().fold("") { acc, label ->
                        val field = classType.getDeclaredField(label)
                        field.isAccessible = true
                        "$acc${field.get(element)}"
                    }
                    MutableDocument("$elementId-$typeName")
                } catch (e: Exception) {
                    MutableDocument()
                }
            }
            else -> {
                MutableDocument()
            }
        }.setData(map)

        classType.declaredFields.filter {
            it.type == ByteArray::class.java
        }.forEach {
                field ->
            val name = field.name
            field.isAccessible = true
            (field.get(element) as? ByteArray)?.let {
                mutableDoc.setBlob(name, Blob("application/binary", it))
            }
        }

        database.save(mutableDoc)
    }

    inline fun <reified T>insertOrUpdate(elements: List<T>) {
        database.inBatch {
            elements.forEach {
                insertOrUpdate(it)
            }
        }
    }

    inline fun <reified T>insertOrUpdate(elements: Array<T>) {
        database.inBatch {
            elements.forEach {
                insertOrUpdate(it)
            }
        }
    }

    inline fun <reified T: PrimaryKey>insertOrUpdate(element: T, expression: Expression) {
        val field = T::class.java.getDeclaredField(element.primaryKey())
        field.isAccessible = true
        val elementId = field.get(element) ?: ""
        if (get<T>(and(element.primaryKey() equal elementId, expression)).isNotEmpty()) {
            insertOrUpdate(element)
        }
    }

    inline fun <reified T>insertOrUpdate(elements: T, crossinline onlyIf: (T) -> Expression) {
        if (get<T>(onlyIf(elements)).isNotEmpty()) {
            insertOrUpdate(elements)
        }
    }

    inline fun <reified T: PrimaryKey>insertOrUpdate(elements: List<T>, expression: Expression) {
        database.inBatch {
            elements.forEach {
                insertOrUpdate(it, expression)
            }
        }
    }

    inline fun <reified T>insertOrUpdate(elements: List<T>, crossinline onlyIf: (T) -> Expression) {
        database.inBatch {
            elements.forEach {
                insertOrUpdate(it, onlyIf)
            }
        }
    }

    inline fun <reified T>insert(element: T) {
        val classType = T::class.java
        val typeName = typeOf<T>().simpleName
        val map = gson.toJSONMap(element).toMutableMap()
        map[type] = typeName
        val mutableDoc = MutableDocument().setData(map)

        classType.declaredFields.filter {
            it.type == ByteArray::class.java
        }.forEach {
                field ->
            val name = field.name
            field.isAccessible = true
            (field.get(element) as? ByteArray)?.let {
                mutableDoc.setBlob(name, Blob("application/binary", it))
            }
        }

        database.save(mutableDoc)
    }

    inline fun <reified T>insert(elements: List<T>) {
        database.inBatch {
            elements.forEach {
                insert(it)
            }
        }
    }

    inline fun <reified T>get(): List<T> {
        val classType = T::class.java
        val typeName = typeOf<T>().simpleName
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(type).equalTo(Expression.string(typeName)))

        val list = mutableListOf<T>()
        val rs = query.execute()
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
                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(vararg orderings: Ordering): List<T> {
        val classType = T::class.java
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName)))
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

                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(filter: Map<String, Any>): List<T> {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName))
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

                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(expression: Expression): List<T> {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName))
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

                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(expression: Expression, vararg orderings: Ordering): List<T> {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName))
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

                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(buildQuery: AdvancedQuery.() -> Unit): List<T> {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName))
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
                .where(Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName)))

        query.execute().map {
            result ->
            val id = result.getString(0)
            if (id != null) {
                val doc = database.getDocument(id)
                database.delete(doc)
            }
        }
    }

    inline fun <reified T>delete(filter: Map<String, Any>) {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName))
        val whereExpression = filter.whereExpression(startingExpression)
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(whereExpression)

        query.execute().map {
            result ->
            val id = result.getString(0)
            if (id != null) {
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
        val document = database.getDocument("$elementId-${typeOf<T>().simpleName}")
        if (document != null && document.getString(type) == typeOf<T>().simpleName) {
            database.delete(document)
        }
    }

    inline fun <reified T: PrimaryKey>delete(elements: List<T>) {
        elements.forEach { delete(it) }
    }

    inline fun <reified T>delete(expression: Expression) {
        val classType = T::class.java
        val startingExpression = Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName))
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(startingExpression.and(expression))

        query.execute().map {
                result ->
            val id = result.getString(0)
            if (id != null) {
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
            .where(Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName)))

        val token = query.addChangeListener { change ->
            try {
                val list = mutableListOf<T>()
                change.results?.map {
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
            .where(Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName)))
            .orderBy(*orderings)

        val token = query.addChangeListener { change ->
            try {
                val list = mutableListOf<T>()
                change.results?.map {
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
        val startingExpression = Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName))
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(startingExpression.and(expression))
            .orderBy(*orderings)

        val token = query.addChangeListener { change ->
            try {
                val list = mutableListOf<T>()
                change.results?.map {
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
        val startingExpression = Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName))

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
                change.results?.map {
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

    // Blobs
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

    fun readReadByteArray(id: String): ByteArray? {
        val mutableDocument = database.getDocument(id)
        return mutableDocument.getBlob("blob")?.content
    }

    fun deleteByteArray(collectionName: String) {
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property(type).equalTo(Expression.string(collectionName)))
        val rs = query.execute()
        rs.allResults().forEach {
                result ->
            val id = result.getString(0)
            if (id != null) {
                val doc = database.getDocument(id)
                if (doc != null) {
                    database.delete(doc)
                }
            }
        }
    }

    // Fulltext
    inline fun <reified T>fulltextIndex(vararg values: String) {
        val classType = T::class.java
        database.createIndex(
            "${typeOf<T>()}-index",
            IndexBuilder.fullTextIndex(*(values.map {
                FullTextIndexItem.property(it)
            }.toTypedArray())).ignoreAccents(false))
    }

    inline fun <reified T>search(text: String): List<T> {
        val classType = T::class.java
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName))
                .and(FullTextExpression.index("${typeOf<T>()}-index").match(text))
            )

        val list = mutableListOf<T>()
        val rs = query.execute()
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

                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>search(text: String, buildQuery: AdvancedQuery.() -> Unit): List<T> {
        val classType = T::class.java
        val startingExpression =
            Expression.property(type).equalTo(Expression.string(typeOf<T>().simpleName))
                .and(FullTextExpression.index("${typeOf<T>()}-index").match(text))

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

                list.add(element)
            }
        }
        return list
    }

    fun clear() {
        database.delete()
        database = Database(name, config)
    }
}
