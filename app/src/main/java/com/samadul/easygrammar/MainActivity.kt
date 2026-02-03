package com.samadul.easygrammar

import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val class8Header = findViewById<TextView>(R.id.class8Header)
        val class8Content = findViewById<LinearLayout>(R.id.class8Content)
        val class9Header = findViewById<TextView>(R.id.class9Header)
        val class9Content = findViewById<LinearLayout>(R.id.class9Content)
        val class11Header = findViewById<TextView>(R.id.class11Header)
        val class11Content = findViewById<LinearLayout>(R.id.class11Content)

        setupToggle(class8Header, class8Content, "Class 8")
        setupToggle(class9Header, class9Content, "Class 9-10")
        setupToggle(class11Header, class11Content, "Class 11-12")

        val myWordsButton = findViewById<Button>(R.id.myWordsButton)
        myWordsButton.setOnClickListener {
            showLearningWords()
        }

        val fullGrammarTestButton = findViewById<Button>(R.id.fullGrammarTestButton)
        fullGrammarTestButton.setOnClickListener {
            showFullGrammarTest()
        }

        val class910TestButton = findViewById<Button>(R.id.class910TestButton)
        class910TestButton.setOnClickListener {
            showClassSpecificTest("Class 9-10")
        }

        val class1112TestButton = findViewById<Button>(R.id.class1112TestButton)
        class1112TestButton.setOnClickListener {
            showClassSpecificTest("Class 11-12")
        }
    }

    private fun setupToggle(header: TextView, content: LinearLayout, label: String) {
        header.setOnClickListener {
            val isVisible = content.visibility == View.VISIBLE
            content.visibility = if (isVisible) View.GONE else View.VISIBLE
            header.text = if (isVisible) "$label ▸" else "$label ▾"
        }
    }

    fun onTopicClicked(view: View) {
        val topic = view.tag?.toString()?.trim().orEmpty()
        if (topic.isBlank()) return
        val intent = Intent(this, TopicActivity::class.java)
            .putExtra(TopicActivity.EXTRA_TOPIC, topic)
        startActivity(intent)
    }

    private fun showLearningWords() {
        val sharedPref = getSharedPreferences("learning_words", MODE_PRIVATE)
        val wordsString = sharedPref.getString("words", "")
        val words = if (wordsString.isNullOrBlank()) {
            emptyList()
        } else {
            wordsString.split(",").filter { it.isNotBlank() }
        }

        val message = if (words.isEmpty()) {
            "No words added yet. Start translating words to add them here!"
        } else {
            words.joinToString("\n • ", "Your Learning Words:\n • ")
        }

        AlertDialog.Builder(this)
            .setTitle("My Learning Words")
            .setMessage(message)
            .setPositiveButton("Clear All") { _, _ ->
                if (words.isNotEmpty()) {
                    sharedPref.edit().putString("words", "").apply()
                    showLearningWords()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showFullGrammarTest() {
        // Collect all test questions from all classes and topics
        val allQuestions = mutableListOf<TestQuestion>()
        
        // Load from both JSON files
        val jsonFiles = listOf(
            readRawJson() to "Class 8",
            readRawJson("Class 9-10") to "Class 9-10"
        )
        
        for ((jsonText, _) in jsonFiles) {
            val root = JSONObject(jsonText)
            
            // Iterate through all classes
            val classes = root.keys()
            while (classes.hasNext()) {
                val className = classes.next()
                val classObj = root.getJSONObject(className)
                
                // Iterate through all topics in the class
                val topics = classObj.keys()
                while (topics.hasNext()) {
                    val topicName = topics.next()
                    val topicObj = classObj.getJSONObject(topicName)
                    
                    if (topicObj.has("tests")) {
                        val testArray = topicObj.getJSONArray("tests")
                        for (i in 0 until testArray.length()) {
                            val testItem = testArray.getJSONObject(i)
                            val question = testItem.optString("en", testItem.optString("question", ""))
                            val answer = testItem.optString("answer", "")
                            if (question.isNotEmpty() && answer.isNotEmpty()) {
                                allQuestions.add(TestQuestion(question, answer))
                            }
                        }
                    }
                }
            }
        }
        
        if (allQuestions.isEmpty()) {
            Toast.makeText(this, "No test questions available", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Select 10 random questions (or less if fewer available)
        val selectedQuestions = allQuestions.shuffled().take(10)
        val userAnswers = MutableList(selectedQuestions.size) { "" }
        var currentQuestionIndex = 0

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val questionText = TextView(this).apply {
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextIsSelectable(true)
            customSelectionActionModeCallback = object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    menu?.add(0, 1, 0, "Translate")?.setIcon(android.R.drawable.ic_menu_edit)
                    menu?.add(0, 2, 0, "Speak")?.setIcon(android.R.drawable.ic_lock_silent_mode_off)
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    val start = selectionStart
                    val end = selectionEnd
                    val selectedText = text.substring(start, end)
                    when (item?.itemId) {
                        1 -> translateText(selectedText)
                        2 -> speakText(selectedText)
                    }
                    mode?.finish()
                    return true
                }

                override fun onDestroyActionMode(mode: ActionMode?) {}
            }
        }

        val answerInput = EditText(this).apply {
            hint = "Type your answer here"
            setSingleLine(false)
            minLines = 2
        }

        val questionCounter = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFF666666.toInt())
        }

        val nextButton = Button(this).apply {
            text = "Next"
        }

        container.addView(questionCounter)
        container.addView(questionText)
        container.addView(answerInput)
        container.addView(nextButton)

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Full Grammar Test - ${selectedQuestions.size} Random Questions")
            .setView(container)
            .setCancelable(false)
        dialogBuilder.setNegativeButton("Cancel") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val dialog = dialogBuilder.create()

        fun updateQuestion() {
            if (currentQuestionIndex < selectedQuestions.size) {
                questionCounter.text = "Question ${currentQuestionIndex + 1} of ${selectedQuestions.size}"
                questionText.text = selectedQuestions[currentQuestionIndex].question
                answerInput.setText(userAnswers[currentQuestionIndex])
                nextButton.text = if (currentQuestionIndex == selectedQuestions.size - 1) "Submit" else "Next"
            }
        }

        nextButton.setOnClickListener {
            userAnswers[currentQuestionIndex] = answerInput.text.toString().trim()
            
            if (currentQuestionIndex == selectedQuestions.size - 1) {
                // Calculate score
                var score = 0
                val results = StringBuilder()
                selectedQuestions.forEachIndexed { index, question ->
                    val userAnswerNormalized = normalizeAnswer(userAnswers[index])
                    val correctAnswerNormalized = normalizeAnswer(question.answer)
                    val isCorrect = userAnswerNormalized == correctAnswerNormalized
                    if (isCorrect) score++
                    
                    val statusText = if (isCorrect) "✓ Correct" else "✗ Wrong"
                    results.append("Q${index + 1}: ${question.question}\n")
                    results.append("Your answer: ${userAnswers[index]}\n")
                    results.append("Correct answer: ${question.answer}\n")
                    results.append("Status: $statusText\n\n")
                }
                
                dialog.dismiss()
                
                // Show results
                val percentage = (score * 100) / selectedQuestions.size
                val message = StringBuilder()
                    .append("Score: $score/${selectedQuestions.size}\n")
                    .append("Percentage: $percentage%\n\n")
                    .append(results)
                val remark = when {
                    score == selectedQuestions.size -> "Perfect! You have mastered English Grammar!"
                    percentage >= 80 -> "Excellent! You have great English Grammar knowledge."
                    percentage >= 60 -> "Good! Keep practicing to improve further."
                    percentage >= 40 -> "Nice effort! Review the lessons and retry."
                    else -> "Keep trying! Practice makes perfect."
                }

                AlertDialog.Builder(this)
                    .setTitle("Full Grammar Test Results")
                    .setMessage("${message}\n$remark")
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                currentQuestionIndex++
                updateQuestion()
            }
        }

        updateQuestion()
        dialog.show()
    }

    private fun showClassSpecificTest(targetClass: String) {
        // Collect all test questions from the specific class
        val allQuestions = mutableListOf<TestQuestion>()
        
        val jsonText = readRawJson(targetClass)
        val root = JSONObject(jsonText)
        
        // Iterate through all classes in the JSON
        val classes = root.keys()
        while (classes.hasNext()) {
            val className = classes.next()
            
            // Check if this class matches the target class
            if (className == targetClass) {
                val classObj = root.getJSONObject(className)
                
                // Iterate through all topics in the class
                val topics = classObj.keys()
                while (topics.hasNext()) {
                    val topicName = topics.next()
                    val topicObj = classObj.getJSONObject(topicName)
                    
                    if (topicObj.has("tests")) {
                        val testArray = topicObj.getJSONArray("tests")
                        for (i in 0 until testArray.length()) {
                            val testItem = testArray.getJSONObject(i)
                            val question = testItem.optString("en", testItem.optString("question", ""))
                            val answer = testItem.optString("answer", "")
                            if (question.isNotEmpty() && answer.isNotEmpty()) {
                                allQuestions.add(TestQuestion(question, answer))
                            }
                        }
                    }
                }
            }
        }
        
        if (allQuestions.isEmpty()) {
            Toast.makeText(this, "No test questions available for $targetClass", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Select 20 random questions (or less if fewer available)
        val selectedQuestions = allQuestions.shuffled().take(20)
        val userAnswers = MutableList(selectedQuestions.size) { "" }
        var currentQuestionIndex = 0

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val questionText = TextView(this).apply {
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextIsSelectable(true)
            customSelectionActionModeCallback = object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    menu?.add(0, 1, 0, "Translate")?.setIcon(android.R.drawable.ic_menu_edit)
                    menu?.add(0, 2, 0, "Speak")?.setIcon(android.R.drawable.ic_lock_silent_mode_off)
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    val start = selectionStart
                    val end = selectionEnd
                    val selectedText = text.substring(start, end)
                    when (item?.itemId) {
                        1 -> translateText(selectedText)
                        2 -> speakText(selectedText)
                    }
                    mode?.finish()
                    return true
                }

                override fun onDestroyActionMode(mode: ActionMode?) {}
            }
        }

        val answerInput = EditText(this).apply {
            hint = "Type your answer here"
            setSingleLine(false)
            minLines = 2
        }

        val questionCounter = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFF666666.toInt())
        }

        val nextButton = Button(this).apply {
            text = "Next"
        }

        container.addView(questionCounter)
        container.addView(questionText)
        container.addView(answerInput)
        container.addView(nextButton)

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("$targetClass Grammar Test - ${selectedQuestions.size} Random Questions")
            .setView(container)
            .setCancelable(false)
        dialogBuilder.setNegativeButton("Cancel") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val dialog = dialogBuilder.create()

        fun updateQuestion() {
            if (currentQuestionIndex < selectedQuestions.size) {
                questionCounter.text = "Question ${currentQuestionIndex + 1} of ${selectedQuestions.size}"
                questionText.text = selectedQuestions[currentQuestionIndex].question
                answerInput.setText(userAnswers[currentQuestionIndex])
                nextButton.text = if (currentQuestionIndex == selectedQuestions.size - 1) "Submit" else "Next"
            }
        }

        nextButton.setOnClickListener {
            userAnswers[currentQuestionIndex] = answerInput.text.toString().trim()
            
            if (currentQuestionIndex == selectedQuestions.size - 1) {
                // Calculate score
                var score = 0
                val results = StringBuilder()
                selectedQuestions.forEachIndexed { index, question ->
                    val userAnswerNormalized = normalizeAnswer(userAnswers[index])
                    val correctAnswerNormalized = normalizeAnswer(question.answer)
                    val isCorrect = userAnswerNormalized == correctAnswerNormalized
                    if (isCorrect) score++
                    
                    val statusText = if (isCorrect) "✓ Correct" else "✗ Wrong"
                    results.append("Q${index + 1}: ${question.question}\n")
                    results.append("Your answer: ${userAnswers[index]}\n")
                    results.append("Correct answer: ${question.answer}\n")
                    results.append("Status: $statusText\n\n")
                }
                
                dialog.dismiss()
                
                // Show results
                val percentage = (score * 100) / selectedQuestions.size
                val message = StringBuilder()
                    .append("Score: $score/${selectedQuestions.size}\n")
                    .append("Percentage: $percentage%\n\n")
                    .append(results)
                val remark = when {
                    score == selectedQuestions.size -> "Perfect! You have mastered $targetClass Grammar!"
                    percentage >= 80 -> "Excellent! You have great $targetClass Grammar knowledge."
                    percentage >= 60 -> "Good! Keep practicing to improve further."
                    percentage >= 40 -> "Nice effort! Review the lessons and retry."
                    else -> "Keep trying! Practice makes perfect."
                }

                AlertDialog.Builder(this)
                    .setTitle("$targetClass Grammar Test Results")
                    .setMessage("${message}\n$remark")
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                currentQuestionIndex++
                updateQuestion()
            }
        }

        updateQuestion()
        dialog.show()
    }

    private fun readRawJson(className: String = ""): String {
        val resourceId = if (className.contains("Class 9") || className.contains("Class 10") || className.contains("Class 11-12")) {
            R.raw.classes_9_10_topics
        } else {
            R.raw.topics
        }
        return resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun translateText(text: String) {
        // Placeholder for translation
        Toast.makeText(this, "Translation feature: $text", Toast.LENGTH_SHORT).show()
    }

    private fun speakText(text: String) {
        // Placeholder for speech
        Toast.makeText(this, "Speaking: $text", Toast.LENGTH_SHORT).show()
    }

    private fun normalizeAnswer(answer: String): String {
        val replacements = listOf(
            "don't" to "do not",
            "doesn't" to "does not",
            "isn't" to "is not",
            "aren't" to "are not",
            "was not" to "wasn't",
            "were not" to "weren't",
            "haven't" to "have not",
            "hasn't" to "has not",
            "hadn't" to "had not",
            "won't" to "will not",
            "wouldn't" to "would not",
            "can't" to "cannot",
            "couldn't" to "could not",
            "shouldn't" to "should not"
        )

        var normalized = answer.lowercase()
            .replace('\'', '\'')
            .replace("\u2019", "'")
            .replace(Regex("[\"—–,.:;!?]"), " ")

        for ((key, value) in replacements) {
            normalized = normalized.replace(Regex("\\b$key\\b"), value)
        }

        normalized = normalized.replace(Regex("\\s+"), " ").trim()
        return normalized
    }

    private data class TestQuestion(
        val question: String,
        val answer: String
    )
}
