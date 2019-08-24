package com.dariopellegrini.storagedone.sorting

import com.couchbase.lite.Ordering

val String.ascending: Ordering
    get() = Ordering.property(this).ascending()

val String.descending: Ordering
 get() = Ordering.property(this).descending()