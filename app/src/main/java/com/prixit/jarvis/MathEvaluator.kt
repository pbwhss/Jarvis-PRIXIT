package com.prixit.jarvis

import kotlin.math.pow
import kotlin.math.sqrt

object MathEvaluator {

    fun trySolve(rawInput: String): String? {
        var expr = rawInput.lowercase()
            .replace("what is", "")
            .replace("calculate", "")
            .replace("solve", "")
            .replace("kitna hota hai", "")
            .replace("kitna hai", "")
            .replace("plus", "+")
            .replace("add", "+")
            .replace("jodo", "+")
            .replace("minus", "-")
            .replace("subtract", "-")
            .replace("ghatao", "-")
            .replace("multiplied by", "*")
            .replace("multiply by", "*")
            .replace("multiply", "*")
            .replace("into", "*")
            .replace("guna", "*")
            .replace("x", "*")
            .replace("divided by", "/")
            .replace("divide by", "/")
            .replace("divide", "/")
            .replace("bhag", "/")
            .replace("open bracket", "(")
            .replace("close bracket", ")")
            .trim()

        if (expr.contains("square root of") || expr.contains("root of")) {
            val numStr = expr.replace(Regex("[^0-9.]"), "")
            val num = numStr.toDoubleOrNull()
            if (num != null && num >= 0) {
                val res = sqrt(num)
                return formatNumber(res)
            }
        }

        if (expr.contains("square")) {
            val numStr = expr.replace(Regex("[^0-9.]"), "")
            val num = numStr.toDoubleOrNull()
            if (num != null) {
                val res = num.pow(2)
                return formatNumber(res)
            }
        }

        val mathOnly = expr.replace(Regex("[^0-9+\\-*/().]"), "")
        if (mathOnly.isEmpty() || !mathOnly.any { it.isDigit() }) return null
        if (!mathOnly.any { it == '+' || it == '-' || it == '*' || it == '/' }) return null

        return try {
            val result = evalBodmas(mathOnly)
            if (result.isNaN() || result.isInfinite()) null else formatNumber(result)
        } catch (e: Exception) {
            null
        }
    }

    private fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format("%.2f", value)
        }
    }

    private fun evalBodmas(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm()
                    else if (eat('-'.code)) x -= parseTerm()
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor()
                    else if (eat('/'.code)) x /= parseFactor()
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()
                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                return x
            }
        }.parse()
    }
}
