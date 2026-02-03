import json

# Read the file
with open(r'c:\Users\samad\OneDrive\Desktop\Android\EasyGrammar\app\src\main\res\raw\topics.json', 'r', encoding='utf-8') as f:
    lines = f.readlines()

print(f"Original: {len(lines)} lines")

# Find where Class 8 actually ends
# Look for the pattern:
#       ]
#     }
#   }
# }

# The file should end after the first occurrence of this pattern after Punctuation tests

for i in range(len(lines)-1, 0, -1):
    line = lines[i].strip()
    if line == '}' and i > 2500:  # After line 2500
        # Check if this is the final closing brace
        prev1 = lines[i-1].strip() if i > 0 else ""
        prev2 = lines[i-2].strip() if i > 1 else ""
        prev3 = lines[i-3].strip() if i > 2 else ""
        
        # Pattern: ]  then }  then }  then }
        if prev1 == '}' and prev2 == '}' and prev3 == ']':
            print(f"\nFound proper end at line {i+1}")
            print(f"  Line {i-2}: {prev3}")
            print(f"  Line {i-1}: {prev2}")
            print(f"  Line {i}: {prev1}")
            print(f"  Line {i+1}: {line}")
            
            # Truncate the file here
            lines = lines[:i+1]
            break

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
