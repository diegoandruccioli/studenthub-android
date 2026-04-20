package com.unibo.android.corsolp2526

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        println("test")
        assertEquals(4, 1 + 2)
    }

    @Test
    fun testListIntegers() {
        val integers = listOf(2, 4, 5)
        println("This list has ${integers.count()} items or ${integers.size} items")
        println("Sum: ${integers.sum()}")
        val avg = integers.average()
        println("Average: $avg")

        val prova = 4 % 2
        val prova2 = 3 % 2
        val listFiltered = integers.filter { it % 2 == 0 }
        val listFiltered1 = integers.filter { it % 2 == 1 }
        println("PARI Filtered list: $listFiltered")
        println("DISPARI Filtered list: $listFiltered1")

        val listFiltered2 = integers.filter { value ->
            println("Sto iterando il valore $value")
            value % 2 == 0
        }
        println("PARI Filtered listFiltered2: $listFiltered2")

        //first e last
        println("first: ${integers.first()} ")
        println("last: ${integers.last()} ")

        println("first condizione: ${integers.first { it > 4 }} ")
    }

    data class Account(val email: String, val age: Int, val type : String?)

    @Test
    fun testAccounts(){
        val account1 = Account("office@unibo.it", 0, "office")
        val accounts = listOf(
            Account("mario.rossi@unibo.it", 20, "student"),
            Account("maria.bianchi@unibo.it", 22, "student"),
            account1,
            Account("undefined", 20, null)
        )
        accounts.forEach { println(it) }

        println("-------------------")
        //Filtrare la lista stampando solo gli elementi di tipo “student”
        accounts.filter { it.type == "student" }.forEach { println(it) }
        println("-------------------")

        //Stampare l’account più vecchio.
        val listSorted = accounts.sortedBy { it.age }
        println("listSorted: $listSorted")
        val lastElement = listSorted.last()
        println("lastElement: $lastElement")
    }
}