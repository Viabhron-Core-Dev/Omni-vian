with open('app/src/main/java/com/example/ui/settings/omniroute/AiManagerPanelScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
imports = []
for line in lines:
    if line.startswith('import '):
        imports.append(line)
    elif line.startswith('package '):
        new_lines.append(line)
        new_lines.extend(imports)
        imports = []
    else:
        new_lines.append(line)

with open('app/src/main/java/com/example/ui/settings/omniroute/AiManagerPanelScreen.kt', 'w') as f:
    f.writelines(new_lines)
