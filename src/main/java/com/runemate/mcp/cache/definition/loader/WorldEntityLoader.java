package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.definition.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.util.*;
import com.google.common.cache.*;
import java.io.*;
import java.time.*;

public class WorldEntityLoader extends ConfigLoader<WorldEntityConfig> {

    private static final Cache<Integer, WorldEntityConfig> CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(1))
        .build();

    public WorldEntityLoader(JagexCache storage) {
        super(ConfigType.WORLD_ENTITY, storage, CACHE);
    }


    @Override
    protected WorldEntityConfig decode(final Archive.File file) throws IOException, UnhandledOpcodeException {
        WorldEntityConfig def = new WorldEntityConfig(file.getFileId());
        file.decode((stream, opcode) -> {
            switch (opcode) {
                case 2 -> {
                    /*def.field2370 = */
                    stream.readUnsignedByte();
                }
                case 3, 10, 11, 13, 21, 22 -> {
                }
                case 4 -> {
                    /*def.field2381 = */
                    stream.readShort();
                }
                case 5 -> {
                    /*def.field2382 = */
                    stream.readShort();
                }
                case 6 -> {
                    /*def.field2384 = */
                    stream.readShort();
                }
                case 7 -> {
                    /*def.field2378 = */
                    stream.readShort();
                }
                case 8 -> {
                    /*def.field2387 = */
                    stream.readUnsignedShort();
                }
                case 9 -> {
                    /*def.field2388 = */
                    stream.readUnsignedShort();
                }
                case 12 -> {
                    def.setName(stream.readString());
                }
                case 14 -> {
//                    def.field2373 = true;
                }
                case 15, 16, 17, 18, 19 -> {
                    int var4 = opcode - 15;
                    def.getActions()[var4] = stream.readString();
                    if (def.getActions()[var4].equalsIgnoreCase("Hidden")) {
                        def.getActions()[var4] = null;
                    }

//                    def.field2373 = true;
                }
                case 20 -> {
                    def.setCategory(stream.readUnsignedShort());
                }
                case 23 -> {
                    /*def.field2394 = */
                    stream.readUnsignedByte(); // (Class404)Class434.method9933(Class404.method4620(), stream.readUnsignedByte());
                }
                case 24 -> {
                    /*def.field2393 = */
                    stream.readUnsignedByte();// (Class387)Class434.method9933(Class387.method5374(), stream.readUnsignedByte());
                }
                case 25 -> {
                    /*def.field2389 = */
                    stream.readUnsignedShort();
                }
                case 26 -> {
                    /*def.field2396 = */
                    stream.readDefaultableUnsignedSmart();
                }
                case 27 -> {
                    /*def.field2377 = */
                    stream.readUnsignedShort();
                }
                default -> throw new UnhandledOpcodeException(opcode, ConfigType.WORLD_ENTITY);
            }
        });

        return def;
    }
}
