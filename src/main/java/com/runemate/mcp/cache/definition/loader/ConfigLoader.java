package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.util.*;
import com.google.common.cache.*;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import lombok.extern.log4j.*;
import org.jetbrains.annotations.*;

@Log4j2
public abstract class ConfigLoader<T> {

    private static final int INDEX = 2;

    private final ConfigType type;
    private final JagexCache storage;
    private final Cache<Integer, T> lookupCache;

    public ConfigLoader(ConfigType type, JagexCache storage, Cache<Integer, T> lookupCache) {
        this.type = type;
        this.storage = storage;
        this.lookupCache = lookupCache;
    }

    public ConfigLoader(ConfigType type, JagexCache storage) {
        this(type, storage, CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(1)).build());
    }


    /**
     * Attempts to load a config from the cache at the specified id. Returns null if either the config does not exist, or
     * decoding of the config fails.
     */
    @Nullable
    public T load(int id) {
        T cached = lookupCache.getIfPresent(id);
        if (cached != null) {
            return cached;
        }

        try {
            Index index = storage.findIndex(INDEX);
            if (index == null) {
                return null;
            }

            Archive archive = index.getArchive(type.getId());
            if (archive == null) {
                return null;
            }

            Archive.Files files = archive.files(storage);
            Archive.File file = files.get(id);
            return lookup(file);
        } catch (IOException e) {
            log.error("Error when loading {} {}", type, id, e);
            return null;
        }
    }

    /**
     * Attempts to load all configs from the cache. Any configs that fail to be properly decoded will be omitted from the results.
     */
    public List<T> loadAll() {
        try {
            Index index = storage.findIndex(INDEX);
            if (index == null) {
                return null;
            }

            Archive archive = index.getArchive(type.getId());
            if (archive == null) {
                return null;
            }

            Archive.Files files = archive.files(storage);
            List<T> result = new ArrayList<>(files.size());
            for (Archive.File file : files.files()) {
                T cached = lookupCache.getIfPresent(file.getFileId());
                if (cached != null) {
                    result.add(cached);
                    continue;
                }

                try {
                    T decoded = lookup(file);
                    result.add(decoded);
                } catch (IOException e) {
                    log.error("Error when loading {} {}", type, file.getFileId(), e);
                }
            }

            return result;
        } catch (IOException e) {
            return null;
        }
    }

    public List<T> loadAll(Predicate<T> filter) {
        return loadAll().stream().filter(filter).toList();
    }

    /**
     * Constructs an instance of the config, decoding the specified file.
     *
     * @throws IOException              when a read operation fails
     * @throws UnhandledOpcodeException when an unhandled opcode is encountered
     */
    protected abstract T decode(Archive.File file) throws IOException, UnhandledOpcodeException;

    @Nullable
    private T lookup(Archive.File file) throws IOException {
        if (file == null) {
            return null;
        }

        try {
            T decoded = decode(file);
            lookupCache.put(file.getFileId(), decoded);
            return decoded;
        } catch (UnhandledOpcodeException e) {
            throw new IOException(e);
        }
    }
}
