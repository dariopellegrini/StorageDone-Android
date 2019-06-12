package com.dariopellegrini.storagedoneapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.dariopellegrini.storagedone.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = StorageDoneDatabase(this)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                database.suspending.insert(listOf(Pet("id1", "Danny1", 32, Home("home1", "Home1")),
                    Pet("id2", "Danny2", 32, Home("home2", "Home2")),
                    Pet("id3", "Danny2", 32, Home("home1", "Home3"))))

                val results = database.suspending.get<Pet>()

                database.suspending.delete<Pet>(mapOf("id" to "id2"))

                val results2 = database.suspending.get<Pet>()

                Log.i("StorageDone", results.toString())
                Log.i("StorageDone", results.toString())
            } catch (e: Exception) {
                Log.e("Exception", e.localizedMessage)
            }
        }

        database += listOf(Pet("id4", "Danny4", 32, Home("home4", "Home4")),
            Pet("id5", "Danny5", 32, Home("home5", "Home5")),
            Pet("id6", "Danny6", 32, Home("home6", "Home6")))
        val oResults: List<Pet> = database filter mapOf("id" to "id2")
        val allResults: List<Pet> = database.all()

        Log.i("StorageDone", oResults.toString())
        Log.i("StorageDone", allResults.toString())
    }
}

data class Pet(val id: String, val name: String, val age: Int, val home: Home): PrimaryKey {
    override fun primaryKey(): String {
        return "id"
    }
}

data class Home(val id: String, val name: String)

