package com.runemate.mcp.cache.fs;

import java.util.*;
import lombok.*;

@Data
@EqualsAndHashCode(of = { "id", "revision", "archives" })
public class Index {

    private final int id;
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private final List<Archive> archives = new ArrayList<>();
    private int protocol = 6;
    private int revision;
    private boolean named, sized;
    private int crc;
    private int compression;

    public List<Archive> archives() {
        return Collections.unmodifiableList(archives);
    }

    public Archive addArchive(int id) {
        int idx = findArchiveIndex(id);
        if (idx >= 0) {
            throw new IllegalArgumentException("Archive " + id + " already exists");
        }

        idx = -idx - 1;
        Archive archive = new Archive(this, id);
        this.archives.add(idx, archive);
        return archive;
    }

    private int findArchiveIndex(int id) {
        int low = 0;
        int high = archives.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;

            Archive a = archives.get(mid);
            int cmp = Integer.compare(a.getArchiveId(), id);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }

        return -(low + 1);
    }

    public Archive getArchive(int id) {
        int idx = findArchiveIndex(id);
        if (idx < 0) {
            return null;
        }

        return archives.get(idx);
    }

}
