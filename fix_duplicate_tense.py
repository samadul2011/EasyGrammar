import json

# Read the file
with open(r'c:\Users\samad\OneDrive\Desktop\Android\EasyGrammar\app\src\main\res\raw\topics.json', 'r', encoding='utf-8') as f:
    content = f.read()

# Split into lines for manipulation
lines = content.split('\n')

# Remove first Subject-Verb Agreement (lines 1362-1608, 0-indexed: 1361-1607)
print(f"Original lines: {len(lines)}")
new_lines = lines[:1361] + lines[1608:]
print(f"After removing first Subject-Verb Agreement: {len(new_lines)}")

# Join back
new_content = '\n'.join(new_lines)

# Write back
with open(r'c:\Users\samad\OneDrive\Desktop\Android\EasyGrammar\app\src\main\res\raw\topics.json', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Removed corrupted Subject-Verb Agreement section")

# Try to parse the JSON to verify it's valid
try:
    data = json.loads(new_content)
    print("✓ JSON is now valid!")
    
    # Count topics in Class 8
    class8_topics = data['Class 8']
    print(f"Class 8 has {len(class8_topics)} topics:")
    for topic_name in class8_topics.keys():
        print(f"  - {topic_name}")
        
except json.JSONDecodeError as e:
    print(f"✗ JSON still has errors: {e}")

