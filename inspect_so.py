#!/usr/bin/env python3
import struct
import os

PT_LOAD = 1

def inspect_file(path):
    with open(path, 'rb') as f:
        data = bytearray(f.read())
    if len(data) < 64 or data[:4] != b'\x7fELF':
        return
    
    cls = data[4]       # 1 = 32-bit, 2 = 64-bit
    endian = '<' if data[5] == 1 else '>'
    
    print(f"\nFile: {path} ({'64-bit' if cls == 2 else '32-bit'})")
    
    if cls == 2:
        phoff = struct.unpack_from(endian + 'Q', data, 32)[0]
        phesz = struct.unpack_from(endian + 'H', data, 54)[0]
        phn   = struct.unpack_from(endian + 'H', data, 56)[0]
        for i in range(phn):
            ph = phoff + i * phesz
            if ph + phesz > len(data):
                break
            if struct.unpack_from(endian + 'I', data, ph)[0] == PT_LOAD:
                o = ph + 48
                align = struct.unpack_from(endian + 'Q', data, o)[0]
                if align < 0x4000:
                    print(f"File: {path} (64-bit)")
                    print(f"  PT_LOAD alignment: {align} (0x{align:x})")
    elif cls == 1:
        phoff = struct.unpack_from(endian + 'I', data, 28)[0]
        phesz = struct.unpack_from(endian + 'H', data, 42)[0]
        phn   = struct.unpack_from(endian + 'H', data, 44)[0]
        for i in range(phn):
            ph = phoff + i * phesz
            if ph + phesz > len(data):
                break
            if struct.unpack_from(endian + 'I', data, ph)[0] == PT_LOAD:
                o = ph + 28
                align = struct.unpack_from(endian + 'I', data, o)[0]
                if align < 0x4000:
                    print(f"File: {path} (32-bit)")
                    print(f"  PT_LOAD alignment: {align} (0x{align:x})")

if __name__ == '__main__':
    lib_dir = "."
    for root, dirs, files in os.walk(lib_dir):
        if ".git" in root or ".gradle" in root:
            continue
        for fname in files:
            if fname.endswith('.so'):
                inspect_file(os.path.join(root, fname))
