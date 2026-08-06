import os
import subprocess

# Let's revert the files since I am not supposed to code
subprocess.run(['git', 'checkout', 'app/src/main/java/com/example/ui/OmniRouteApp.kt'])
subprocess.run(['git', 'checkout', 'app/src/main/java/com/example/ui/settings/GlobalSettingsScreen.kt'])
subprocess.run(['git', 'checkout', 'BLUEPRINT.md'])
if os.path.exists('app/src/main/java/com/example/ui/settings/LogKeeperScreen.kt'):
    os.remove('app/src/main/java/com/example/ui/settings/LogKeeperScreen.kt')
