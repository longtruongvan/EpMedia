#!/usr/bin/env python3
import subprocess

def get_logs():
    cmd = ["/Users/longtv/Library/Android/sdk/platform-tools/adb", "logcat", "-d", "-v", "time"]
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    stdout, stderr = proc.communicate()
    lines = stdout.decode('utf-8', errors='ignore').split('\n')
    
    with open("filtered_logs.txt", "w") as f:
        for line in lines:
            if " 15:19:" in line or " 15:20:" in line or " 15:21:" in line:
                f.write(line + "\n")
    print("Wrote logs to filtered_logs.txt")

if __name__ == '__main__':
    get_logs()
