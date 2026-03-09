import sys
import os

filepath = r"c:\Users\DELL\OneDrive\Desktop\resolveiq_backend\app\utils\dept_isolation.py"

with open(filepath, 'r') as f:
    lines = f.readlines()
    for i, line in enumerate(lines):
        if "Software Installation" in line or "Application Downtime" in line:
            print(f"Line {i+1}: {repr(line)}")
