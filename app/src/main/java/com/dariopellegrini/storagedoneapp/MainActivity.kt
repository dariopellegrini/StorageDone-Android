package com.dariopellegrini.storagedoneapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dariopellegrini.storagedone.*
import com.dariopellegrini.storagedone.query.and
import com.dariopellegrini.storagedone.query.equal
import com.dariopellegrini.storagedone.query.isNull
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

            database.clear()

            val newGirl1 = Girl(11, "Lara", 21, listOf("Bs"))
            val newGirl2 = Girl(12, "Lara1", 21, listOf("Bs"))

            database.suspending.insertOrUpdate(listOf(newGirl1, newGirl2))

            val newGirl1Null = Girl(11, "Lara", 21, null)
            val newGirl2Null = Girl(12, "Lara", 21, null)

            database.suspending.insertOrUpdate(listOf(newGirl1, newGirl2))

            database.insertOrUpdate(listOf(newGirl1Null, newGirl2Null), "name" equal "Lara")

//            database.insertOrUpdate(listOf(newGirl1Null, newGirl2Null)) {
//                and("id" equal it.id, "cities".isNull)
//            }

            val newGirls = database.suspending.get<Girl>()

            println(newGirls)
            println(newGirls)
        }
    }
}

data class Pet(val id: String, val name: String, val age: Int, val home: Home, val isValid: Boolean?, val list: List<String>, val date: Date): PrimaryKey {
    override fun primaryKey(): String {
        return "id"
    }
}

data class Girl(val id: Int, val name: String, val age: Int, val cities: List<String>?): PrimaryKey {
    override fun primaryKey(): String {
        return "id"
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
