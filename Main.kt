package calculator
import java.math.BigInteger
var exit = false
var map = mutableMapOf<String, String>()
var stack = mutableListOf<String>()

// Execute the command based on input
fun chooseOption(input: String) {
    if (input.isEmpty()) return
    if (input.first() == '/') {
        when (input) {
            "/help" -> {
                help()
                return
            }
            "/exit" -> {
                println("Bye!")
                exit = true
                return
            }
            else -> {
                println("Unknown command")
                return
            }
        }
    }
    if (input.contains("=")) {
        processVariable(input)
    } else if (isVariable(input)) {
        printVariable(input)
    } else {
        calculate(input)
    }
}

// Print help message when /help is entered
fun help() {
    println("This program calculates the sum of numbers")
}

// Process the input with multiple + and -
fun convertOperator(input: String): String {
    val output = input.replace(" ", "")
        .replace("--", "+")
        .replace("\\+\\++".toRegex(), "+")
        .replace("\\+-".toRegex(), "-")
        .replace("-\\+".toRegex(), "-")
    if (output.contains("\\*\\*+|//+".toRegex())) {
        println("Invalid expression")
        return ""
    }
    return output
}

// Support multiplication, integer division and parentheses
fun calculate(input: String) {
    val sepChar = makeCharList(convertOperator(input))
    val inputPostfix = convertPostFix(sepChar)
    if (inputPostfix.isNotEmpty()) {
        val res = calPostfix(convertToDigit(inputPostfix))
        println(res)
    }
}

// If "=" is in the input, check the input & perform assignment when input is valid
fun processVariable(input: String) {
    val output = input.replace(" ", "").split("=")
    val reg = "[a-zA-Z]+=-?\\d+|[a-zA-Z]+=[a-zA-Z]+".toRegex()
    val variable = output[0]
    val num = output[1]
    if (!variable.matches("[a-zA-Z]+".toRegex())) {
        println("Invalid identifier")
    } else if (!input.replace(" ", "").matches(reg)) {
        println("Invalid assignment")
    } else if (!num.matches("-?\\d+".toRegex()) && num !in map.keys) {
        println("Unknown variable")
    } else if (num.matches("[a-zA-Z]+".toRegex()) && variable.matches("[a-zA-Z]+".toRegex())) {
        if (num !in map.keys) {
            println("Unknown variable")
        } else {
            map[variable] = map[num]!!
        }
    } else {
        map[variable] = num
    }
}

// Check if the variable has already been declared or not
fun isVariable(input: String) = input.replace(" ", "") matches ("[a-zA-Z]+".toRegex())

// Print valid variable
fun printVariable(input: String) {
    if (map.containsKey(input.trim())) {
        println(map[input.trim()]!!)
    } else {
        println("Unknown variable")
    }
}

// Convert a list with variables to a list with all "digit" strings
fun convertToDigit(input: List<String>): MutableList<String> {
    val output = mutableListOf<String>()
    for (e in input) {
        if (map.containsKey(e)) {
            output.add(map[e]!!)
        } else if (map.containsKey(e.drop(1))) {
            output.add("-${map[e.drop(1)]!!}")
        } else {
            output.add(e)
        }
    }
    return output
}

// Make a char list
fun makeCharList(input: String): MutableList<String> {
    val output = input.replace(" ", "")
    val res = mutableListOf<Char>() // List of a char from input
    val res2 = mutableListOf<String>() // Combine digit char to a number
    val res3 = mutableListOf<String>() // Combine minus char to a negative number
    for (ch in output) {
        res.add(ch)
    }
    val numList = mutableListOf<String>()
    for (i in res.indices) {
        if (res[i].toString().matches("\\d+|\\w+".toRegex())) {
            numList.add(res[i].toString())
        } else if (numList.isNotEmpty()) {
            res2.add(numList.joinToString(""))
            numList.removeAll(numList)
            res2.add(res[i].toString())
        } else {
            res2.add(res[i].toString())
        }
        if (i == res.lastIndex && numList.isNotEmpty()) {
            res2.add(numList.joinToString(""))
        }
    }
    var foundNegNumber = false
    for (j in res2.indices) {
        // if the first char is "-" then foundNegNumber is true
        if (j == 0 && res2[j] == "-") {
            foundNegNumber = true
        }
        // give the current char is "-" and the previous element is not digit
        else if (res2[j] == "-" && res2[j - 1] == "(") {
            foundNegNumber = true
        } else if (res2[j].matches("\\d+".toRegex()) && foundNegNumber) {
            res3.add("-${res2[j]}")
            foundNegNumber = false
        } else {
            res3.add(res2[j])
        }
    }
    return res3
}

