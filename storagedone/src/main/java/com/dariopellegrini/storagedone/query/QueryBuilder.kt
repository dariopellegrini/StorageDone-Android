package com.dariopellegrini.storagedone.query

import com.couchbase.lite.ArrayFunction
import com.couchbase.lite.Expression
import com.dariopellegrini.storagedone.toJSONMap
import com.google.gson.Gson
import java.util.*

infix fun String.equal(value: Any): Expression {
    return when(value) {
        is String -> Expression.property(this).equalTo(Expression.string(value))
        is Int -> Expression.property(this).equalTo(Expression.intValue(value))
        is Boolean -> Expression.property(this).equalTo(Expression.booleanValue(value))
        is Float -> Expression.property(this).equalTo(Expression.floatValue(value))
        is Double -> Expression.property(this).equalTo(Expression.doubleValue(value))
        is Date -> Expression.property(this).equalTo(Expression.date(value))
        else -> {
            val expressions = Gson().toJSONMap(value).map {
                    entry ->
                val k = entry.key
                val v = entry.value
                "$this.$k" equal v
            }
            and(*ArrayList(expressions).toArray(arrayOf<Expression>()))
        }
    }
}

infix fun String.notEqual(value: Any): Expression {
    return when(value) {
        is String -> Expression.property(this).notEqualTo(Expression.string(value))
        is Int -> Expression.property(this).notEqualTo(Expression.intValue(value))
        is Boolean -> Expression.property(this).notEqualTo(Expression.booleanValue(value))
        is Float -> Expression.property(this).notEqualTo(Expression.floatValue(value))
        is Double -> Expression.property(this).notEqualTo(Expression.doubleValue(value))
        is Date -> Expression.property(this).notEqualTo(Expression.date(value))
        else -> {
            val expressions = Gson().toJSONMap(value).map {
                    entry ->
                val k = entry.key
                val v = entry.value
                "$this.$k" notEqual v
            }
            and(*ArrayList(expressions).toArray(arrayOf<Expression>()))
        }
    }
}

infix fun String.greaterThan(value: Number): Expression {
    return Expression.property(this).greaterThan(Expression.value(value))
}

infix fun String.greaterThanOrEqual(value: Number): Expression {
    return Expression.property(this).greaterThanOrEqualTo(Expression.value(value))
}

infix fun String.lessThan(value: Number): Expression {
    return Expression.property(this).lessThan(Expression.value(value))
}

infix fun String.lessThanOrEqual(value: Number): Expression {
    return Expression.property(this).lessThanOrEqualTo(Expression.value(value))
}

infix fun String.inside(value: List<Any>): Expression {
    val expressions = value.map {
        Expression.value(it)
    }
    return Expression.property(this).`in`(*ArrayList(expressions).toArray(arrayOf<Expression>()))
}

infix fun String.contains(value: Any): Expression {
    return ArrayFunction.contains(Expression.property(this), Expression.value(value))
}

infix fun String.like(value: Any): Expression {
    return Expression.property(this).like(Expression.value(value))
}

infix fun String.regex(value: String): Expression {
    return Expression.property(this).regex(Expression.string(value))
}

val String.isNull: Expression
    get() = Expression.property(this).isNullOrMissing

val String.isNotNull: Expression
    get() = Expression.property(this).notNullOrMissing()

infix fun String.between(pair: Pair<Number, Number>): Expression {
    return Expression.property(this).between(Expression.value(pair.first), Expression.value(pair.second))
}

infix fun String.greaterThan(value: Date): Expression {
    return Expression.property(this).greaterThan(Expression.longValue(value.time))
}

infix fun String.greaterOrEqualThan(value: Date): Expression {
    return Expression.property(this).greaterThanOrEqualTo(Expression.longValue(value.time))
}

infix fun String.lessThan(value: Date): Expression {
    return Expression.property(this).lessThan(Expression.longValue(value.time))
}

infix fun String.lessOrEqualThan(value: Date): Expression {
    return Expression.property(this).lessThanOrEqualTo(Expression.longValue(value.time))
}

infix fun String.betweenDates(pair: Pair<Date, Date>): Expression {
    return Expression.property(this).between(Expression.longValue(pair.first.time), Expression.longValue(pair.second.time))
}

fun or(vararg expressions: Expression): Expression {
    var expression = expressions[0]
    if (expressions.size > 1) {
        expressions.toList().subList(1, expressions.size).forEach {
            expression = expression.or(it)
        }
    }
    return expression
}

fun and(vararg expressions: Expression): Expression {
    var expression = expressions[0]
    if (expressions.size > 1) {
        expressions.toList().subList(1, expressions.size).forEach {
            expression = expression.and(it)
        }
    }
    return expression
}
