#!/usr/bin/env python3
import struct
import os
import sys

PT_LOAD = 1

def patch_file(path):
    with open(path, 'rb') as f:
        data = bytearray(f.read())
    if len(data) < 64 or data[:4] != b'\x7fELF':
        return
    
    cls = data[4]       # 1 = 32-bit, 2 = 64-bit
    endian = '<' if data[5] == 1 else '>'
    patched = False
    
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
                if struct.unpack_from(endian + 'Q', data, o)[0] < 0x4000:
                    struct.pack_into(endian + 'Q', data, o, 0x4000)
                    patched = True
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
                if struct.unpack_from(endian + 'I', data, o)[0] < 0x4000:
                    struct.pack_into(endian + 'I', data, o, 0x4000)
                    patched = True
                    
    if patched:
        with open(path, 'wb') as f:
            f.write(data)
        print(f'  Patched: {path}')

if __name__ == '__main__':
    lib_dirs = ["joevideolib/libs"]
    for lib_dir in lib_dirs:
        print(f'Scanning {lib_dir} for shared libraries...')
        for root, dirs, files in os.walk(lib_dir):
            for fname in files:
                if fname.endswith('.so'):
                    patch_file(os.path.join(root, fname))
    print('Done patching all shared libraries.')
