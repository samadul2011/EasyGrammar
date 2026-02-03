import json
import re

# Read the file
with open(r'c:\Users\samad\OneDrive\Desktop\Android\EasyGrammar\app\src\main\res\raw\topics.json', 'r', encoding='utf-8') as f:
    content = f.read()

# Try to identify all duplicate sections
lines = content.split('\n')

# Find all topic names and their line numbers
topic_pattern = r'^\s*"([^"]+)":\s*\{$'
topics = []

for i, line in enumerate(lines):
    match = re.match(topic_pattern, line)
    if match:
        topic_name = match.group(1)
        # Check if it's inside Class 8
        # We need to find where Class 8 starts and ends
        topics.append((i+1, topic_name))  # Convert to 1-based line numbers

print("Found topics at:")
for line_num, name in topics[-30:]:  # Show last 30
    print(f"  Line {line_num}: {name}")

# Find duplicates
from collections import defaultdict
duplicates = defaultdict(list)
for line_num, name in topics:
    duplicates[name].append(line_num)

print("\nDuplicate topics:")
for name, line_nums in duplicates.items():
    if len(line_nums) > 1:
        print(f"  {name}: lines {line_nums}")
