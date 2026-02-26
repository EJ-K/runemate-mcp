package com.runemate.mcp.cache.index;

import lombok.*;

@Data
public class ArchiveData {

    private int id;
    private int name;
    private int crc;
    private int revision;
    private int compressedSize;
    private int decompressedSize;
    private FileData[] files;
}
