package contacts.Contacts

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.io.File

class LocalDateTimeAdapter : JsonAdapter<LocalDateTime>(){
    override fun toJson(writer: JsonWriter, value: LocalDateTime?) {
        value?.let { writer?.value(it.format(formatter)) }

    }

    override fun fromJson(reader: JsonReader): LocalDateTime? {
        return if (reader.peek() != JsonReader.Token.NULL) {
            fromNonNullString(reader.nextString())
        } else {
            reader.nextNull<Any>()
            null
        }    }

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    private fun fromNonNullString(nextString: String) : LocalDateTime = LocalDateTime.parse(nextString, formatter)

}

class LocalDateAdapter : JsonAdapter<LocalDate>(){
    override fun toJson(writer: JsonWriter, value: LocalDate?) {
        value?.let { writer?.value(it.format(formatter)) }
    }

    override fun fromJson(reader: JsonReader): LocalDate? {
        return if (reader.peek() != JsonReader.Token.NULL) {
            fromNonNullString(reader.nextString())
        } else {
            reader.nextNull<Any>()
            null
        }
    }
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private fun fromNonNullString(nextString: String) : LocalDate = LocalDate.parse(nextString, formatter)

}

val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .add(LocalDateTime::class.java, LocalDateTimeAdapter().nullSafe())
    .add(LocalDate::class.java, LocalDateAdapter().nullSafe())
    .build()

val recAdapter = moshi.adapter(Contact::class.java)
val type = Types.newParameterizedType(List::class.java, Contact::class.java)
val recListAdapter = moshi.adapter<MutableList<Contact?>>(type)

class CLException(s: String): Exception(s)
const val WR_INP = "Wrong input!"
const val EMPTY_LIST = "The list is empty."

var phBook = mutableListOf<Contact>()

fun main(args: Array<String>) {

    // if there's input argument read-write file
    val isFile: Boolean
    if (args.size > 0) {
        isFile = true
        val file = File(args[0])
        if (file.exists()) {
            val readFile = file.readText()
            val dbFromJson = recListAdapter.fromJson(readFile)
            if (dbFromJson != null) {
                for (rec in dbFromJson) {
                    if (rec != null) {
                        phBook.add(rec)
                    }
                }
            }
            println("open ${args[0]}\n")

        }
    } else isFile = false

    // main menu
    do {
        print("[menu] Enter action (add, list, search, count, exit): ")
        try {
            when (readln()) {
                "add" -> add()
                "list" -> list()
                "search" -> search()
                "count" -> count()
                "exit" -> break
            }
        } catch (e: CLException) {
            println(e.message)
        }
        println("")
    } while (true)

    // write file if there was an argument
    if (isFile) {
        val file = File(args[0])
        file.writeText(recListAdapter.toJson(phBook as MutableList<Contact?>))
    }
}

fun list() {
    if (phBook.isEmpty()) throw CLException(EMPTY_LIST)
    printList(phBook)
    print("\n[list] Enter action ([number], back): ")
    val inp = readln()
    if (inp.equals("back")) return
    val i: Int
    try {
        i = inp.toInt()
    } catch (e: NumberFormatException) {
        throw CLException(WR_INP)
    }
    if (i <= 0 || i > phBook.size) throw CLException(WR_INP)

    editRecord(phBook[i-1])
}
fun printList(list: MutableList<Contact>) {
    for (i in 0..list.lastIndex) {
        println("${i+1}. ${list[i].getInfo()}")
    }
}

fun editRecord(rec: Contact) {
    while (true) {
        rec.printFullInfo()
        print("\n[record] Enter action (edit, delete, menu): ")
        when (readln()) {
            "edit" -> editRec(rec)
            "delete" -> {
                phBook.remove(rec)
                println("The record removed!")
            }
            "menu" -> return
            else -> throw CLException(WR_INP)
        }
    }
}

fun editRec(rec: Contact) {
    print("Select a field (" + rec.properties()[0])
    for (i in 1..rec.properties().lastIndex) print(", " + rec.properties()[i])
    print("): ")
    val prop = readln()
    if (!rec.properties().contains(prop)) throw CLException(WR_INP)
    print("Enter " + prop + ": ")
    val value = readln()
    rec.setProperty(prop + rec.splitter + value)
    println("Saved")
}

fun count() {
    println("The Phone Book has ${phBook.size} records.")
}

fun add() {

    print("\n[add] Enter the type (person, organization): ")
    val rec = when (readln()) {
        "person" -> Contact(true)
        "organization" -> Contact(false)
        else -> throw CLException(WR_INP)
    }

    for (prop in rec.properties()) {
        print("Enter " + prop + ": ")
        rec.setProperty(prop + rec.splitter + readln())
    }

    phBook.add(rec)
    println("The record added.")
}

fun search() {
    while (true) {
        print("Enter search query: ")
        val word = readln()
        val searchRes = mutableListOf<Contact>()
        for (rec in phBook) {
            if (rec.contain(word)) searchRes.add(rec)
        }
        printList(searchRes)
        print("\n[search] Enter action ([number], back, again): ")
        val inp = readln()
        if (inp.equals("back")) return
        if (inp.equals("again")) continue
        val i: Int
        try {
            i = inp.toInt()
        } catch (e: NumberFormatException) {
            throw CLException(WR_INP)
        }
        if (i <= 0 || i > searchRes.size) throw CLException(WR_INP)

        editRecord(searchRes[i-1])
        return
    }
}


