import shutil
import os

src_base = r"C:\Users\28767\WorkBuddy\local-agent\app\src\test\java"
old_pkg = os.path.join(src_base, "com", "example", "localagent")
new_pkg = os.path.join(src_base, "ai", "nora")

# Create new directory structure
os.makedirs(new_pkg, exist_ok=True)

# Walk old package dir and copy all files
for root, dirs, files in os.walk(old_pkg):
    rel_path = os.path.relpath(root, old_pkg)
    dest_dir = os.path.join(new_pkg, rel_path)
    os.makedirs(dest_dir, exist_ok=True)
    for f in files:
        src_file = os.path.join(root, f)
        dst_file = os.path.join(dest_dir, f)
        shutil.copy2(src_file, dst_file)
        print(f"Copied: {dst_file}")

# Remove old directory
shutil.rmtree(old_pkg)
print(f"\nOld directory removed: {old_pkg}")
print(f"New directory created: {new_pkg}")
