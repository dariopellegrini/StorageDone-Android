# StorageDone-Android
Kotlin library to make easy using local document-oriented database in Android apps.

## Installation

Edit your build.gradle file
``` groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Then add as dependency to your app/build.gradle
``` groovy
dependencies {
    ...
    implementation 'com.github.dariopellegrini:StorageDone-Android:v0.2'
}
```

### Usage
StorageDone lets you save Codable models in a local database very easily.

First create a model
```kotlin
data class Teacher(val id: String,
                   val name: String?,
                   val surname: String?,
                   val age: Int?,
                   val cv: String?)
```

Then create a `StorageDoneDatabase` object and save an instance of a Codable model in it
```kotlin
val database = StorageDoneDatabase(context, "teachers")
val teacher = Teacher("id1", "Sarah", "Jones", 29, "https://my.cv.com/sarah_jones")

try {
    database.insert(teacher)
} catch(e: Exception) {
    Log.e("StorageDone", e.localizedMessage)
}
```

Reading database content will retrieve an array of the decleared model
```kotlin
try {
  val teachers = database.get<List<Teacher>>()
} catch(e: Exception) {
  Log.e("StorageDone", e.localizedMessage)
}
```

Other methods allow filtering and deletion.

### Primary key
A model can implement `PrimaryKey` interface, in order to have an attribute set as database primary key
```kotlin
data class Teacher(val id: String,
                   val name: String?,
                   val surname: String?,
                   val age: Int?,
                   val cv: String?): PrimaryKey {
    override fun primaryKey(): String {
        return "id"
    }
}
```

Primary keys come in combination with insert or update methods
```kotlin
val teachers = listOf(Teacher(id: "id1", name: "Sarah", surname: "Jones", age: 29, cv: "https://my.cv.com/sarah_jones"),
                Teacher(id: "id2", name: "Silvia", surname: "Jackson", age: 29, cv: "https://my.cv.com/silvia_jackson"),
                Teacher(id: "id3", name: "John", surname: "Jacobs", age: 30, cv: "https://my.cv.com/john_jackobs"))      
try {
        this.insertOrUpdate(teachers)
    } catch (e: Exception) {
        Log.e("StorageDone", e.localizedMessage)
    }
```

### Coroutines
Every operation has its suspending version. Each can be used through suspendable extension
```kotlin
database.suspending.insertOrUpdate(teachers)

database.suspending.insert(teachers)

val teachers: List<Teacher> = database.suspending.get()

database.suspending.delete(map("id" to "id2"))

```

### Operators
Database objects can use different functions, which wrap try-catch logic and give a more compact way to access database
```kotlin
// Insert or update
database += teachers

// Read
val teachers: List<Teacher> = database.all()

// Filter
val filteredTeachers: List<Teacher> = database filter mapOf("id" to "id2")

// Delete if model implements PrimaryKey protocol
database -= teachers
```

### Queries
Get and delete commands can use queries. Queries can be built in different ways, using custom operators

```kotlin

// Equal
"id" equal "id1"

// Comparison (Numeric only)
"age" greaterThan 20
"age" greaterThanOrEqual 20
"age" lessThan 20
"age" lessThanOrEqual 20
"age" between (10 to 20)

// Is null
"age".isNull

// Is not null
"age".isNotNull

// Value inside array
"id" inside listOf("id1", "id2", "id3")

// Array contains value
"array" contains "A1"

// Like
"name" like "A%"

// Regex
"city" regex "\\bEng.*e\\b"

// Dates comparisons
"dateCreated" greaterThan Date()
"dateCreated" greaterOrEqualThan Date()
"dateCreated" lessThan Date()
"dateCreated" lessOrEqualThan Date()
"dateCreated" betweenDates (Date() to Date())

// And
and(expression1, expression2, expression3)

// Or
or(expression1, expression2, expression3)

database.get(expression)
```

## Live queries
Using live queries it's possible to observe database changes.
```kotlin

// All elements
val liveQuery = database.live<Teacher> {
    teachers ->
        Log.i("LiveQuery", "$teachers")
}

// Elements with query
val liveQuery = database.live<Teacher>("id" equal "id1", "name") {
    -> teachers
        Log.i("LiveQuery", "$teachers")
}
```

To cancel a live query simply call cancel on databse passing the LiveQuery object:
```kotlin
database.cancel(liveQuery)
```

## Author

Dario Pellegrini, pellegrini.dario.1303@gmail.com

## License

StorageDone-Android is available under the MIT license. See the LICENSE file for more info.
