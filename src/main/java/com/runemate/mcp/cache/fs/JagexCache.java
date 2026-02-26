package com.runemate.mcp.cache.fs;

import com.runemate.mcp.cache.index.*;
import com.runemate.mcp.cache.io.*;
import java.io.*;
import java.util.*;
import lombok.extern.log4j.*;
import org.jetbrains.annotations.*;

@Log4j2
public class JagexCache implements Closeable {

    private static final String MAIN_FILE_CACHE_DAT = "main_file_cache.dat2";
    private static final String MAIN_FILE_CACHE_IDX = "main_file_cache.idx";

    private final File root;
    private final CacheDataFile data;
    private final CacheIndexFile index255;

    private final List<Index> indexes = new ArrayList<>();
    private final List<CacheIndexFile> indexFiles = new ArrayList<>();

    public JagexCache(final File root) throws IOException {
        this.root = root;
        this.data = new CacheDataFile(new File(root, MAIN_FILE_CACHE_DAT));
        this.index255 = new CacheIndexFile(255, new File(root, MAIN_FILE_CACHE_IDX + "255"));
    }


    @Override
    public void close() throws IOException {
        data.close();
        index255.close();
        for (CacheIndexFile idx : indexFiles) {
            idx.close();
        }
    }

    public Index findIndex(int id) {
        for (Index index : indexes) {
            if (index.getId() == id) {
                return index;
            }
        }

        try {
            Index index = new Index(id);
            load(index);
            indexes.add(index);
            return index;
        } catch (IOException e) {
            log.error("Error loading index {}", id, e);
        }

        return null;
    }

    @Nullable
    public byte[] load(Archive archive) throws IOException {
        return load(archive.getIndex().getId(), archive.getArchiveId());
    }

    @Nullable
    public byte[] load(int index, int archive) throws IOException {
        CacheIndexFile indexFile = getIndexFile(index);
        IndexEntry entry = indexFile.readEntry(archive);
        if (entry == null) {
            return null;
        }
        return data.read(index, entry);
    }

    private CacheIndexFile getIndexFile(int i) throws IOException {
        if (i == 255) {
            return index255;
        }

        for (CacheIndexFile indexFile : indexFiles) {
            if (indexFile.getIndexFileId() == i) {
                return indexFile;
            }
        }

        CacheIndexFile indexFile = new CacheIndexFile(i, new File(root, MAIN_FILE_CACHE_IDX + i));
        indexFiles.add(indexFile);
        return indexFile;
    }

    private void load(Index index) throws IOException {
        byte[] indexData = readIndex(index.getId());
        if (indexData == null) {
            return;
        }

        DataBlock res = DataBlock.decompress(indexData, null);
        byte[] data = res.data();

        IndexData id = IndexData.load(data);
        for (ArchiveData ad : id.archives()) {
            Archive archive = index.addArchive(ad.getId());
            archive.setName(ad.getName());
            archive.setCrc(ad.getCrc());
            archive.setCompressedSize(ad.getCompressedSize());
            archive.setDecompressedSize(ad.getDecompressedSize());
            archive.setRevision(ad.getRevision());
            archive.setFileData(ad.getFiles());

            assert ad.getFiles().length > 0;
        }

        index.setCrc(res.crc());
        index.setCompression(res.compression());
        assert res.revision() == -1;
//        indexes.add(index);
    }

    private byte[] readIndex(int indexId) throws IOException {
        IndexEntry entry = index255.readEntry(indexId);
        if (entry == null) {
            return null;
        }

        return data.read(index255.getIndexFileId(), entry);
    }


}
