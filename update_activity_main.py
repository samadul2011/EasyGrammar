import re
import sys

# Ensure UTF-8 output
if sys.stdout.encoding != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8')

# Read the XML file
with open('app/src/main/res/layout/activity_main.xml', 'r', encoding='utf-8') as f:
    content = f.read()

# Update Class 9 header to Class 9-10
content = re.sub(
    r'android:id="@\+id/class9Header"[^>]*android:text="Class 9 [^"]*"',
    'android:id="@+id/class9Header"\n            android:layout_width="match_parent"\n            android:layout_height="wrap_content"\n            android:layout_marginTop="12dp"\n            android:background="#EEF2FF"\n            android:padding="12dp"\n            android:text="Class 9-10 [Teaching Content from Both] ▸"',
    content,
    flags=re.DOTALL
)

# Fix the previous replacement more carefully - replace the entire Class 9 section header
old_class9_header = '''        <!-- Class 9 -->
        <TextView
            android:id="@+id/class9Header"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:background="#EEF2FF"
            android:padding="12dp"
            android:text="Class 9 ▸"
            android:textSize="18sp"
            android:textStyle="bold" />'''

new_class9_header = '''        <!-- Class 9-10 (Merged per NCTB) -->
        <TextView
            android:id="@+id/class9Header"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:background="#EEF2FF"
            android:padding="12dp"
            android:text="Class 9-10 ▸"
            android:textSize="18sp"
            android:textStyle="bold" />'''

if old_class9_header in content:
    content = content.replace(old_class9_header, new_class9_header)
    print("[OK] Class 9 header updated to Class 9-10")

# Remove Class 10 entire section (header + content layout)
# Find and remove the Class 10 comment and header
class10_pattern = r'\s*<!-- Class 10 -->.*?</LinearLayout>\s*'
content = re.sub(class10_pattern, '\n\n        ', content, flags=re.DOTALL)
print("[OK] Class 10 section removed")

# Update Class 11 and Class 12 - rename to Class 11-12
old_class11_header = '''        <!-- Class 11 -->
        <TextView
            android:id="@+id/class11Header"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:background="#EEF2FF"
            android:padding="12dp"
            android:text="Class 11 ▸"
            android:textSize="18sp"
            android:textStyle="bold" />'''

new_class11_header = '''        <!-- Class 11-12 (Merged per NCTB) -->
        <TextView
            android:id="@+id/class11Header"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:background="#EEF2FF"
            android:padding="12dp"
            android:text="Class 11-12 ▸"
            android:textSize="18sp"
            android:textStyle="bold" />'''

if old_class11_header in content:
    content = content.replace(old_class11_header, new_class11_header)
    print("[OK] Class 11 header updated to Class 11-12")

# Update tag references in Class 9-10 topics from "Class 9" to "Class 9-10"
content = re.sub(
    r'android:tag="Class 9 - ',
    'android:tag="Class 9-10 - ',
    content
)
print("[OK] Updated Class 9 tag references to Class 9-10")

# Update tag references in Class 11-12 topics from "Class 11" to "Class 11-12"
content = re.sub(
    r'android:tag="Class 11 - ',
    'android:tag="Class 11-12 - ',
    content
)
print("[OK] Updated Class 11 tag references to Class 11-12")

# Remove any remaining Class 10 tag references (shouldn't exist but just in case)
# Update them to Class 9-10 if found
content = re.sub(
    r'android:tag="Class 10 - ',
    'android:tag="Class 9-10 - ',
    content
)
print("[OK] Updated any Class 10 tag references to Class 9-10")

# Remove any remaining Class 12 tag references
# Update them to Class 11-12 if found
content = re.sub(
    r'android:tag="Class 12 - ',
    'android:tag="Class 11-12 - ',
    content
)
print("[OK] Updated any Class 12 tag references to Class 11-12")

# Write the updated content
with open('app/src/main/res/layout/activity_main.xml', 'w', encoding='utf-8') as f:
    f.write(content)

print("\n[OK] activity_main.xml updated successfully!")
print("[OK] Classes consolidated to: Class 8, Class 9-10, Class 11-12")