class Contact(val isPerson: Boolean) {
    private var timeCreate: LocalDateTime
    private var timeEdit: LocalDateTime
    init {
        timeCreate = LocalDateTime.now()
        timeEdit = LocalDateTime.now()
    }

    val splitter = "%$%"
    var name = ""
    var surname = ""
    var birth: LocalDate? = null
    var gender = "[no data]"
    var address = ""


    private var phNumber: String? = null
        private get() {
            return field
        }
        private set(str) {
            timeEdit = LocalDateTime.now()
            field = if (correctNumberFormat(str!!)) str
            else {
                println("Wrong number format!")
                null
            }
        }

    protected val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

    fun properties(): List<String> {
        return if (this.isPerson) listOf("name", "surname", "birth", "gender", "number")
        else listOf("name", "address", "number")
    }

    fun setProperty(str: String) {
        val splitStr = str.split(this.splitter)

        if (splitStr.size != 2) throw CLException(WR_INP)

        if ( !this.properties().contains(splitStr[0]) ) throw CLException(WR_INP)

        when (splitStr[0]) {
            "name" -> this.name = splitStr[1]
            "surname" -> this.surname = splitStr[1]
            "birth" -> this.birth = try {
                LocalDate.parse(splitStr[1])
            } catch (e: DateTimeParseException) {
                println("Bad birth date!")
                null
            }
            "gender" -> {
                this.gender = when (splitStr[1]) {
                    "M" -> "M"
                    "F" -> "F"
                    else -> {
                        println("Bad gender!")
                        "[no data]"
                    }
                }
            }
            "number" -> this.setNumber(splitStr[1])
            "address" -> this.address = splitStr[1]
        }
        this.timeEdit = LocalDateTime.now()
    }

    fun getProperty(str: String) : String {
        if ( !this.properties().contains(str) ) throw CLException(WR_INP)

        return when (str) {
            "name" -> this.name
            "surname" -> this.surname
            "birth" -> this.birth?.toString() ?: "[no data]"
            "gender" -> this.gender
            "address" -> this.address
            "number" -> this.getNumber() ?: "[no data]"
            else -> throw CLException(WR_INP)
        }
    }
    fun setNumber(str: String) {
        this.phNumber = str
    }
    fun getNumber(): String? {
        return this.phNumber
    }
    fun getDateCreate(): LocalDateTime {
        return this.timeCreate
    }

    fun getDateEdit(): LocalDateTime {
        return this.timeEdit
    }
    private fun correctNumberFormat(str: String): Boolean {
        //val pnFormat = Regex(pattern = """\+?(\(?[\da-zA-Z]+\)?)?([ -]\(?[\da-zA-Z]{2,}\)?)?([ -][\da-zA-Z]{2,})*""")
        //val pnFormat = Regex(pattern = "\\+?(\\(?[0-9a-zA-Z]+\\)?)?([ -]\\(?[0-9a-zA-Z]+\\)?)?([ -][0-9a-zA-Z]{2,})*")
        if (str.length == 0) return false
        val nLeftPar = str.filter { it == '(' }.count()
        val nRightPar = str.filter { it == ')' }.count()
        if (nLeftPar != nRightPar || nLeftPar > 1) return false

        val blocks: List<String>
        blocks = if (str[0] == '+') str.substring(1).split(Regex("[ -]"))
        else str.split(Regex("[ -]"))

        if (blocks.size == 0) return false

        if (!(Regex("[0-9a-zA-Z]+").matches(blocks[0]) || Regex("\\([0-9a-zA-Z]+\\)").matches(blocks[0]))) return false

        if (blocks.size == 1) return true

        if (!(Regex("[0-9a-zA-Z]{2,}").matches(blocks[1]) || Regex("\\([0-9a-zA-Z]{2,}\\)").matches(blocks[1]))) return false

        if (blocks.size == 2) return true

        for (i in 2..blocks.lastIndex) {
            if (!Regex("[0-9a-zA-Z]{2,}").matches(blocks[i])) return false
        }

        return true
    }

    fun hasNumber(): Boolean = this.phNumber != null

    fun getInfo(): String {
        return if (this.isPerson) this.name + " " + this.surname
        else this.name
    }

     fun contain( str: String): Boolean {
         val searchFields = this.name + " " + this.surname + " " + this.address +
                            " " + this.getNumber()
         return searchFields.contains(str, true)
     }

    fun printFullInfo() {
        if (this.isPerson) {
            println("Name: ${this.name}")
            println("Surname: ${this.surname}")
            println("Birth date: ${this.birth ?: "[no data]"}")
            println("Gender: ${this.gender}")
        } else {
            println("Organization name: ${this.name}")
            println("Address: ${this.address}")
        }
        println("Number: ${this.getNumber() ?: "[no data]"}")
        println("Time created: ${this.getDateCreate().format(formatter)}")
        println("Time last edit: ${this.getDateEdit().format(formatter)}")
    }
}
