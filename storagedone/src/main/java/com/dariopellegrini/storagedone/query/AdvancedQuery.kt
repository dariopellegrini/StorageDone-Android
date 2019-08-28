package com.dariopellegrini.storagedone.query

import com.couchbase.lite.Expression
import com.couchbase.lite.Ordering

class AdvancedQuery() {
    var expression: Expression? = null
    var orderings: Array<Ordering>? = null
    var limit: Int? = null
    var skip: Int? = null
}
