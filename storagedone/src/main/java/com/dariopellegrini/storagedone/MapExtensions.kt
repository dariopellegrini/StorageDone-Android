package com.dariopellegrini.storagedone

import com.couchbase.lite.Expression
import com.google.gson.Gson
import java.util.*

fun Map<String, Any>.whereExpression(startingExpression: Expression): Expression {
    var whereExpression = startingExpression
    this.forEach {
        entry ->
        val key = entry.key
        val value = entry.value
        whereExpression = whereExpression.and(when(value) {
            is String -> Expression.property(key).equalTo(Expression.string(value))
            is Int -> Expression.property(key).equalTo(Expression.intValue(value))
            is Boolean -> Expression.property(key).equalTo(Expression.booleanValue(value))
            is Float -> Expression.property(key).equalTo(Expression.floatValue(value))
            is Double -> Expression.property(key).equalTo(Expression.doubleValue(value))
            is Date -> Expression.property(key).equalTo(Expression.date(value))
            else -> {
                val map = Gson().toJSONMap(value)
                Expression.property(key).equalTo(Expression.map(map))
            }
        })
    }
    return whereExpression
}