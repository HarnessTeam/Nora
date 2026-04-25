import os
import glob

base = r"C:\Users\28767\WorkBuddy\local-agent\app\src"
pattern = os.path.join(base, "**", "*.kt")
files = glob.glob(pattern, recursive=True)

count = 0
for f in files:
    try:
        content = open(f, "r", encoding="utf-8").read()
        if "package com.example.localagent" in content:
            new_content = content.replace("package com.example.localagent", "package ai.nora")
            open(f, "w", encoding="utf-8").write(new_content)
            print(f"Updated: {f}")
            count += 1
    except Exception as e:
        print(f"Error: {f} -> {e}")

print(f"\nTotal: {count} files updated")
