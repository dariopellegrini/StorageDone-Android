package com.dariopellegrini.storagedoneapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.dariopellegrini.storagedone.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var database: StorageDoneDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = StorageDoneDatabase(this, "pets")

        val pets = listOf(Pet("id1", "Pet 1", 10, Home("idHome1", "Home1", Address("Street1", "City1")), true),
            Pet("id2", "Pet 2", 11, Home("idHome2", "Home2", Address("Street2", "City2")), true),
            Pet("id3", "Pet 3", 12, Home("idHome2", "Home2", Address("Street3", "City3")), false))

        database += pets

        val databasePets: List<Pet> = database.get(mapOf("home" to mapOf("id" to "idHome2")))

        Log.i("Pets", "$pets")
    }
}

data class Pet(val id: String, val name: String, val age: Int, val home: Home, val isValid: Boolean): PrimaryKey {
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
                   val cv: String?): PrimaryKey {
    override fun primaryKey(): String {
        return "id"
    }
}

val String.date: Date
    get() {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"))
        return sdf.parse(this)
    }

 val Date.string: String
     get() {
         val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
         sdf.setTimeZone(TimeZone.getTimeZone("GMT"))
         return sdf.format(this)
     }

