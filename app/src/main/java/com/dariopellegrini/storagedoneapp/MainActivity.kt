package com.dariopellegrini.storagedoneapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.dariopellegrini.storagedone.*
import com.dariopellegrini.storagedone.query.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*




class MainActivity : AppCompatActivity() {

    lateinit var database: StorageDoneDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"))
        val date2011 = sdf.parse("14-03-2011")
        val date2015 = sdf.parse("14-03-2015")
        val date2019 = sdf.parse("14-03-2019")
        val date2021 = sdf.parse("14-03-2021")

        database = StorageDoneDatabase(this, "pets")

        val pets = listOf(Pet("id1", "Engineer", 10, Home("idHome1", "Home1", Address("Street1", "City1")), true, listOf("1", "2", "3"), date2011),
            Pet("id2", "Pet 2", 11, Home("idHome2", "Home2", Address("Street2", "City2")), null, listOf("4", "5", "6"), date2015),
            Pet("id3", "Pet 3", 12, Home("idHome2", "Home2", Address("Street3", "City3")), false, listOf("7", "8", "9"), date2021))

        database += pets

        try {
            val databasePets: List<Pet> = database.get(
                    or(
                        and(
                            "id" equal "id1",
                            "age" equal 10
                        ),
                        "age" greaterThanOrEqual 11,
                        "home" equal Home("idHome21", "Home2", Address("Street3", "City3")),
                        "date" betweenDates (date2019 to date2021)
                )
            )
            Log.i("Pets", "$databasePets")
        } catch (e: Exception) {
            print(e)
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
                   val cv: String?): PrimaryKey {
    override fun primaryKey(): String {
        return "id"
    }
}