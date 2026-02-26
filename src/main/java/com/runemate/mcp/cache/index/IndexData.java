package com.runemate.mcp.cache.index;

import com.runemate.mcp.cache.io.*;
import java.io.*;

public record IndexData(int protocol, int revision, boolean named, boolean sized, ArchiveData[] archives) {

    private static final int NAMED = 0x1;
    private static final int SIZED = 0x4;

    public static IndexData load(byte[] data) {
        try (Js5InputStream stream = new Js5InputStream(data)) {
            int protocol = stream.readUnsignedByte();
            if (protocol < 5 || protocol > 7) {
                throw new IllegalArgumentException("Unsupported protocol");
            }

            int revision = 6;
            if (protocol >= 6) {
                revision = stream.readInt();
            }

            int flags = stream.readUnsignedByte();
            boolean named = (flags & NAMED) != 0;
            boolean sized = (flags & SIZED) != 0;

            if ((flags & ~(NAMED | SIZED)) != 0) {
                throw new IllegalArgumentException("Unknown flags: " + flags);
            }

            int validArchivesCount = protocol >= 7 ? stream.readBigSmart() : stream.readUnsignedShort();
            int lastArchiveId = 0;

            ArchiveData[] archives = new ArchiveData[validArchivesCount];

            for (int index = 0; index < validArchivesCount; ++index) {
                int archive = lastArchiveId += protocol >= 7 ? stream.readBigSmart() : stream.readUnsignedShort();

                ArchiveData ad = new ArchiveData();
                ad.setId(archive);
                archives[index] = ad;
            }

            if (named) {
                for (int index = 0; index < validArchivesCount; ++index) {
                    int nameHash = stream.readInt();
                    ArchiveData ad = archives[index];
                    ad.setName(nameHash);
                }
            }

            for (int index = 0; index < validArchivesCount; ++index) {
                int crc = stream.readInt();

                ArchiveData ad = archives[index];
                ad.setCrc(crc);
            }

            if (sized) {
                for (int i = 0; i < validArchivesCount; i++) {
                    ArchiveData ad = archives[i];
                    ad.setCompressedSize(stream.readInt());
                    ad.setDecompressedSize(stream.readInt());
                }
            }

            for (int index = 0; index < validArchivesCount; ++index) {
                int archiveRevision = stream.readInt();
                ArchiveData ad = archives[index];
                ad.setRevision(archiveRevision);
            }

            int[] numberOfFiles = new int[validArchivesCount];
            for (int index = 0; index < validArchivesCount; ++index) {
                int num = protocol >= 7 ? stream.readBigSmart() : stream.readUnsignedShort();
                numberOfFiles[index] = num;
            }

            for (int index = 0; index < validArchivesCount; ++index) {
                ArchiveData ad = archives[index];
                int num = numberOfFiles[index];

                ad.setFiles(new FileData[num]);

                int last = 0;
                for (int i = 0; i < num; ++i) {
                    int fileId = last += protocol >= 7 ? stream.readBigSmart() : stream.readUnsignedShort();

                    FileData fd = ad.getFiles()[i] = new FileData();
                    fd.setId(fileId);
                }
            }

            if (named) {
                for (int index = 0; index < validArchivesCount; ++index) {
                    ArchiveData ad = archives[index];
                    int num = numberOfFiles[index];

                    for (int i = 0; i < num; ++i) {
                        FileData fd = ad.getFiles()[i];
                        fd.setName(stream.readInt());
                    }
                }
            }

            return new IndexData(protocol, revision, named, sized, archives);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
