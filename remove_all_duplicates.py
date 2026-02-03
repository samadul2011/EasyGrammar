import json

# Read the file
with open(r'c:\Users\samad\OneDrive\Desktop\Android\EasyGrammar\app\src\main\res\raw\topics.json', 'r', encoding='utf-8') as f:
    content = f.read()

# Split into lines for manipulation
lines = content.split('\n')

print(f"Original lines: {len(lines)}")

# Remove duplicates in reverse order (from bottom to top) to preserve line numbers
# 4. Punctuation (basic): Remove lines 2918-3134 (0-indexed: 2917-3133)
lines = lines[:2917] + lines[3134:]
print(f"After removing Punctuation duplicate: {len(lines)}")

# 3. Modifiers (basic): Remove lines 2475-2687 (0-indexed: 2474-2686)
lines = lines[:2474] + lines[2687:]
print(f"After removing Modifiers duplicate: {len(lines)}")

# 2. Narration (statements): Remove lines 2044-2242 (0-indexed: 2043-2241)
lines = lines[:2043] + lines[2242:]
print(f"After removing Narration duplicate: {len(lines)}")

# 1. Voice (simple): Remove lines 1606-1808 (0-indexed: 1605-1807)
lines = lines[:1605] + lines[1808:]
print(f"After removing Voice duplicate: {len(lines)}")

# Join back
new_content = '\n'.join(lines)

# Write back
with open(r'c:\Users\samad\OneDrive\Desktop\Android\EasyGrammar\app\src\main\res\raw\topics.json', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("\nRemoved all corrupted duplicate sections!")

# Try to parse the JSON to verify it's valid
try:
    data = json.loads(new_content)
    print("✓ JSON is now valid!")
    
    # Count topics in Class 8
    class8_topics = data['Class 8']
    print(f"\nClass 8 has {len(class8_topics)} topics:")
    for topic_name in class8_topics.keys():
        topic = class8_topics[topic_name]
        num_lessons = len(topic.get('lessons', []))
        num_tests = len(topic.get('tests', []))
        print(f"  - {topic_name}: {num_lessons} lessons, {num_tests} tests")
        
except json.JSONDecodeError as e:
    print(f"✗ JSON still has errors: {e}")
    print(f"  Error at line {e.lineno}, column {e.colno}")
