package com.dariopellegrini.storagedoneapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.dariopellegrini.storagedone.*
import com.dariopellegrini.storagedone.query.*
import com.dariopellegrini.storagedone.sorting.ascending
import com.dariopellegrini.storagedone.sorting.descending
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
            Pet("id12", "Engineer", 11, Home("idHome1", "Home1", Address("Street1", "City1")), true, listOf("1", "2", "3"), date2019),
            Pet("id2", "Pet 2", 11, Home("idHome2", "Home2", Address("Street2", "City2")), null, listOf("4", "5", "6"), date2015),
            Pet("id3", "Pet 3", 12, Home("idHome2", "Home2", Address("Street3", "City3")), false, listOf("7", "8", "9"), date2021))

        val liveQuery = database.live<Pet> {
            petsList ->
            Log.i("LiveQuery", "Count ${petsList.size}")
        }

        CoroutineScope(Dispatchers.IO).launch {
            database.suspending.insertOrUpdate(pets)

            val orderedPets = database.get<Pet>("name".ascending, "date".ascending)

            orderedPets.forEach {
                Log.i("Ordered", it.id)
            }
        }

        try {
            val databasePets: List<Pet> = database.all()

            "id" equal "id1"

            "age" greaterThan 20
            "age" greaterThanOrEqual 20
            "age" lessThan 20
            "age" lessThanOrEqual 20
            "age" between (10 to 20)

            "age".isNull

            "age".isNotNull

            "id" inside listOf("id1", "id2", "id3")
            "array" contains "A1"

            "name" like "A%"

            "city" regex "\\bEng.*e\\b"

            "dateCreated" greaterThan Date()
            "dateCreated" greaterOrEqualThan Date()
            "dateCreated" lessThan Date()
            "dateCreated" lessOrEqualThan Date()
            "dateCreated" betweenDates (Date() to Date())

            Log.i("Pets", "$databasePets")
        } catch (e: Exception) {
            print(e)
        }
        database.insertOrUpdate(Pet("id1", "Engineer", 10, Home("idHome1", "Home1", Address("Street1", "City1")), true, listOf("1", "2", "3"), date2011))

        addButton.setOnClickListener {
            database.insert(Pet("id1", "Engineer", 10, Home("idHome1", "Home1", Address("Street1", "City1")), true, listOf("1", "2", "3"), Date()))
        }

        cancelButton.setOnClickListener {
            liveQuery.cancel()
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