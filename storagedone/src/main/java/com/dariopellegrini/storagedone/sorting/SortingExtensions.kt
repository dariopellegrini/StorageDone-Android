package com.dariopellegrini.storagedone.sorting

import com.couchbase.lite.Ordering
import kotlin.reflect.KCallable

val String.ascending: Ordering
    get() = Ordering.property(this).ascending()

val String.descending: Ordering
 get() = Ordering.property(this).descending()

fun <T> KCallable<T>.ascending() = Ordering.property(this.name).ascending()

fun <T> KCallable<T>.descending() = Ordering.property(this.name).descending()