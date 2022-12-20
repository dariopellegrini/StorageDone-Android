package com.dariopellegrini.storagedone.query

import com.couchbase.lite.ArrayFunction
import com.couchbase.lite.Expression
import com.couchbase.lite.Function
import com.dariopellegrini.storagedone.toJSONMap
import com.google.gson.Gson
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.KProperty
import kotlin.reflect.KType

// String

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

fun String.like(value: String, caseInsensitive: Boolean): Expression {
    return  if (caseInsensitive) {
        Function.lower(Expression.property(this)).like(Expression.value(value.toLowerCase(Locale.ROOT)))
    } else {
        Expression.property(this).like(Expression.value(value))
    }
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


// Typesafe

infix fun <T: Any> KProperty<T>.equal(value: T): Expression {
    return this.name.equal(value)
}

infix fun <T: Any> KProperty<T>.notEqual(value: T): Expression {
    return this.name.notEqual(value)
}

infix fun <T: Number> KProperty<T>.greaterThan(value: T): Expression {
    return Expression.property(this.name).greaterThan(Expression.value(value))
}

infix fun <T: Number> KProperty<T>.greaterThanOrEqual(value: T): Expression {
    return Expression.property(this.name).greaterThanOrEqualTo(Expression.value(value))
}

infix fun <T: Number> KProperty<T>.lessThan(value: T): Expression {
    return Expression.property(this.name).lessThan(Expression.value(value))
}

infix fun <T: Number> KProperty<T>.lessThanOrEqual(value: T): Expression {
    return Expression.property(this.name).lessThanOrEqualTo(Expression.value(value))
}

infix fun <T: Any> KProperty<T>.inside(value: List<T>): Expression {
    val expressions = value.map {
        Expression.value(it)
    }
    return Expression.property(this.name).`in`(*ArrayList(expressions).toArray(arrayOf<Expression>()))
}

infix fun <T: Any> KProperty<T>.contains(value: T): Expression {
    return ArrayFunction.contains(Expression.property(this.name), Expression.value(value))
}

infix fun <T: Any> KProperty<T>.like(value: Any): Expression {
    return Expression.property(this.name).like(Expression.value(value))
}

infix fun <T: Any> KProperty<T>.regex(value: String): Expression {
    return Expression.property(this.name).regex(Expression.string(value))
}

val <T: Any> KProperty<T?>.isNull: Expression
    get() = Expression.property(this.name).isNullOrMissing

val <T: Any> KProperty<T?>.isNotNull: Expression
    get() = Expression.property(this.name).notNullOrMissing()

infix fun <T: Number> KProperty<T>.between(pair: Pair<T, T>): Expression {
    return Expression.property(this.name).between(Expression.value(pair.first), Expression.value(pair.second))
}

infix fun <T: Date> KProperty<T>.greaterThan(value: T): Expression {
    return Expression.property(this.name).greaterThan(Expression.longValue(value.time))
}

infix fun <T: Date> KProperty<T>.greaterOrEqualThan(value: T): Expression {
    return Expression.property(this.name).greaterThanOrEqualTo(Expression.longValue(value.time))
}

infix fun <T: Date> KProperty<T>.lessThan(value: T): Expression {
    return Expression.property(this.name).lessThan(Expression.longValue(value.time))
}

infix fun <T: Date> KProperty<T>.lessOrEqualThan(value: T): Expression {
    return Expression.property(this.name).lessThanOrEqualTo(Expression.longValue(value.time))
}

infix fun <T: Date> KProperty<T>.betweenDates(pair: Pair<T, T>): Expression {
    return Expression.property(this.name).between(Expression.longValue(pair.first.time), Expression.longValue(pair.second.time))
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

fun not(expression: Expression): Expression {
    return Expression.not(expression)
}
