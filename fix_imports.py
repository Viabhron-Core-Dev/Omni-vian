with open('app/src/main/java/com/example/ui/chat/GeminiClient.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
imports_seen = set()

for line in lines:
    if line.startswith('import '):
        if line in imports_seen:
            continue
        imports_seen.add(line)
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/chat/GeminiClient.kt', 'w') as f:
    f.writelines(new_lines)

