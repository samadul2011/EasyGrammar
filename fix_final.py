import json

# Read the file
with open(r'c:\Users\samad\OneDrive\Desktop\Android\EasyGrammar\app\src\main\res\raw\topics.json', 'r', encoding='utf-8') as f:
    lines = f.readlines()

print(f"Original: {len(lines)} lines")

# Keep only lines 1-2538 (0-indexed: 0-2537)
lines = lines[:2538]

print(f"Truncated to: {len(lines)} lines")

# Write back
output_file = r'c:\Users\samad\OneDrive\Desktop\Android\EasyGrammar\app\src\main\res\raw\topics.json'
with open(output_file, 'w', encoding='utf-8') as f:
    f.writelines(lines)

# Verify JSON
try:
    with open(output_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    print("\n✓ JSON is VALID!")
    
    class8_topics = data['Class 8']
    print(f"\n✓ Class 8 has {len(class8_topics)} topics:")
    for i, (topic_name, topic_data) in enumerate(class8_topics.items(), 1):
        lessons = len(topic_data.get('lessons', []))
        tests = len(topic_data.get('tests', []))
        print(f"  {i}. {topic_name}: {lessons} lessons, {tests} tests")
        
except json.JSONDecodeError as e:
    print(f"\n✗ JSON ERROR: {e}")
    print(f"  At line {e.lineno}")
