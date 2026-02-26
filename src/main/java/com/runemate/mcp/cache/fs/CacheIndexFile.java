package com.runemate.mcp.cache.fs;

import java.io.*;
import lombok.*;
import lombok.extern.log4j.*;
import org.jetbrains.annotations.*;

@Log4j2
class CacheIndexFile implements Closeable {

    private static final int INDEX_ENTRY_LEN = 6;

    private final RandomAccessFile raf;
    private final File file;
    @Getter
    private final int indexFileId;

    private final byte[] buffer = new byte[INDEX_ENTRY_LEN];

    CacheIndexFile(int indexFileId, File file) throws IOException {
        this.indexFileId = indexFileId;
        this.file = file;
        this.raf = new RandomAccessFile(file, "r");
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

    @Nullable
    synchronized IndexEntry readEntry(int id) throws IOException {
        raf.seek((long) id * INDEX_ENTRY_LEN);
        int read = raf.read(buffer);
        if (read != INDEX_ENTRY_LEN) {
            throw new StreamCorruptedException("Failed to read index entry for id " + id);
        }
        int length = ((buffer[0] & 0xFF) << 16) | ((buffer[1] & 0xFF) << 8) | (buffer[2] & 0xFF);
        int sector = ((buffer[3] & 0xFF) << 16) | ((buffer[4] & 0xFF) << 8) | (buffer[5] & 0xFF);
        if (length == 0 || sector == 0) {
//            log.warn("Invalid index entry for id {}: length={}, sector={}", id, length, sector);
            return null;
        }
        return new IndexEntry(this, id, sector, length);
    }

    int indexCount() {
        return (int) (file.length() / INDEX_ENTRY_LEN);
    }
}
