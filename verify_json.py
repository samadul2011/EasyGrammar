import json

try:
    with open(r'c:\Users\samad\OneDrive\Desktop\Android\EasyGrammar\app\src\main\res\raw\topics.json', 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    print("✓ JSON is VALID!")
    print(f"\nClass 8 has {len(data['Class 8'])} topics:")
    for topic_name, topic_data in data['Class 8'].items():
        lessons = len(topic_data.get('lessons', []))
        tests = len(topic_data.get('tests', []))
        print(f"  {topic_name}: {lessons} lessons, {tests} tests")
        
except json.JSONDecodeError as e:
    print(f"✗ JSON is INVALID: {e}")
except Exception as e:
    print(f"✗ Error: {e}")
