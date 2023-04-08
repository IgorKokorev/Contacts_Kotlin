package contacts

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
// import kotlinx.datetime.*

val phBook = mutableListOf<Record>()
val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

class CLException(s: String): Exception(s)

fun main() {

    do {
        print("Enter action (add, remove, edit, count, info, exit): ")
        try {
            when (readln()) {
                "count" -> count()
                "edit" -> edit()
                "remove" -> remove()
                "add" -> add()
                "info" -> info()
                "exit" -> break
            }
        } catch (e: CLException) {
            println(e)
        }
        println("")
    } while (true)
}

fun edit() {
    if (phBook.size == 0) println("No records to edit!")
    else {
        list()
        print("Select a record: ")
        val indx = getIndex()
        if (phBook[indx].isPerson) editPerson(indx)
        else editOrganization(indx)
        println("The record updated!")
    }
}

fun editPerson(indx: Int) {
    print("Select a field (name, surname, birth, gender, number): ")
    when (readln()) {
        "number" -> {
            print("Enter number: ")
            phBook[indx].setNumber(readln())
        }
        "name" -> {
            print("Enter name: ")
            phBook[indx].name = readln()
        }
        "surname" -> {
            print("Enter surname: ")
            phBook[indx].surname = readln()
        }
        "birth" -> {
            print("Enter the birth date: ")
            phBook[indx].bDay = try {
                LocalDate.parse(readln())
            } catch (e: DateTimeParseException) {
                println("Bad birth date!")
                null
            }
        }
        "gender" -> {
            phBook[indx].gender = when (readln()) {
                "M" -> "M"
                "F" -> "F"
                else -> {
                    println("Bad gender!")
                    "[no data]"
                }
            }
        }
        else -> {
            println("Wrong input!")
            return
        }
    }
}

fun editOrganization(indx: Int) {
    print("Select a field (address, number): ")
    when (readln()) {
        "number" -> {
            print("Enter number: ")
            phBook[indx].setNumber(readln())
        }
        "address" -> {
            print("Enter the address: ")
            phBook[indx].address = readln()
        }
    }
}

private fun getIndex(): Int {
    val indx: Int
    try {
        indx = readln().toInt() - 1
    } catch (e: NumberFormatException) {
        throw CLException("Wrong input!")
    }
    if (indx !in 0..phBook.lastIndex) {
        throw CLException("Wrong input!")
    }
    return indx
}

fun count() {
    println("The Phone Book has ${phBook.size} records.")
}

fun remove() {
    if (phBook.size == 0) println("No records to remove!")
    else {
        list()
        phBook.removeAt(getIndex())
    }
    println("The record removed!")
}

fun add() {

    print("Enter the type (person, organization): ")
    when (readln()) {
        "person" -> addPerson()
        "organization" -> addOrganization()
        else -> throw CLException("Wrong input!")
    }

    println("The record added.")
}

fun addPerson() {
    print("Enter the name: ")
    val name = readln()
    print("Enter the surname: ")
    val surname = readln()
    print("Enter the birth date: ")
    val bDay = try {
        LocalDate.parse(readln())
    } catch (e: DateTimeParseException) {
        println("Bad birth date!")
        null
    }
    print("Enter the gender (M, F): ")
    val gender = when (readln()) {
        "M" -> "M"
        "F" -> "F"
        else -> {
            println("Bad gender!")
            "[no data]"
        }
    }
    print("Enter the number: ")
    val number = readln()
    val person = Person(name, surname, number, bDay, gender)
    phBook.add(person)
}

fun addOrganization() {
    print("Enter the organization name: ")
    val nameOrg = readln()
    print("Enter the address: ")
    val address = readln()
    print("Enter the number: ")
    val number = readln()
    phBook.add(Organization(nameOrg, address, number))
}

fun list() {
    for (i in 0..phBook.lastIndex) {
        println("${i+1}. ${phBook[i].getInfo()}")
    }
}

fun info() {
    list()
    print("Enter index to show info: ")
    val indx = getIndex()
    phBook[indx].printFullInfo()
}

open class Record(val isPerson: Boolean) {
    private val dateCreate: LocalDateTime
    private var dateEdit: LocalDateTime
    init {
        dateCreate = LocalDateTime.now()
        dateEdit = LocalDateTime.now()
    }
    var name = ""
        set(value) {
            dateEdit = LocalDateTime.now()
            field = value
        }
    var surname = ""
        set(value) {
            dateEdit = LocalDateTime.now()
            field = value
        }

    var bDay: LocalDate? = null
        set(value) {
            dateEdit = LocalDateTime.now()
            field = value
        }

    var gender = "[no data]"
        set(value) {
            dateEdit = LocalDateTime.now()
            field = value
        }

    var nameOrg = ""
        set(value) {
            dateEdit = LocalDateTime.now()
            field = value
        }

    var address = ""
        set(value) {
            dateEdit = LocalDateTime.now()
            field = value
        }


    private var phone: String? = null
        get() {
            return field
        }
        set(str) {
            dateEdit = LocalDateTime.now()
            field = if (correctNumberFormat(str!!)) str
            else {
                println("Wrong number format!")
                null
            }
        }

    public val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

    fun setNumber(str: String) {
        this.phone = str
    }
    fun getNumber(): String? {
        return this.phone
    }

    fun getDateCreate(): LocalDateTime {
        return this.dateCreate
    }

    fun getDateEdit(): LocalDateTime {
        return this.dateEdit
    }
    private fun correctNumberFormat(str: String): Boolean {
        //val pnFormat = Regex(pattern = """\+?(\(?[\da-zA-Z]+\)?)?([ -]\(?[\da-zA-Z]{2,}\)?)?([ -][\da-zA-Z]{2,})*""")
        //val pnFormat = Regex(pattern = "\\+?(\\(?[0-9a-zA-Z]+\\)?)?([ -]\\(?[0-9a-zA-Z]+\\)?)?([ -][0-9a-zA-Z]{2,})*")
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

    fun hasNumber(): Boolean {
        return this.phone != null
    }

    open fun getInfo(): String {
        return ""
    }

    open fun printFullInfo() {

    }
}


class Person( name: String, surname: String, phone: String, bDay: LocalDate?, gender: String ) : Record(true) {
    init {
        super.setNumber(phone)
        super.name = name
        super.surname = surname
        super.bDay = bDay
        super.gender = gender
    }



    override fun getInfo(): String {
        return this.name + " " + this.surname
    }

    override fun printFullInfo() {
        println("Name: ${super.name}")
        println("Surname: ${super.surname}")
        println("Birth date: ${this.bDay ?: "[no data]"}")
        println("Gender: ${this.gender}")
        println("Number: ${this.getNumber() ?: "[no data]"}")
        println("Time created: ${this.getDateCreate().format(formatter)}")
        println("Time last edit: ${this.getDateEdit().format(formatter)}")
    }
}

class Organization( nameOrg: String, address: String, phone: String ) : Record(false) {
    init {
        super.setNumber(phone)
        super.nameOrg = nameOrg
        super.address = address
    }

    override fun getInfo(): String {
        return this.nameOrg
    }

    override fun printFullInfo() {
        println("Organization name: ${this.nameOrg}")
        println("Address: ${this.address}")
        println("Number: ${this.getNumber() ?: "[no data]"}")
        println("Time created: ${this.getDateCreate().format(formatter)}")
        println("Time last edit: ${this.getDateEdit().format(formatter)}")
    }
}
