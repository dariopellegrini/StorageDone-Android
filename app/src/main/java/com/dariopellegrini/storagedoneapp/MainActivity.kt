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

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"))
        val date2011 = sdf.parse("14-03-2011")
        val date2015 = sdf.parse("14-03-2015")
        val date2019 = sdf.parse("14-03-2019")
        val date2021 = sdf.parse("14-03-2021")

        database = StorageDoneDatabase("pets")
//        val database2 = StorageDoneDatabase(this, "teachers")

        val pets = arrayOf(Pet("id1", "Engineer", 10, Home("idHome1", "Home1", Address("Street1", "City1")), true, listOf("1", "2", "3"), date2011),
            Pet("id12", "Engineer", 11, Home("idHome1", "Home1", Address("Street1", "City1")), true, listOf("1", "2", "3"), date2019),
            Pet("id2", "Pet 2", 11, Home("idHome2", "Home2", Address("Street2", "City2")), null, listOf("4", "5", "6"), date2015),
            Pet("id3", "Pet 3", 12, Home("idHome2", "Home2", Address("Street3", "City3")), false, listOf("7", "8", "9"), date2021))

        val liveQuery = database.live<Pet> {
            petsList ->
            Log.i("LiveQuery", "Count ${petsList.size}")
        }

        val liveQuery2 = database.live<Teacher> {
            petsList ->
            Log.i("LiveQuery", "Count ${petsList.size}")
        }

        CoroutineScope(Dispatchers.IO).launch {
            database.suspending.delete<Pet>()
            database.suspending.insertOrUpdate(pets)

            val databasePets = database.get<Pet> {
                expression = or(Pet::id equal "id1", Pet::name equal "Engineer")
                orderings = arrayOf(Pet::name.ascending(), Pet::date.descending())
            }

            databasePets.forEach {
                Log.i("Pets", it.id)
            }

            val orderedPets = database.get<Pet>()

            orderedPets.forEach {
                Log.i("Ordered", it.id)
            }
        }
    }
}

data class Pet(val id: String, val name: String, val age: Int, val home: Home, val isValid: Boolean?, val list: List<String>, val date: Date): PrimaryKey {
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
