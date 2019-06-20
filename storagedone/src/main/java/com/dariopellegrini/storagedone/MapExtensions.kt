package com.dariopellegrini.storagedone

import com.couchbase.lite.Expression
import com.google.gson.Gson
import java.util.*

fun Map<String, Any>.whereExpression(startingExpression: Expression, keyPrefix: String? = null): Expression {
    var whereExpression = startingExpression
    this.forEach {
        entry ->
        val key = if (keyPrefix != null) "$keyPrefix${entry.key}" else entry.key
        val value = entry.value
        whereExpression = whereExpression.and(when(value) {
            is String -> Expression.property(key).equalTo(Expression.string(value))
            is Int -> Expression.property(key).equalTo(Expression.intValue(value))
            is Boolean -> Expression.property(key).equalTo(Expression.booleanValue(value))
            is Float -> Expression.property(key).equalTo(Expression.floatValue(value))
            is Double -> Expression.property(key).equalTo(Expression.doubleValue(value))
            is Date -> {
                print(value)
                Expression.property(key).equalTo(Expression.date(value))
            }
            else -> {
                Gson().toJSONMap(value).whereExpression(whereExpression, "$key.")
            }
        })
    }
    return whereExpression
}

