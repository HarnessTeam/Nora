import shutil
import os

# Clean up nora_old
nora_old = r"C:\Users\28767\WorkBuddy\local-agent\app\src\test\java\ai\nora_old"
if os.path.exists(nora_old):
    for root, dirs, files in os.walk(nora_old):
        rel_path = os.path.relpath(root, nora_old)
        dest_dir = os.path.join(r"C:\Users\28767\WorkBuddy\local-agent\app\src\test\java\ai\nora", rel_path)
        os.makedirs(dest_dir, exist_ok=True)
        for f in files:
            src_file = os.path.join(root, f)
            dst_file = os.path.join(dest_dir, f)
            if not os.path.exists(dst_file):
                shutil.copy2(src_file, dst_file)
                print(f"Copied: {dst_file}")
    shutil.rmtree(nora_old)
    print(f"Removed nora_old")

# Clean up old com directory
com_dir = r"C:\Users\28767\WorkBuddy\local-agent\app\src\test\java\com"
if os.path.exists(com_dir):
    shutil.rmtree(com_dir)
    print(f"Removed com dir")

# Verify
for root, dirs, files in os.walk(r"C:\Users\28767\WorkBuddy\local-agent\app\src\test\java\ai\nora"):
    for f in files:
        print(f"Found: {os.path.join(root, f)}")
