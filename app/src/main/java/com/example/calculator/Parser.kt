package com.example.calculator

import java.lang.Math.abs
import java.util.*

enum class Symbol(val entity: String, val precedence: Int) {
    SUBTRACT("–", 2),
    ADD("+", 2),
    DIVIDE("÷", 3),
    MULTIPLY("×", 3),
    PERCENTAGE("%", 4),
    OPEN_PARENTHESIS("(", 1),
    CLOSE_PARENTHESIS(")", 1);

    companion object {
        fun from(str: String) = when(str) {
            "+" -> ADD
            "–" -> SUBTRACT
            "×" -> MULTIPLY
            "÷" -> DIVIDE
            "%" -> PERCENTAGE
            "(" -> OPEN_PARENTHESIS
            ")" -> CLOSE_PARENTHESIS
            else -> "Unknown symbol"
        }
    }
}

class Parser {
    private var equationList = listOf<String>()
    private val postFixQueue = mutableListOf<Any>()
    private val operatorStack = Stack<String>()

    companion object {
        private const val TAG = "Parser"
        // split string by symbols
        private val delimiters = "(?=[^0-9\\.])|(?<=[^0-9\\.])".toRegex()
    }

    fun calculate(equation: String): Number {
        splitEquation(equation)

        enqueuePostFixEquation(equationList)

        val doubleResult = evaluatePostFixQueue(postFixQueue)

        postFixQueue.clear()
        operatorStack.clear()

        return if (abs(doubleResult.rem(1)).equals(0.0)) {
            doubleResult.toInt()
        } else {
            doubleResult
        }
    }

    private fun splitEquation(equation: String) {
        equationList = equation
            .split(delimiters)
            .filter { it != "" }
    }

    private fun enqueuePostFixEquation(equation: List<String>) {
        // use the shunting-yard algorithm to transform to postfix notation

        equation.forEach {
            when(val item = castFromString(it)) {
                is Int, is Double -> postFixQueue.add(item)
                else -> pushOperand(item.toString())
            }
        }

        while(operatorStack.isNotEmpty()) {
            postFixQueue.add(operatorStack.pop())
        }
    }

    private fun castFromString(item: String): Any {
        val int = item.toIntOrNull()
        val double = item.toDoubleOrNull()

        if (int != null) {
            return int
        }

        if (double != null) {
            return double
        }

        return item
    }

    private fun pushOperand(item: String) {
        if (operatorStack.isEmpty()) {
            operatorStack.push(item)
            return
        }

        var headOfStackSymbol = castToSymbol(operatorStack.peek())
        val itemSymbol = castToSymbol(item)

        // if item is closing parenthesis, pop from the operand stack until
        // we get to the opening parenthesis, then pop that from the stack and discard
        if (itemSymbol == Symbol.CLOSE_PARENTHESIS) {
            while (headOfStackSymbol != Symbol.OPEN_PARENTHESIS && operatorStack.isNotEmpty()) {
                postFixQueue.add(operatorStack.pop())
                if (operatorStack.isNotEmpty()) {
                    headOfStackSymbol = castToSymbol(operatorStack.peek())
                }
            }

            operatorStack.pop()
            return
        }

        // if the head of the stack has an equal or higher precedence than the item
        // to be pushed, pop from the stack and enqueue item until the head has a lower precedence
        while (shouldEnqueue(itemSymbol, headOfStackSymbol)) {
            postFixQueue.add(operatorStack.pop())
            if (operatorStack.isNotEmpty()) {
                headOfStackSymbol = castToSymbol(operatorStack.peek())
            }
        }
        operatorStack.push(item)
    }

    private fun shouldEnqueue(itemSymbol: Symbol, headOfStackSymbol: Symbol) =
        (itemSymbol != Symbol.OPEN_PARENTHESIS &&
                headOfStackSymbol != Symbol.OPEN_PARENTHESIS &&
                headOfStackSymbol.precedence >= itemSymbol.precedence) &&
                operatorStack.isNotEmpty()

    private fun evaluatePostFixQueue(queue: MutableList<Any>): Double {
        if (queue.isEmpty()) {
            return 0.0
        }

        val resultStack = Stack<Double>()

        queue.forEach {
            if (it is Number) {
                resultStack.push(it.toDouble())
            } else {
                val symbol = castToSymbol(it.toString())
                if (symbol == Symbol.PERCENTAGE) {
                    val result = equatePercentage(resultStack.pop())
                    resultStack.push(result)
                } else if (resultStack.size >= 2) {
                    val right = resultStack.pop()
                    val left = resultStack.pop()
                    val result = equate(left, right, symbol)
                    resultStack.push(result)
                }
            }
        }

        val result = resultStack.pop()
        resultStack.clear()

        return result
    }

    private fun equatePercentage(num: Double): Double {
        return num / 100
    }

    private fun equate(num1: Double, num2: Double, symbol: Symbol): Double {
        return when (symbol) {
            Symbol.MULTIPLY -> num1 * num2
            Symbol.DIVIDE -> num1 / num2
            Symbol.ADD -> num1 + num2
            Symbol.SUBTRACT -> num1 - num2
            else -> 0.0
        }
    }

    fun castToSymbol(item: String): Symbol {
        return Symbol.from(item) as Symbol
    }
}