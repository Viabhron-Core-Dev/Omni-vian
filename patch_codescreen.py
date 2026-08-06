import re

with open('app/src/main/java/com/example/ui/code/CodeScreen.kt', 'r') as f:
    content = f.read()

# Remove 'scope.launch { drawerState.close() }' from onFileClick inside ModalDrawerSheet
new_content = content.replace('''                            onFileClick = { fileNode ->
                                selectedFile = fileNode
                                scope.launch { drawerState.close() }
                            }''', '''                            onFileClick = { fileNode ->
                                selectedFile = fileNode
                            }''')

with open('app/src/main/java/com/example/ui/code/CodeScreen.kt', 'w') as f:
    f.write(new_content)

