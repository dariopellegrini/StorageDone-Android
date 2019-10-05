package com.dariopellegrini.storagedoneapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.dariopellegrini.storagedone.*
import com.dariopellegrini.storagedone.query.*
import com.dariopellegrini.storagedone.sorting.ascending
import com.dariopellegrini.storagedone.sorting.descending
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory
import androidx.core.widget.addTextChangedListener
import com.google.gson.annotations.SerializedName
import kotlin.reflect.KProperty


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