// Convert infix to postfix
fun convertPostFix(inputList: MutableList<String>): MutableList<String> {
    val output = mutableListOf<String>()
    for (i in inputList.indices) {
        val e = inputList[i]
        when {
            e.matches(Regex("-?\\d+|\\w+")) -> output.add(e)
            e == "(" -> stack.add(0, "(")
            stack.isEmpty() || stack.first() == "(" -> stack.add(0, e)
            e == ")" -> {
                stack.add(0, e)
                output.addAll(processRightParenthesis())
            }
            e.isHigher(stack.first()) -> stack.add(0, e)
            e.isLower(stack.first()) || e.isEqual(stack.first()) -> {
                output.addAll(processLowerOrEqual())
                stack.add(0, e)
            }
        }
        if (i == inputList.lastIndex && stack.isNotEmpty()) {
            output.addAll(stack)
            stack.clear()
        }
    }
    if (output.contains("(") || output.contains(")")) {
        println("Invalid expression")
        return emptyList<String>().toMutableList()
    }
    return output
}

// Compare if string has higher precedence
fun String.isHigher(input: String): Boolean {
    return this == "*" && input == "+" ||
            this == "*" && input == "-" ||
            this == "/" && input == "+" ||
            this == "/" && input == "-"
}

// Compare if string has lower precedence
fun String.isLower(input: String): Boolean {
    return this == "+" || this == "-" && input == "*" || this == "-" && input == "/"
}

// Compare if string has equal precedence
fun String.isEqual(input: String): Boolean {
    return this == "+" || this == "-" && input == "+" || input == "-" ||
            this == "*" || this == "/" && input == "*" || input == "/"
}

// Process when incoming element is lower or equal precedence
fun processLowerOrEqual(): MutableList<String> {
    val output = mutableListOf<String>()
    while (stack.isNotEmpty()) {
        if (stack.first() == "(") break
        val remove = stack.removeAt(0)
        output.add(remove)
    }
    return output
}

// Process when incoming element is right parenthesis
fun processRightParenthesis(): MutableList<String> {
    val n = stack.indexOf("(")
    return try {
        val output = stack.subList(1, n)
        stack = stack.drop(n + 1).toMutableList()
        output
    } catch (e: Exception) {
        emptyList<String>().toMutableList()
    }
}

// Calculate the result provided that the input is a postfix list
fun calPostfix(inputPostfix: MutableList<String>): String {
    var stack = mutableListOf<String>()
    for (e in inputPostfix) {
        when {
            e.matches(Regex("-?\\d+|\\w+")) -> stack.add(0, e)
            e.matches("[/+\\-*]".toRegex()) -> stack = processOperator(e, stack)
        }
    }
    return stack.first()
}

// When the element is operator, remove two numbers and operate them.
fun processOperator(operator: String, stack: MutableList<String>): MutableList<String> {
    val b = stack.removeAt(0).toBigInteger()
    val a = if (stack.isNotEmpty()) {
        stack.removeAt(0).toBigInteger()
    } else BigInteger.ZERO
    stack.add(0, operator.operate(a, b))
    return stack
}

// Make the operation based on the operator and 2 ints.
fun String.operate(a: BigInteger = BigInteger.ZERO, b: BigInteger = BigInteger.ZERO): String {
    val res = when (this) {
        "+" -> a + b
        "-" -> a - b
        "*" -> a * b
        "/" -> a / b
        else -> -1
    }
    return res.toString()
}

fun main() {
    while (!exit) {
        val input = readLine()!!
        chooseOption(input)
    }
}