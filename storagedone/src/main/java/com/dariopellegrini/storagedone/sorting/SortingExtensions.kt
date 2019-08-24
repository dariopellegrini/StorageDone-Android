package com.dariopellegrini.storagedone.sorting

import com.couchbase.lite.Ordering

fun String.ascending(): Ordering {
    return Ordering.property(this).ascending()
}

fun String.descending(): Ordering {
    return Ordering.property(this).descending()
}