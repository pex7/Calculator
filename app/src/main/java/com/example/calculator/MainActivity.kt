package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    private lateinit var equationTextInput: EditText
    private val parser = Parser()
    private val digitRegex = "[0-9\\.]".toRegex()
    private var equationCalculated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        equationTextInput = findViewById(R.id.equationText)
        equationTextInput.requestFocus()
        equationTextInput.showSoftInputOnFocus = false

        findViewById<Button>(R.id.buttonZero).setOnClickListener { handleButtonPress(getString(R.string.zero)) }
        findViewById<Button>(R.id.buttonOne).setOnClickListener { handleButtonPress(getString(R.string.one)) }
        findViewById<Button>(R.id.buttonTwo).setOnClickListener { handleButtonPress(getString(R.string.two)) }
        findViewById<Button>(R.id.buttonThree).setOnClickListener { handleButtonPress(getString(R.string.three)) }
        findViewById<Button>(R.id.buttonFour).setOnClickListener { handleButtonPress(getString(R.string.four)) }
        findViewById<Button>(R.id.buttonFive).setOnClickListener { handleButtonPress(getString(R.string.five)) }
        findViewById<Button>(R.id.buttonSix).setOnClickListener { handleButtonPress(getString(R.string.six)) }
        findViewById<Button>(R.id.buttonSeven).setOnClickListener { handleButtonPress(getString(R.string.seven)) }
        findViewById<Button>(R.id.buttonEight).setOnClickListener { handleButtonPress(getString(R.string.eight)) }
        findViewById<Button>(R.id.buttonNine).setOnClickListener { handleButtonPress(getString(R.string.nine)) }
        findViewById<Button>(R.id.buttonAdd).setOnClickListener { handleButtonPress(getString(R.string.add)) }
        findViewById<Button>(R.id.buttonSubtract).setOnClickListener { handleButtonPress(getString(R.string.subtract)) }
        findViewById<Button>(R.id.buttonMultiply).setOnClickListener { handleButtonPress(getString(R.string.multiply)) }
        findViewById<Button>(R.id.buttonDivide).setOnClickListener { handleButtonPress(getString(R.string.divide)) }
        findViewById<Button>(R.id.buttonPercentage).setOnClickListener { handleButtonPress(getString(R.string.percentage)) }
        findViewById<Button>(R.id.buttonDecimal).setOnClickListener { handleButtonPress(getString(R.string.decimal)) }
        findViewById<Button>(R.id.buttonParenthesis).setOnClickListener { handleParenthesisButtonPress() }
        findViewById<Button>(R.id.buttonClear).setOnClickListener { clear() }
        findViewById<Button>(R.id.buttonBackspace).setOnClickListener { handleBackspace() }
        findViewById<Button>(R.id.buttonEquals).setOnClickListener { calculateEquation() }
    }

    private fun handleBackspace() {
        val equationText = equationTextInput.text.toString()
        val cursorPosition = equationTextInput.selectionStart

        if (equationText.isEmpty() || cursorPosition == 0) return

        equationTextInput.setText(equationText.removeRange(cursorPosition - 1, cursorPosition))
        equationTextInput.setSelection(cursorPosition - 1)
    }

    private fun handleButtonPress(value: String) {
        checkEquationCalculated()

        val equationText = equationTextInput.text.toString()
        val cursorPosition = equationTextInput.selectionStart

        if (equationText.isEmpty()) {
            if (!value.matches(digitRegex)) {
                return
            }
            equationTextInput.setText(value)
        } else {
            val startText = equationText.substring(0, cursorPosition)
            val endText = equationText.substring(cursorPosition)
            val newEquationText = "$startText$value$endText"
            equationTextInput.setText(newEquationText)
        }

        equationTextInput.setSelection(cursorPosition + 1)
    }

    private fun handleParenthesisButtonPress() {
        checkEquationCalculated()
        
        val equationText = equationTextInput.text.toString()
        var openingParenthesisCount = 0
        var closingParenthesisCount = 0

        equationText.forEach {
            if (!it.toString().matches(digitRegex)) {
                val itemSymbol = parser.castToSymbol(it.toString())
                if (itemSymbol == Symbol.OPEN_PARENTHESIS) {
                    openingParenthesisCount += 1
                } else if (itemSymbol == Symbol.CLOSE_PARENTHESIS) {
                    closingParenthesisCount += 1
                }
            }
        }

        val lastStringItem = equationText.takeLast(1)

        if (openingParenthesisCount == closingParenthesisCount &&
            !lastStringItem.matches(digitRegex) &&
            lastStringItem != Symbol.CLOSE_PARENTHESIS.entity
        ) {
            handleButtonPress(Symbol.OPEN_PARENTHESIS.entity)
        } else if (openingParenthesisCount > closingParenthesisCount && lastStringItem.matches(digitRegex)) {
            handleButtonPress(Symbol.CLOSE_PARENTHESIS.entity)
        }
    }

    private fun calculateEquation() {
        val result = parser.calculate(equationTextInput.text.toString()).toString()
        equationTextInput.setText(result)
        equationTextInput.setSelection(result.length)
        equationCalculated = true
    }

    private fun checkEquationCalculated() {
        if (equationCalculated) {
            clear()
            equationCalculated = false
        }
    }

    private fun clear() {
        equationTextInput.setText("")
    }
 }