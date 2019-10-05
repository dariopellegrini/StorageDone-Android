package com.dariopellegrini.storagedoneapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dariopellegrini.storagedone.*
import java.util.*
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    lateinit var database: StorageDoneDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StorageDoneDatabase.configure(this)

        database = StorageDoneDatabase("pets")

        CoroutineScope(Dispatchers.IO).launch {
            val girl1 = Girl(1, "Lara", 21)

            database.suspending.insertOrUpdate(girl1)

            val girl2 = Girl(1, "Lara", 21)

            database.suspending.insertOrUpdate(girl2)

            val girl3 = Girl(1, "Lara2", 21)

            database.suspending.insertOrUpdate(girl3)

            val girls = database.suspending.get<Girl>()

            val boy1 = Boy("id1", "Name1", 21)

            database.suspending.insertOrUpdate(boy1)

            val boy2 = Boy("id2", "Name2", 22)

            database.suspending.insertOrUpdate(boy2)

            val boys = database.suspending.get<Boy>()

            println(girls)
            println(girls)
        }
    }
}

data class Pet(val id: String, val name: String, val age: Int, val home: Home, val isValid: Boolean?, val list: List<String>, val date: Date): PrimaryKey {
    override fun primaryKey(): String {
        return "id"
    }
}

data class Girl(val id: Int, val name: String, val age: Int): MultiplePrimaryKey {
    override fun primaryKeys(): Array<String> {
        return arrayOf("id", "name")
    }
}

data class Boy(val id: String, val name: String, val age: Int): PrimaryKey {
    override fun primaryKey(): String {
        return "id"
    }
}

data class Home(val id: String, val name: String, val address: Address)

data class Address(val street: String, val city: String)

data class Teacher(val id: String,
                   val name: String?,
                   val surname: String?,
                   val age: Int?,
                   val cv: String?,
                   val bytes: ByteArray? = null): PrimaryKey {
    override fun primaryKey(): String {
        return "id"
    }
}

data class Product(val id: String, val name: String, val category: String, val price: Double, val vendor: String): PrimaryKey {
    override fun primaryKey(): String {
        return "id"
    }
}
