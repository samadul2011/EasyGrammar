import json
import sys

# Ensure UTF-8 output in terminal
if sys.stdout.encoding != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8')

# Read the JSON file with UTF-8 encoding
with open('app/src/main/res/raw/topics.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

# Class 9 topics (from activity_main.xml)
class_9_topics = {
    "Parts of Speech (word form)": {
        "lessons": [
            {"en": "Understanding word forms: noun, verb, adjective variations"}
        ],
        "examples": [
            {"en": "run (verb) - runner (noun) - running (adjective)"}
        ],
        "practice": [
            {"en": "Convert verbs to noun forms"}
        ],
        "tests": [{"en": "What is the noun form of 'happy'?", "options": ["happily", "happiness", "happy"], "answer": "happiness"}]
    },
    "Sentences (phrase vs clause, simple/compound)": {
        "lessons": [
            {"en": "Understanding phrases vs clauses"}
        ],
        "examples": [
            {"en": "Phrase: 'in the morning' / Clause: 'when the sun rises'"}
        ],
        "practice": [
            {"en": "Identify phrases and clauses"}
        ],
        "tests": [{"en": "Is 'in the park' a phrase or clause?", "options": ["phrase", "clause"], "answer": "phrase"}]
    },
    "Articles (exceptions)": {
        "lessons": [
            {"en": "Special uses of articles"}
        ],
        "examples": [
            {"en": "The earth revolves around the sun"}
        ],
        "practice": [
            {"en": "Fill in the correct article"}
        ],
        "tests": [{"en": "Complete: ___ Nile is a river", "options": ["A", "The", "An"], "answer": "The"}]
    },
    "Prepositions (collocations)": {
        "lessons": [
            {"en": "Common preposition combinations"}
        ],
        "examples": [
            {"en": "look forward to, depend on, take care of"}
        ],
        "practice": [
            {"en": "Fill in the correct preposition"}
        ],
        "tests": [{"en": "I look ___ to the vacation", "options": ["for", "at", "forward"], "answer": "forward"}]
    },
    "Tense (continuous/perfect)": {
        "lessons": [
            {"en": "Present and past continuous tenses"}
        ],
        "examples": [
            {"en": "I am reading. / I was reading."}
        ],
        "practice": [
            {"en": "Convert to continuous form"}
        ],
        "tests": [{"en": "Convert 'He reads' to continuous", "options": ["He is reading", "He reads", "He read"], "answer": "He is reading"}]
    },
    "Subject-Verb Agreement (tricky subjects)": {
        "lessons": [
            {"en": "Agreement with collective nouns and plural subjects"}
        ],
        "examples": [
            {"en": "The team are ready. / The committee has decided."}
        ],
        "practice": [
            {"en": "Choose the correct verb form"}
        ],
        "tests": [{"en": "The team ___ playing well", "options": ["is", "are"], "answer": "are"}]
    }
}

# Class 10 topics (same as Class 9 per NCTB)
class_10_topics = class_9_topics.copy()

# Class 11 topics
class_11_topics = {
    "Conditional Clauses": {
        "lessons": [
            {"en": "First, second, and third conditionals"}
        ],
        "examples": [
            {"en": "If I study, I pass. / If I studied, I would pass."}
        ],
        "practice": [
            {"en": "Complete conditional sentences"}
        ],
        "tests": [{"en": "If I had known, I ___ told you", "options": ["would have", "will", "would"], "answer": "would have"}]
    },
    "Relative Clauses": {
        "lessons": [
            {"en": "Restrictive and non-restrictive relative clauses"}
        ],
        "examples": [
            {"en": "The boy who studies hard passes. / John, who studies hard, passes."}
        ],
        "practice": [
            {"en": "Identify relative clauses"}
        ],
        "tests": [{"en": "Complete: The person ___ called is here", "options": ["which", "who", "that"], "answer": "who"}]
    },
    "Modal Verbs (advanced)": {
        "lessons": [
            {"en": "Advanced uses of modals: might, must, should, could, would"}
        ],
        "examples": [
            {"en": "You should study more. / It must be true."}
        ],
        "practice": [
            {"en": "Fill in the correct modal"}
        ],
        "tests": [{"en": "You ___ see this movie", "options": ["must", "could", "should"], "answer": "must"}]
    },
    "Reported Speech (advanced)": {
        "lessons": [
            {"en": "Converting complex direct and indirect speech"}
        ],
        "examples": [
            {"en": "Direct: 'I will go tomorrow.' / Indirect: He said he would go the next day."}
        ],
        "practice": [
            {"en": "Convert to reported speech"}
        ],
        "tests": [{"en": "He said, 'I am tired.' becomes He said he ___ tired", "options": ["was", "is", "were"], "answer": "was"}]
    }
}

# Class 12 topics (same as Class 11 per NCTB)
class_12_topics = class_11_topics.copy()

# Add the classes to the data
data["Class 9"] = class_9_topics
data["Class 10"] = class_10_topics
data["Class 11"] = class_11_topics
data["Class 12"] = class_12_topics

# Now consolidate per NCTB standards
consolidated = {}

# Class 8
if "Class 8" in data:
    consolidated["Class 8"] = data["Class 8"]
    print("[OK] Class 8 kept as is")

# Merge Class 9 and 10
if "Class 9" in data and "Class 10" in data:
    merged_9_10 = {}
    merged_9_10.update(data["Class 9"])
    # Class 10 topics should be the same, but if any duplicates, Class 9 takes priority
    merged_9_10.update(data["Class 10"])
    consolidated["Class 9-10"] = merged_9_10
    print("[OK] Class 9 and Class 10 merged into Class 9-10")

# Merge Class 11 and 12
if "Class 11" in data and "Class 12" in data:
    merged_11_12 = {}
    merged_11_12.update(data["Class 11"])
    # Class 12 topics should be the same, but if any duplicates, Class 11 takes priority
    merged_11_12.update(data["Class 12"])
    consolidated["Class 11-12"] = merged_11_12
    print("[OK] Class 11 and Class 12 merged into Class 11-12")

# Write the consolidated JSON
with open('app/src/main/res/raw/topics.json', 'w', encoding='utf-8') as f:
    json.dump(consolidated, f, ensure_ascii=False, indent=2)

print("\n[OK] Classes consolidated successfully!")
print("Final structure: " + str(list(consolidated.keys())))
