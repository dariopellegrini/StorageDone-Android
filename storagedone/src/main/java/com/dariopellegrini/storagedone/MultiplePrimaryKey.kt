package com.dariopellegrini.storagedone

interface MultiplePrimaryKey {
    fun primaryKeys(): Array<String>
}