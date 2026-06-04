#!/usr/bin/env python3
import struct
import os
import sys

def align_elf(filepath):
    with open(filepath, 'rb') as f:
        data = bytearray(f.read())
    
    if len(data) < 64 or data[:4] != b'\x7fELF':
        return False

    elf_class = data[4] # 1 = 32-bit, 2 = 64-bit
    endian = '<' if data[5] == 1 else '>'
    
    if elf_class == 2:
        # 64-bit ELF
        e_phoff = struct.unpack_from(endian + 'Q', data, 32)[0]
        e_phentsize = struct.unpack_from(endian + 'H', data, 54)[0]
        e_phnum = struct.unpack_from(endian + 'H', data, 56)[0]
        e_shoff = struct.unpack_from(endian + 'Q', data, 40)[0]
        e_shentsize = struct.unpack_from(endian + 'H', data, 58)[0]
        e_shnum = struct.unpack_from(endian + 'H', data, 60)[0]
    elif elf_class == 1:
        # 32-bit ELF
        e_phoff = struct.unpack_from(endian + 'I', data, 28)[0]
        e_phentsize = struct.unpack_from(endian + 'H', data, 42)[0]
        e_phnum = struct.unpack_from(endian + 'H', data, 44)[0]
        e_shoff = struct.unpack_from(endian + 'I', data, 32)[0]
        e_shentsize = struct.unpack_from(endian + 'H', data, 46)[0]
        e_shnum = struct.unpack_from(endian + 'H', data, 48)[0]
    else:
        return False

    # Read all segment headers
    segments = []
    for i in range(e_phnum):
        ph_offset = e_phoff + i * e_phentsize
        if elf_class == 2:
            p_type, p_flags, p_offset, p_vaddr, p_paddr, p_filesz, p_memsz, p_align = struct.unpack_from(endian + 'IIQQQQQQ', data, ph_offset)
        else:
            p_type, p_offset, p_vaddr, p_paddr, p_filesz, p_memsz, p_flags, p_align = struct.unpack_from(endian + 'IIIIIIII', data, ph_offset)
        segments.append({
            'index': i,
            'ph_offset': ph_offset,
            'p_type': p_type,
            'p_flags': p_flags,
            'p_offset': p_offset,
            'p_vaddr': p_vaddr,
            'p_paddr': p_paddr,
            'p_filesz': p_filesz,
            'p_memsz': p_memsz,
            'p_align': p_align
        })

    # Read all section headers
    sections = []
    for i in range(e_shnum):
        sh_offset_pos = e_shoff + i * e_shentsize
        if elf_class == 2:
            sh_name, sh_type, sh_flags, sh_addr, sh_offset = struct.unpack_from(endian + 'IIQQQ', data, sh_offset_pos)
        else:
            sh_name, sh_type, sh_flags, sh_addr, sh_offset = struct.unpack_from(endian + 'IIIII', data, sh_offset_pos)
        sections.append({
            'index': i,
            'sh_offset_pos': sh_offset_pos,
            'sh_name': sh_name,
            'sh_type': sh_type,
            'sh_flags': sh_flags,
            'sh_addr': sh_addr,
            'sh_offset': sh_offset
        })

    # Sort PT_LOAD segments by original offset
    load_segments = sorted([s for s in segments if s['p_type'] == 1], key=lambda s: s['p_offset'])
    
    total_padding_inserted = 0
    
    for seg in load_segments:
        curr_offset = seg['p_offset']
        curr_vaddr = seg['p_vaddr']
        
        if curr_offset == 0:
            seg['p_align'] = max(seg['p_align'], 16384)
            continue
            
        target_mod = curr_vaddr % 16384
        curr_mod = curr_offset % 16384
        
        if curr_mod != target_mod:
            padding = (target_mod - curr_mod) % 16384
            
            # Insert padding bytes
            data[curr_offset:curr_offset] = b'\x00' * padding
            total_padding_inserted += padding
            
            # Adjust segment offsets
            for s in segments:
                if s['p_offset'] >= curr_offset:
                    s['p_offset'] += padding
            
            # Adjust section offsets
            for sec in sections:
                if sec['sh_offset'] >= curr_offset:
                    sec['sh_offset'] += padding
                    
            # Adjust e_shoff
            if e_shoff >= curr_offset:
                e_shoff += padding
                
            # Adjust e_phoff
            if e_phoff >= curr_offset:
                e_phoff += padding

        seg['p_align'] = max(seg['p_align'], 16384)

    if total_padding_inserted > 0 or any(s['p_align'] < 16384 for s in segments if s['p_type'] == 1):
        # Update ELF header
        if elf_class == 2:
            struct.pack_into(endian + 'Q', data, 40, e_shoff)
            struct.pack_into(endian + 'Q', data, 32, e_phoff)
        else:
            struct.pack_into(endian + 'I', data, 32, e_shoff)
            struct.pack_into(endian + 'I', data, 28, e_phoff)

        # Update program headers
        for s in segments:
            ph_offset = e_phoff + s['index'] * e_phentsize
            if elf_class == 2:
                struct.pack_into(endian + 'IIQQQQQQ', data, ph_offset,
                                 s['p_type'], s['p_flags'], s['p_offset'], s['p_vaddr'],
                                 s['p_paddr'], s['p_filesz'], s['p_memsz'], s['p_align'])
            else:
                struct.pack_into(endian + 'IIIIIIII', data, ph_offset,
                                 s['p_type'], s['p_offset'], s['p_vaddr'], s['p_paddr'],
                                 s['p_filesz'], s['p_memsz'], s['p_flags'], s['p_align'])

        # Update section headers
        for sec in sections:
            sh_offset_pos = e_shoff + sec['index'] * sec_sz if 'sec_sz' in locals() else e_shoff + sec['index'] * e_shentsize
            if elf_class == 2:
                struct.pack_into(endian + 'Q', data, sh_offset_pos + 24, sec['sh_offset'])
            else:
                struct.pack_into(endian + 'I', data, sh_offset_pos + 16, sec['sh_offset'])

        with open(filepath, 'wb') as f:
            f.write(data)
        print(f"  Aligned {filepath}: added {total_padding_inserted} bytes.")
        return True
    else:
        print(f"  Already aligned: {filepath}")
        return False

if __name__ == '__main__':
    lib_dirs = ["joevideolib/libs"]
    for lib_dir in lib_dirs:
        if not os.path.exists(lib_dir):
            continue
        print(f"Scanning {lib_dir} for shared libraries to align to 16KB page size...")
        for root, dirs, files in os.walk(lib_dir):
            for fname in files:
                if fname.endswith('.so'):
                    align_elf(os.path.join(root, fname))
    print("Alignment completed successfully.")
