package com.dariopellegrini.storagedone

import com.couchbase.lite.Expression
import com.google.gson.Gson
import java.util.*

fun Map<String, Any>.whereExpression(keyPrefix: String? = null): Expression? {
    val iterator = this.entries.iterator()
    if (!iterator.hasNext()) return null

    // prima condizione
    val first = iterator.next()
    val firstKey = if (keyPrefix != null) "$keyPrefix${first.key}" else first.key
    var expr: Expression = when (val v = first.value) {
        is String -> Expression.property(firstKey).equalTo(Expression.string(v))
        is Int -> Expression.property(firstKey).equalTo(Expression.intValue(v))
        is Boolean -> Expression.property(firstKey).equalTo(Expression.booleanValue(v))
        is Float -> Expression.property(firstKey).equalTo(Expression.floatValue(v))
        is Double -> Expression.property(firstKey).equalTo(Expression.doubleValue(v))
        is Date -> Expression.property(firstKey).equalTo(Expression.date(v))
        else -> null!!
    }

    // altre condizioni AND
    while (iterator.hasNext()) {
        val entry = iterator.next()
        val key = if (keyPrefix != null) "$keyPrefix${entry.key}" else entry.key
        val value = entry.value
        val nextExpr = when (value) {
            is String -> Expression.property(key).equalTo(Expression.string(value))
            is Int -> Expression.property(key).equalTo(Expression.intValue(value))
            is Boolean -> Expression.property(key).equalTo(Expression.booleanValue(value))
            is Float -> Expression.property(key).equalTo(Expression.floatValue(value))
            is Double -> Expression.property(key).equalTo(Expression.doubleValue(value))
            is Date -> Expression.property(key).equalTo(Expression.date(value))
            else -> null!!
        }
        expr = expr.and(nextExpr)
    }
    return expr
}

