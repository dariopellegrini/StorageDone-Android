package com.dariopellegrini.storagedoneapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.couchbase.lite.*
import com.dariopellegrini.storagedone.*
import com.dariopellegrini.storagedone.query.*
import com.dariopellegrini.storagedone.sorting.ascending
import java.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmName
import kotlin.reflect.typeOf


class MainActivity : AppCompatActivity() {
    lateinit var database: StorageDoneDatabase

    val channel = ConflatedBroadcastChannel<List<Teacher>>()

    @OptIn(ExperimentalStdlibApi::class)
    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StorageDoneDatabase.configure(this)

        database = StorageDoneDatabase("pets")

        CoroutineScope(Dispatchers.IO).launch {
            database.suspending.bindCollect(channel)
        }

        CoroutineScope(Dispatchers.IO).launch {

            database.suspending.delete<Teacher>()

            channel.asFlow().onEach {
                Log.i("BroadcastChannel", "${it.size}")
            }.launchIn(this)

            val (variable, flow) = database.channel<Teacher>()
            flow.launchIn(this)

            val teacher1 = Teacher("a1", "Silvia", "B", 30, "https://cv.com/silviab")
            database.suspending.upsert(teacher1)

            val job = CoroutineScope(Dispatchers.IO).launch {
                database.suspending.live<Teacher> {
                    expression = "id".equal("a3")
                    orderings = arrayOf("id".ascending)
                }.onEach {
                    Log.i("Flow", "${it.map { it.id }}")
                }.launchIn(this)
            }

            delay(1000L)
            val teacher2 = Teacher("a2", "Silvia", "B", 30, "https://cv.com/silviab")
            database.suspending.upsert(teacher2)

            delay(1000)

            val teacher3 = Teacher("a3", "Silvia", "B", 30, "https://cv.com/silviab")
            database.suspending.upsert(teacher3)

            delay(1000)

            val teacher4 = Teacher("a4", "Silvia", "B", 30, "https://cv.com/silviab")
            database.suspending.upsert(teacher4)

            delay(1000)

            val teacher5 = Teacher("a5", "Silvia", "B", 30, "https://cv.com/silviab")
            database.suspending.upsert(teacher5)

            val teachers = database.suspending.get<Teacher>()

            database.suspending.delete(teacher3)
            database.suspending.delete(teacher4)
            database.suspending.delete(teacher5)

            val teachers2 = database.suspending.get<Teacher>()

            Log.i("LogsList", "Teachers ${teachers2.size}")

            val p1 = Followed<Human>(1, Human("D", "P"), true)
            val e1 = Followed<Elf>(2, Elf("D", "P"), true)

            database.suspending.upsert(p1)
            database.suspending.upsert(e1)

            val humans = database.suspending.get<Followed<Human>>()

            val elves = database.suspending.get<Followed<Elf>>()

            println(humans)
            println(elves)

            database.suspending.purgeDeletedDocuments()

            val query = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(database.database))
                .where(Meta.deleted)
            val rs = query.execute()
            val count = rs.allResults().size

            Log.i("LogsList", "${count}")
            Log.i("LogsList", "${elves}")
            Log.i("LogsList", "${database.get<Teacher>()}")

//            database.clear()
//
//            val newGirl1 = Girl(11, "Lara", 21, listOf("Bs"), Date())
//            val newGirl2 = Girl(12, "Lara1", 21, listOf("Bs"), Date())
//
//            database.suspending.insertOrUpdate(listOf(newGirl1, newGirl2))
//
//            val newGirl1Null = Girl(11, "Lara", 21, null, Date())
//            val newGirl2Null = Girl(12, "Lara", 21, null, Date())
//
//            database.suspending.insertOrUpdate(listOf(newGirl1, newGirl2))
//
//            database.insertOrUpdate(arrayListOf(newGirl1Null, newGirl2Null), "name" equal "Lara")
//
////            database.insertOrUpdate(listOf(newGirl1Null, newGirl2Null)) {
////                and("id" equal it.id, "cities".isNull)
////            }
//
//            val newGirls = database.suspending.get<Girl>()
//
//            println(newGirls)
//            println(newGirls)
        }
    }
}

data class Pet(val id: String, val name: String, val age: Int, val home: Home, val isValid: Boolean?, val list: List<String>, val date: Date): PrimaryKey {
    override fun primaryKey(): String {
        return "id"
    }
}

data class Girl(val id: Int, val name: String, val age: Int, val cities: List<String>?, val date: Date): PrimaryKey {
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

data class Human(val name: String?, val surname: String)
data class Elf(val name: String?, val surname: String)

data class Followed<T: Any>(val id: Int, val value: T, var followed: Boolean): PrimaryKey {
    override fun primaryKey(): String {
        return "id"
    }
}
