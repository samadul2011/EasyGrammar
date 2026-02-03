package com.samadul.easygrammar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class TopicActivity : AppCompatActivity() {
    private lateinit var textToSpeech: TextToSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic)

        // Initialize Text-to-Speech
        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show()
            }
        }

        val topicRaw = intent.getStringExtra(EXTRA_TOPIC).orEmpty()
        val (className, topicName) = parseTopic(topicRaw)
        val titleView = findViewById<TextView>(R.id.topicTitle)
        val subtitleView = findViewById<TextView>(R.id.topicSubtitle)
        val lessonsHeader = findViewById<TextView>(R.id.lessonsHeader)
        val examplesHeader = findViewById<TextView>(R.id.examplesHeader)
        val practiceHeader = findViewById<TextView>(R.id.practiceHeader)
        val lessonsContainer = findViewById<LinearLayout>(R.id.lessonsContainer)
        val examplesContainer = findViewById<LinearLayout>(R.id.examplesContainer)
        val practiceContainer = findViewById<LinearLayout>(R.id.practiceContainer)

        titleView.text = if (topicName.isBlank()) "Topic" else topicName

        val content = loadTopicContent(className, topicName) ?: defaultTopicContent()

        subtitleView.text = if (className.isBlank()) {
            "Short lessons, examples, and practice drills"
        } else {
            className
        }

        lessonsHeader.text = "Lessons"
        examplesHeader.text = "Examples"
        practiceHeader.text = "Practice"

        val examplesForLessons = if (topicName == "Parts of Speech (basic)") content.examples else emptyList()

        renderCards(lessonsContainer, content.lessons, "Lesson", examplesForLessons)
        renderCards(examplesContainer, content.examples, "Example")
        renderCards(practiceContainer, content.practice, "Practice")

        if (topicName == "Parts of Speech (basic)") {
            examplesContainer.visibility = android.view.View.GONE
            examplesHeader.visibility = android.view.View.GONE
        }

        // Setup test button if test questions exist
        val testHeader = findViewById<TextView>(R.id.testHeader)
        val startTestButton = findViewById<Button>(R.id.startTestButton)
        val prepositionGuideButton = findViewById<Button>(R.id.prepositionListButton)
        if (content.test.isNotEmpty()) {
            testHeader.visibility = android.view.View.VISIBLE
            startTestButton.visibility = android.view.View.VISIBLE
            startTestButton.setOnClickListener {
                showTestDialog(content.test)
            }
        }
        if (topicName == "Prepositions (time/place/movement)") {
            prepositionGuideButton.visibility = android.view.View.VISIBLE
            prepositionGuideButton.setOnClickListener {
                showPrepositionGuide()
            }
        }
    }

    companion object {
        const val EXTRA_TOPIC = "extra_topic"
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    private data class TopicContent(
        val lessons: List<String>,
        val examples: List<String>,
        val practice: List<String>,
        val test: List<TestQuestion> = emptyList()
    )

    private data class TestQuestion(
        val question: String,
        val options: List<String>,
        val answer: String
    )

    private fun parseTopic(raw: String): Pair<String, String> {
        val parts = raw.split(" - ", limit = 2)
        return if (parts.size == 2) {
            parts[0].trim() to parts[1].trim()
        } else {
            "" to raw.trim()
        }
    }

    private fun loadTopicContent(className: String, topicName: String): TopicContent? {
        if (className.isBlank() || topicName.isBlank()) return null
        val jsonText = readRawJson(className)
        val root = JSONObject(jsonText)
        if (!root.has(className)) return null
        val classObj = root.getJSONObject(className)
        if (!classObj.has(topicName)) return null
        val topicObj = classObj.getJSONObject(topicName)

        val lessons = topicObj.getJSONArray("lessons").toStringList()
        val examples = topicObj.getJSONArray("examples").toStringList()
        val practice = topicObj.getJSONArray("practice").toStringList()
        val test = if (topicObj.has("tests")) {
            topicObj.getJSONArray("tests").toTestQuestionList()
        } else {
            emptyList()
        }

        return TopicContent(lessons, examples, practice, test)
    }

    private fun readRawJson(className: String = ""): String {
        val resourceId = if (className.contains("Class 9") || className.contains("Class 10") || className.contains("Class 11-12")) {
            R.raw.classes_9_10_topics
        } else {
            R.raw.topics
        }
        return resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
    }

    private fun defaultTopicContent(): TopicContent {
        return TopicContent(
            lessons = listOf(
                "Overview of this topic.",
                "Key rules and common errors."
            ),
            examples = listOf(
                "Short example sentences will appear here.",
                "Add more examples in topics.json."
            ),
            practice = listOf(
                "Try a short exercise for this topic.",
                "Add practice items in topics.json."
            )
        )
    }

    private fun org.json.JSONArray.toStringList(): List<String> {
        val list = ArrayList<String>()
        for (i in 0 until length()) {
            val obj = optJSONObject(i)
            if (obj != null) {
                val en = obj.optString("en")
                list.add(en)
            } else {
                val text = getString(i)
                list.add(text)
            }
        }
        return list
    }

    private fun org.json.JSONArray.toTestQuestionList(): List<TestQuestion> {
        val list = ArrayList<TestQuestion>()
        for (i in 0 until length()) {
            val obj = getJSONObject(i)
            val question = obj.getString("en")
            val optionsArray = obj.getJSONArray("options")
            val options = mutableListOf<String>()
            for (j in 0 until optionsArray.length()) {
                options.add(optionsArray.getString(j))
            }
            val answer = obj.getString("answer")
            list.add(TestQuestion(question, options, answer))
        }
        return list
    }



    private fun renderCards(
        container: LinearLayout,
        items: List<String>,
        label: String,
        examples: List<String> = emptyList()
    ) {
        container.removeAllViews()
        items.forEachIndexed { index, itemText ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_card)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                if (index > 0) {
                    params.topMargin = dpToPx(10)
                }
                layoutParams = params
                gravity = Gravity.START
            }

            val title = TextView(this).apply {
                val titleText = if (label == "Lesson" && itemText.contains(":")) {
                    itemText.substringBefore(":")
                } else if (label == "Example") {
                    ""
                } else {
                    "$label ${index + 1}"
                }
                text = titleText
                textSize = 16f
                setTextColor(0xFF0F172A.toInt())
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val body = TextView(this).apply {
                val bodyText = if (label == "Lesson" && itemText.contains(":")) {
                    itemText.substringAfter(": ").trim()
                } else {
                    itemText
                }
                text = bodyText
                textSize = 14f
                setTextColor(0xFF334155.toInt())
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
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.topMargin = dpToPx(6)
                layoutParams = params
            }

            item.addView(title)
            item.addView(body)

            if (examples.isNotEmpty() && index < examples.size) {
                val example = TextView(this).apply {
                    text = examples[index]
                    textSize = 13f
                    setTextColor(0xFF64748B.toInt())
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
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.topMargin = dpToPx(4)
                    layoutParams = params
                }
                item.addView(example)
            }

            container.addView(item)
        }
    }

    private fun translateText(text: String) {
        val webView = WebView(this)
        webView.webViewClient = WebViewClient()
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        val encodedText = Uri.encode(text)
        val url = "https://translate.google.com/?sl=en&tl=bn&text=$encodedText&op=translate"
        webView.loadUrl(url)
        
        AlertDialog.Builder(this)
            .setTitle("Translation")
            .setView(webView)
            .setPositiveButton("Add to Learning (+)", { _, _ ->
                addWordToLearning(text)
            })
            .setNegativeButton("Close", null)
            .show()
    }

    private fun addWordToLearning(word: String) {
        val sharedPref = getSharedPreferences("learning_words", MODE_PRIVATE)
        val existingWords = sharedPref.getString("words", "")?.split(",")?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
        
        if (!existingWords.contains(word)) {
            existingWords.add(word)
            sharedPref.edit().putString("words", existingWords.joinToString(",")).apply()
            Toast.makeText(this, "\"$word\" added to learning list", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "\"$word\" already in learning list", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakText(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun showTestDialog(allQuestions: List<TestQuestion>) {
        // Select 5 random questions
        val selectedQuestions = allQuestions.shuffled().take(5)
        val userAnswers = MutableList(5) { "" }
        var currentQuestionIndex = 0

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val scrollView = android.widget.ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addView(container)
        }

        val questionText = TextView(this).apply {
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextIsSelectable(true)
        }

        val optionsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
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
        container.addView(optionsContainer)
        container.addView(nextButton)

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Test - Answer 5 Questions")
            .setView(scrollView)
            .setCancelable(false)
        dialogBuilder.setNegativeButton("Cancel") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val dialog = dialogBuilder.create()

        fun updateQuestion() {
            if (currentQuestionIndex < selectedQuestions.size) {
                val question = selectedQuestions[currentQuestionIndex]
                questionCounter.text = "Question ${currentQuestionIndex + 1} of 5"
                questionText.text = question.question
                
                optionsContainer.removeAllViews()
                question.options.forEach { option ->
                    val radioButton = android.widget.RadioButton(this).apply {
                        text = option
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        if (userAnswers[currentQuestionIndex] == option) {
                            isChecked = true
                        }
                        setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                userAnswers[currentQuestionIndex] = option
                            }
                        }
                    }
                    optionsContainer.addView(radioButton)
                }
                
                nextButton.text = if (currentQuestionIndex == 4) "Submit" else "Next"
            }
        }

        nextButton.setOnClickListener {
            if (userAnswers[currentQuestionIndex].isEmpty()) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (currentQuestionIndex == 4) {
                // Calculate score
                var score = 0
                val results = StringBuilder()
                selectedQuestions.forEachIndexed { index, question ->
                    val isCorrect = userAnswers[index] == question.answer
                    if (isCorrect) score++
                    
                    val statusText = if (isCorrect) "✓ Correct" else "✗ Wrong"
                    results.append("Q${index + 1}: ${question.question}\n")
                    results.append("Your answer: ${userAnswers[index]}\n")
                    results.append("Correct answer: ${question.answer}\n")
                    results.append("Status: $statusText\n\n")
                }
                
                dialog.dismiss()
                
                // Show results
                val message = StringBuilder()
                    .append("Score: $score/5\n\n")
                    .append(results)
                val remark = when (score) {
                    5 -> "Congratulations! You got full marks."
                    in 3..4 -> "Well done! Keep practicing to perfect it."
                    in 1..2 -> "Nice effort! Review the lessons and retry."
                    else -> "Keep trying! Practice makes perfect."
                }

                AlertDialog.Builder(this)
                    .setTitle("Test Results")
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

    private fun showPrepositionGuide() {
        val guide = """
            Time prepositions:
            - at: exact time (at 5 pm)
            - on: days, dates (on Monday, on July 4)
            - in: months, years, long periods (in June, in 2026)

            Place prepositions:
            - in: enclosed spaces (in the room)
            - on: surfaces (on the table)
            - at: specific points (at the door)

            Movement/direction:
            - to/into/onto: motion toward (go to school, jump into water)
            - from/through/across/over: starting point or crossing (from the city, through the tunnel)

            Other uses:
            - by: means of transport (by bus)
            - with: accompaniment or tools (with a pen)
            - during/for: duration (during the journey, for two hours)
            - before/after: order in time (before lunch, after class)
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Preposition guide")
            .setMessage(guide)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun normalizeAnswer(answer: String): String {
        val replacements = listOf(
            "don't" to "do not",
            "doesn't" to "does not",
            "isn't" to "is not",
            "aren't" to "are not",
            "wasn't" to "was not",
            "weren't" to "were not",
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
            .replace('’', '\'')
            .replace("\u2019", "'")
            .replace(Regex("[\"—–,.:;!?]"), " ")

        for ((key, value) in replacements) {
            normalized = normalized.replace(Regex("\\b$key\\b"), value)
        }

        normalized = normalized.replace(Regex("\\s+"), " ").trim()
        return normalized
    }
}
