package com.dariopellegrini.storagedone

import android.content.Context
import com.couchbase.lite.*
import com.couchbase.lite.Collection
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
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf

open class StorageDoneDatabase(val name: String = "StorageDone") {

    companion object {
        fun configure(context: Context) {
            CouchbaseLite.init(context)
        }
    }

    var config = DatabaseConfiguration()
    var database = Database(name, config)

    val type = "StorageDoneType"
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, ser)
        .registerTypeAdapter(Date::class.java, deser)
        .setExclusionStrategies(byteArrayExclusionStrategy)
        .create()

    inline fun <reified T>collection(): Collection {
        val collection = database.getCollection(getTypeName<T>()) ?: database.createCollection(getTypeName<T>()).apply {
            createIndex(
                "${getTypeName<T>()}-idIndex",
                IndexBuilder.valueIndex(
                    ValueIndexItem.property("id")
                )
            )
        }
        return collection
    }

    inline fun <reified T>insertOrUpdate(element: T, useExistingValuesAsFallback: Boolean = false) {
        val classType = T::class.java
        val typeName = getTypeName<T>()
        val map = gson.toJSONMap(element).toMutableMap()

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
        }

        if (useExistingValuesAsFallback) {
            val existingMap = collection<T>().getDocument(mutableDoc.id)?.toMap()
            existingMap?.keys?.forEach { key ->
                if (map[key] == null) {
                    val existingElement = existingMap[key]
                    if (existingElement != null) {
                        map[key] = existingElement
                    }
                }
            }
        }

        mutableDoc.setData(map)

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
        collection<T>().save(mutableDoc)
    }

    inline fun <reified T>insertOrUpdate(elements: List<T>, useExistingValuesAsFallback: Boolean = false) {
        database.inBatch<Exception> {
            elements.forEach {
                insertOrUpdate(it, useExistingValuesAsFallback)
            }
        }
    }

    inline fun <reified T>insertOrUpdate(elements: Array<T>, useExistingValuesAsFallback: Boolean = false) {
        database.inBatch<Exception> {
            elements.forEach {
                insertOrUpdate(it, useExistingValuesAsFallback)
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
        database.inBatch<Exception> {
            elements.forEach {
                insertOrUpdate(it, expression)
            }
        }
    }

    inline fun <reified T>insertOrUpdate(elements: List<T>, crossinline onlyIf: (T) -> Expression) {
        database.inBatch<Exception> {
            elements.forEach {
                insertOrUpdate(it, onlyIf)
            }
        }
    }

    inline fun <reified T>insert(element: T) {
        val classType = T::class.java
        val map = gson.toJSONMap(element).toMutableMap()
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

        collection<T>().save(mutableDoc)
    }

    inline fun <reified T>insert(elements: List<T>) {
        database.inBatch<Exception> {
            elements.forEach {
                insert(it)
            }
        }
    }

    inline fun <reified T>get(): List<T> {
        val collection = collection<T>()
        val classType = T::class.java
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))

        val list = mutableListOf<T>()
        val rs = query.execute()
        rs.forEach {
            result ->
            val map = result.toMap()[collection.name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)

                classType.declaredFields.filter {
                    it.type == ByteArray::class.java
                }.forEach {
                        field ->
                    val fieldName = field.name
                    field.isAccessible = true
                    (result.getValue(collection.name) as? Dictionary)?.getBlob(fieldName)?.let {
                        field.set(element, it.content)
                    }
                }
                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(vararg orderings: Ordering): List<T> {
        val collection = collection<T>()
        val classType = T::class.java
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))
            .orderBy(*orderings)

        val list = mutableListOf<T>()
        val rs = query.execute()
        rs.map {
                result ->
            val map = result.toMap()[collection.name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)

                classType.declaredFields.filter {
                    it.type == ByteArray::class.java
                }.forEach {
                        field ->
                    val fieldName = field.name
                    field.isAccessible = true
                    (result.getValue(collection.name) as? Dictionary)?.getBlob(fieldName)?.let {
                        field.set(element, it.content)
                    }
                }

                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(filter: Map<String, Any>): List<T> {
        val collection = collection<T>()
        val classType = T::class.java
        val startingExpression = Expression.all()
        val whereExpression = filter.whereExpression(startingExpression)
        val query = QueryBuilder.select(SelectResult.all())
                .from(DataSource.collection(collection))
                .where(whereExpression)

        val list = mutableListOf<T>()
        val rs = query.execute()
        rs.map {
            result ->
            val map = result.toMap()[collection.name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)

                classType.declaredFields.filter {
                    it.type == ByteArray::class.java
                }.forEach {
                        field ->
                    val fieldName = field.name
                    field.isAccessible = true
                    (result.getValue(collection.name) as? Dictionary)?.getBlob(fieldName)?.let {
                        field.set(element, it.content)
                    }
                }

                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(expression: Expression): List<T> {
        val collection = collection<T>()
        val classType = T::class.java
        val startingExpression = Expression.all()
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))
            .where(startingExpression.and(expression))

        val list = mutableListOf<T>()
        val rs = query.execute()
        rs.map {
                result ->
            val map = result.toMap()[collection.name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)

                classType.declaredFields.filter {
                    it.type == ByteArray::class.java
                }.forEach {
                        field ->
                    val fieldName = field.name
                    field.isAccessible = true
                    (result.getValue(collection.name) as? Dictionary)?.getBlob(fieldName)?.let {
                        field.set(element, it.content)
                    }
                }

                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(expression: Expression, vararg orderings: Ordering): List<T> {
        val collection = collection<T>()
        val classType = T::class.java
        val startingExpression = Expression.all()
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))
            .where(startingExpression.and(expression))
            .orderBy(*orderings)

        val list = mutableListOf<T>()
        val rs = query.execute()
        rs.map {
                result ->
            val map = result.toMap()[collection.name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)

                classType.declaredFields.filter {
                    it.type == ByteArray::class.java
                }.forEach {
                        field ->
                    val fieldName = field.name
                    field.isAccessible = true
                    (result.getValue(collection.name) as? Dictionary)?.getBlob(fieldName)?.let {
                        field.set(element, it.content)
                    }
                }

                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>get(buildQuery: AdvancedQuery.() -> Unit): List<T> {
        val collection = collection<T>()
        val classType = T::class.java
        val startingExpression = Expression.all()
        val advancedQuery = AdvancedQuery()
        advancedQuery.buildQuery()
        var query: Query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))

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
            val map = result.toMap()[collection.name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)

                classType.declaredFields.filter {
                    it.type == ByteArray::class.java
                }.forEach {
                        field ->
                    val fieldName = field.name
                    field.isAccessible = true
                    (result.getValue(collection.name) as? Dictionary)?.getBlob(fieldName)?.let {
                        field.set(element, it.content)
                    }
                }

                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>delete() {
        val query = QueryBuilder.select(
                SelectResult.expression(Meta.id))
                .from(DataSource.collection(collection<T>()))

        database.inBatch<Exception> {
            query.execute().map {
                    result ->
                val id = result.getString(0)
                if (id != null) {
//                val doc = database.getDocument(id)
                    collection<T>().purge(id)
                }
            }
        }
    }

    inline fun <reified T>delete(filter: Map<String, Any>) {
        val startingExpression = Expression.all()
        val whereExpression = filter.whereExpression(startingExpression)
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.collection(collection<T>()))
                .where(whereExpression)

        database.inBatch<Exception> {
            query.execute().map {
                    result ->
                val id = result.getString(0)
                if (id != null) {
//                    val doc = database.getDocument(id)
                    collection<T>().purge(id)
                }
            }
        }
    }

    inline fun <reified T: PrimaryKey>delete(element: T) {
        val classType = T::class.java
        val primaryLabel = element.primaryKey()
        val field = classType.getDeclaredField(primaryLabel)
        field.isAccessible = true
        val elementId = field.get(element)
        val document = collection<T>().getDocument("$elementId-${getTypeName<T>()}")
        if (document != null) {
            collection<T>().purge(document)
//            database.delete(document)
        }
    }

    inline fun <reified T: PrimaryKey>delete(elements: List<T>) {
        database.inBatch<Exception> {
            elements.forEach { delete(it) }
        }
    }

    inline fun <reified T>delete(expression: Expression) {
        val startingExpression = Expression.all()
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(collection<T>()))
            .where(startingExpression.and(expression))

        database.inBatch<Exception> {
            query.execute().map {
                    result ->
                val id = result.getString(0)
                if (id != null) {
//                    val doc = database.getDocument(id)
                    collection<T>().purge(id)
                }
            }
        }
    }

//    fun purgeDeletedDocuments() {
//        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
//            .from(DataSource.database(database))
//            .where(Meta.deleted)
//
//        database.inBatch<Exception> {
//            query.execute().map {
//                    result ->
//                val id = result.getString(0)
//                if (id != null) {
//                    database.purge(id)
//                }
//            }
//        }
//    }

    // Live
    inline fun <reified T>live(crossinline closure: (List<T>) -> Unit): LiveQuery {

        val query = QueryBuilder.select(SelectResult.all()).from(DataSource.collection(collection<T>()))

        val token = query.addChangeListener { change ->

            try {
                CoroutineScope(Dispatchers.Default).launch {
                    val list = mutableListOf<T>()
                    change.results?.let { rs ->
                        rs.forEach { result ->
                            val map = result.toMap()[getTypeName<T>()]
                            if (map != null) {
                                try {
                                    val mutableMap = map
                                    val json = gson.toJson(mutableMap)
                                    val element = gson.fromJson<T>(json)
                                    list.add(element)
                                } catch (e: Exception) {
                                    android.util.Log.e("LiveQuery", "$e")
                                } catch (t: Throwable) {
                                    android.util.Log.e("LiveQuery", "$t")
                                }
                            }
                        }
                    }
                    closure(list)
                }
            } catch (e: Exception) {
                android.util.Log.e("LiveQuery", "$e")
            }
        }
        query.execute()
        return LiveQuery(query, token)
    }

    inline fun <reified T>live(vararg orderings: Ordering, crossinline closure: (List<T>) -> Unit): LiveQuery {
        val collection = collection<T>()
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))
            .orderBy(*orderings)

        val token = query.addChangeListener { change ->
            try {
                CoroutineScope(Dispatchers.Default).launch {
                    val list = mutableListOf<T>()
                    change.results?.let { rs ->
                        rs.forEach { result ->
                            val map = result.toMap()[getTypeName<T>()] as? Map<*, *>
                            if (map != null) {
                                try {
                                    val mutableMap = map.toMutableMap()
                                    val json = gson.toJson(mutableMap)
                                    val element = gson.fromJson<T>(json)
                                    list.add(element)
                                } catch (e: Exception) {
                                    android.util.Log.e("LiveQuery", "$e")
                                } catch (t: Throwable) {
                                    android.util.Log.e("LiveQuery", "$t")
                                }
                            }
                        }
                    }
                    closure(list)
                }
            } catch (e: Exception) {
                android.util.Log.e("LiveQuery", "$e")
            }
        }
        query.execute()
        return LiveQuery(query, token)
    }

    inline fun <reified T>live(expression: Expression, vararg orderings: Ordering, crossinline closure: (List<T>) -> Unit): LiveQuery {
        val collection = collection<T>()
        val startingExpression = Expression.all()
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))
            .where(startingExpression.and(expression))
            .orderBy(*orderings)

        val token = query.addChangeListener { change ->
            try {
                CoroutineScope(Dispatchers.Default).launch {
                    val list = mutableListOf<T>()
                    change.results?.let { rs ->
                        rs.forEach { result ->
                            val map = result.toMap()[getTypeName<T>()] as? Map<*, *>
                            if (map != null) {
                                try {
                                    val mutableMap = map.toMutableMap()
                                    val json = gson.toJson(mutableMap)
                                    val element = gson.fromJson<T>(json)
                                    list.add(element)
                                } catch (e: Exception) {
                                    android.util.Log.e("LiveQuery", "$e")
                                } catch (t: Throwable) {
                                    android.util.Log.e("LiveQuery", "$t")
                                }
                            }
                        }
                    }
                    closure(list)
                }
            } catch (e: Exception) {
                android.util.Log.e("LiveQuery", "$e")
            }
        }
        query.execute()
        return LiveQuery(query, token)
    }

    inline fun <reified T>live(buildQuery: AdvancedQuery.() -> Unit, crossinline closure: (List<T>) -> Unit): LiveQuery {
        val collection = collection<T>()
        val startingExpression = Expression.all()

        val advancedQuery = AdvancedQuery()
        advancedQuery.buildQuery()
        var query: Query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))

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
                CoroutineScope(Dispatchers.Default).launch {
                    val list = mutableListOf<T>()
                    change.results?.let { rs ->
                        rs.forEach { result ->
                            val map = result.toMap()[getTypeName<T>()] as? Map<*, *>
                            if (map != null) {
                                try {
                                    val mutableMap = map.toMutableMap()
                                    val json = gson.toJson(mutableMap)
                                    val element = gson.fromJson<T>(json)
                                    list.add(element)
                                } catch (e: Exception) {
                                    android.util.Log.e("LiveQuery", "$e")
                                } catch (t: Throwable) {
                                    android.util.Log.e("LiveQuery", "$t")
                                }
                            }
                        }
                    }
                    closure(list)
                }
            } catch (e: Exception) {
                android.util.Log.e("LiveQuery", "$e")
            }
        }
        query.execute()
        return LiveQuery(query, token)
    }

    // Blobs
    fun saveByteArray(id: String, collectionName: String, byteArray: ByteArray) {
        val collection = database.getCollection(collectionName) ?: database.createCollection(collectionName)
        val mutableDocument = MutableDocument(id)
        val blob = Blob("image/jpeg", byteArray)
        mutableDocument.setBlob("blob", blob)
        collection.save(mutableDocument)
    }

    fun blobsCount(collectionName: String): Int {
        val collection = database.getCollection(collectionName) ?: database.createCollection(collectionName)
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))
        val rs = query.execute()
        return rs.allResults().size
    }

    fun readReadByteArray(id: String, collectionName: String): ByteArray? {
        val collection = database.getCollection(collectionName) ?: database.createCollection(collectionName)
        val mutableDocument = collection.getDocument(id)
        return mutableDocument?.getBlob("blob")?.content
    }

    fun deleteByteArray(collectionName: String) {
        val collection = database.getCollection(collectionName) ?: database.createCollection(collectionName)
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(collection))
        val rs = query.execute()
        rs.allResults().forEach {
                result ->
            val id = result.getString(0)
            if (id != null) {
                val doc = collection.getDocument(id)
                if (doc != null) {
                    collection.delete(doc)
                }
            }
        }
    }

    // Fulltext
    inline fun <reified T>fulltextIndex(vararg values: String) {
        collection<T>().createIndex(
            "${getTypeName<T>()}-index",
            IndexBuilder.fullTextIndex(*(values.map {
                FullTextIndexItem.property(it)
            }.toTypedArray())).ignoreAccents(false))
    }

    inline fun <reified T>search(text: String): List<T> {
        val collection = collection<T>()
        val classType = T::class.java
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))
            .where(Expression.all()
                .and(FullTextFunction.match(Expression.fullTextIndex("${getTypeName<T>()}-index"), text))
            )

        val list = mutableListOf<T>()
        val rs = query.execute()
        rs.forEach {
                result ->
            val map = result.toMap()[collection.name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)

                classType.declaredFields.filter {
                    it.type == ByteArray::class.java
                }.forEach {
                        field ->
                    val fieldName = field.name
                    field.isAccessible = true
                    (result.getValue(collection.name) as? Dictionary)?.getBlob(fieldName)?.let {
                        field.set(element, it.content)
                    }
                }

                list.add(element)
            }
        }
        return list
    }

    inline fun <reified T>search(text: String, buildQuery: AdvancedQuery.() -> Unit): List<T> {
        val collection = collection<T>()
        val classType = T::class.java
        val startingExpression =
            Expression.all()
                .and(FullTextFunction.match(Expression.fullTextIndex("${getTypeName<T>()}-index"), text))

        val advancedQuery = AdvancedQuery()
        advancedQuery.buildQuery()
        var query: Query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))

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
            val map = result.toMap()[collection.name] as? Map<*, *>
            if (map != null) {
                val mutableMap = map.toMutableMap()
                val json = gson.toJson(mutableMap)
                val element = gson.fromJson<T>(json)

                classType.declaredFields.filter {
                    it.type == ByteArray::class.java
                }.forEach {
                        field ->
                    val fieldName = field.name
                    field.isAccessible = true
                    (result.getValue(collection.name) as? Dictionary)?.getBlob(fieldName)?.let {
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

    inline fun <reified T>getTypeName(): String {
        return typeOf<T>().simpleName.replace("<", "generics").replace(">", "").replace("Series", "CollectionSeries")
    }
}
