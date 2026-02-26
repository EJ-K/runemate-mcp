package com.runemate.mcp.cache.fs;

import java.io.*;
import java.nio.*;

class CacheDataFile implements Closeable {

    private static final int SECTOR_SIZE = 520;

    private final RandomAccessFile raf;

    CacheDataFile(File file) throws IOException {
        this.raf = new RandomAccessFile(file, "r");
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

    synchronized byte[] read(int indexId, IndexEntry entry) throws IOException {
        return read(indexId, entry.id(), entry.sector(), entry.length());
    }

    synchronized byte[] read(int indexId, int archiveId, int sector, int size) throws IOException {
        byte[] readBuffer = new byte[SECTOR_SIZE];
        ByteBuffer buffer = ByteBuffer.allocate(size);
        for (int part = 0, readBytesCount = 0, nextSector; size > readBytesCount; sector = nextSector) {
            if (sector == 0) {
                throw new EOFException("No data for file " + indexId + "/" + archiveId);
            }

            raf.seek((long) SECTOR_SIZE * sector);

            int dataBlockSize = size - readBytesCount;
            byte headerSize;
            int currentIndex;
            int currentPart;
            int currentArchive;
            if (archiveId > 0xFFFF) {
                headerSize = 10;
                if (dataBlockSize > SECTOR_SIZE - headerSize) {
                    dataBlockSize = SECTOR_SIZE - headerSize;
                }

                int i = raf.read(readBuffer, 0, headerSize + dataBlockSize);
                if (i != headerSize + dataBlockSize) {
                    throw new EOFException("Short read for file data " + indexId + "/" + archiveId);
                }

                currentArchive = ((readBuffer[0] & 0xFF) << 24)
                    | ((readBuffer[1] & 0xFF) << 16)
                    | ((readBuffer[2] & 0xFF) << 8)
                    | (readBuffer[3] & 0xFF);
                currentPart = ((readBuffer[4] & 0xFF) << 8) + (readBuffer[5] & 0xFF);
                nextSector = ((readBuffer[6] & 0xFF) << 16)
                    | ((readBuffer[7] & 0xFF) << 8)
                    | (readBuffer[8] & 0xFF);
                currentIndex = readBuffer[9] & 0xFF;
            } else {
                headerSize = 8;
                if (dataBlockSize > SECTOR_SIZE - headerSize) {
                    dataBlockSize = SECTOR_SIZE - headerSize;
                }

                int i = raf.read(readBuffer, 0, headerSize + dataBlockSize);
                if (i != headerSize + dataBlockSize) {
                    throw new EOFException("Short read for file data " + indexId + "/" + archiveId);
                }

                currentArchive = ((readBuffer[0] & 0xFF) << 8) | (readBuffer[1] & 0xFF);
                currentPart = ((readBuffer[2] & 0xFF) << 8) | (readBuffer[3] & 0xFF);
                nextSector = ((readBuffer[4] & 0xFF) << 16) | ((readBuffer[5] & 0xFF) << 8) | (readBuffer[6] & 0xFF);
                currentIndex = readBuffer[7] & 0xFF;
            }

            if (archiveId != currentArchive || currentPart != part || indexId != currentIndex) {
                throw new StreamCorruptedException("Invalid data for file " + indexId + "/" + archiveId);
            }

            if (raf.length() / SECTOR_SIZE < (long) nextSector) {
                throw new IllegalStateException("Next sector is invalid: " + nextSector);
            }

            buffer.put(readBuffer, headerSize, dataBlockSize);
            readBytesCount += dataBlockSize;

            ++part;
        }

        buffer.flip();
        return buffer.array();
    }
}
