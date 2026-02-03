import json

# Read the file
with open(r'c:\Users\samad\OneDrive\Desktop\Android\EasyGrammar\app\src\main\res\raw\topics.json', 'r', encoding='utf-8') as f:
    lines = f.readlines()

print(f"Original file: {len(lines)} lines")

# Find the end of each corrupted section by looking for the pattern:
# - corrupted section starts without "lessons": [
# - ends with }]\n},\n just before the next correct section

# Strategy: Remove lines in reverse order to preserve line numbers

# 1. Tense (simple): lines 1106-1300 (keep 1301+)
# 2. Subject-Verb Agreement: lines 1557-1803 (keep 1804+)  
# 3. Voice: lines 2048-2250 (keep 2251+)
# 4. Narration: lines 2486-2684 (keep 2685+)
# 5. Modifiers: lines 2917-3127 (keep 3128+)
# 6. Punctuation: lines 3360-3574 (keep 3575+)

# Let's verify the boundaries by checking the lines
def check_line(line_num):
    if line_num <= len(lines):
        return f"Line {line_num}: {lines[line_num-1].strip()[:80]}"
    return f"Line {line_num}: OUT OF RANGE"

print("\nChecking boundaries:")
print("Tense end:", check_line(1300))
print("Tense start correct:", check_line(1301))
print()
print("Subject-Verb end:", check_line(1803))
print("Subject-Verb start correct:", check_line(1804))
print()
print("Voice end:", check_line(2250))
print("Voice start correct:", check_line(2251))
print()
print("Narration end:", check_line(2684))
print("Narration start correct:", check_line(2685))
print()
print("Modifiers end:", check_line(3127))
print("Modifiers start correct:", check_line(3128))
print()
print("Punctuation end:", check_line(3574))
print("Punctuation start correct:", check_line(3575))

# Remove in reverse order (0-indexed, so subtract 1)
sections_to_remove = [
    ("Punctuation", 3359, 3574),  # lines 3360-3574 (0-indexed: 3359-3573)
    ("Modifiers", 2916, 3127),    # lines 2917-3127 (0-indexed: 2916-3126)
    ("Narration", 2485, 2684),    # lines 2486-2684 (0-indexed: 2485-2683)
    ("Voice", 2047, 2250),        # lines 2048-2250 (0-indexed: 2047-2249)
    ("Subject-Verb", 1556, 1803), # lines 1557-1803 (0-indexed: 1556-1802)
    ("Tense", 1105, 1300),        # lines 1106-1300 (0-indexed: 1105-1299)
]

for name, start_idx, end_idx in sections_to_remove:
    # Remove lines from start_idx to end_idx (inclusive, 0-indexed)
    before_count = len(lines)
    lines = lines[:start_idx] + lines[end_idx:]
    after_count = len(lines)
    print(f"\nRemoved {name}: {before_count} -> {after_count} lines (removed {before_count - after_count})")

# Write back
output_file = r'c:\Users\samad\OneDrive\Desktop\Android\EasyGrammar\app\src\main\res\raw\topics.json'
with open(output_file, 'w', encoding='utf-8') as f:
    f.writelines(lines)

print(f"\n✓ Final file: {len(lines)} lines")

# Try to parse JSON
try:
    with open(output_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    print("✓ JSON is VALID!")
    
    class8_topics = data['Class 8']
    print(f"\n✓ Class 8 has {len(class8_topics)} topics:")
    for i, (topic_name, topic_data) in enumerate(class8_topics.items(), 1):
        lessons = len(topic_data.get('lessons', []))
        tests = len(topic_data.get('tests', []))
        print(f"  {i}. {topic_name}: {lessons} lessons, {tests} tests")
        
except json.JSONDecodeError as e:
    print(f"\n✗ JSON ERROR: {e}")
    print(f"  At line {e.lineno}, column {e.colno}")
except Exception as e:
    print(f"\n✗ ERROR: {e}")
