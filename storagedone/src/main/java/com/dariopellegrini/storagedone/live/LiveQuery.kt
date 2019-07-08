package com.dariopellegrini.storagedone.live

import com.couchbase.lite.ListenerToken
import com.couchbase.lite.Query

open class LiveQuery(val query: Query, val token: ListenerToken) {
    fun cancel() {
        query.removeChangeListener(token)
    }
}