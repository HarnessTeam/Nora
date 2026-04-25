import glob

base = r"C:\Users\28767\WorkBuddy\local-agent\app\src"
pattern = base + r"\**\*.kt"
files = glob.glob(pattern, recursive=True)

count = 0
for f in files:
    try:
        content = open(f, "r", encoding="utf-8").read()
        if "com.example.localagent" in content:
            new_content = content.replace("com.example.localagent.", "ai.nora.")
            new_content = new_content.replace("com.example.localagent.LocalAgentApp", "ai.nora.LocalAgentApp")
            new_content = new_content.replace("com.example.localagent.MainActivity", "ai.nora.MainActivity")
            open(f, "w", encoding="utf-8").write(new_content)
            print(f"Updated: {f}")
            count += 1
    except Exception as e:
        print(f"Error: {f} -> {e}")

print(f"\nTotal: {count} files updated")
