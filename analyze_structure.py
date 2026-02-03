import json
import re

# Read the current broken file
with open(r'c:\Users\samad\OneDrive\Desktop\Android\EasyGrammar\app\src\main\res\raw\topics.json', 'r', encoding='utf-8') as f:
    content = f.read()

# Let me manually fix by identifying each Class 8 topic boundary
# The structure should be:
# "Class 8": {
#   "Topic Name": {
#     "lessons": [...],
#     "tests": [...]
#   },
#   ...
# }

lines = content.split('\n')

# First, let's identify where Class 8 starts and ends
class8_start = None
class8_end = None

for i, line in enumerate(lines):
    if '"Class 8"' in line and '{' in line:
        class8_start = i
        print(f"Class 8 starts at line {i+1}")
    elif class8_start and '"Class 9' in line:
        class8_end = i
        print(f"Class 8 ends at line {i+1}")
        break

if class8_start is None:
    print("Could not find Class 8!")
else:
    print(f"Class 8 spans lines {class8_start+1} to {class8_end+1 if class8_end else 'EOF'}")
    
# Now let's extract and parse just Class 8
# We need to identify proper topic boundaries and remove duplicates

# Pattern: topic starts with "TopicName": {
#  next line should be either "lessons": [ or directly a {
topic_starts = []
for i in range(class8_start+1, class8_end if class8_end else len(lines)):
    line = lines[i].strip()
    # Match lines like: "Topic Name": {
    if line.startswith('"') and '": {' in line:
        topic_name = line.split('"')[1]
        # Check next line
        next_line = lines[i+1].strip() if i+1 < len(lines) else ""
        has_lessons = '"lessons"' in next_line
        topic_starts.append((i+1, topic_name, has_lessons))
        
print(f"\nFound {len(topic_starts)} topic sections:")
for line_num, name, has_lessons in topic_starts:
    status = "✓ OK" if has_lessons else "✗ CORRUPTED"
    print(f"  Line {line_num}: {name} - {status}")
