package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.definition.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.util.*;
import com.google.common.cache.*;
import java.io.*;
import java.time.*;

public class VarbitLoader extends ConfigLoader<VarbitConfig> {

    private static final Cache<Integer, VarbitConfig> CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(5))
        .build();

    public VarbitLoader(JagexCache storage) {
        super(ConfigType.VARBIT, storage, CACHE);
    }


    @Override
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    protected VarbitConfig decode(final Archive.File file) throws IOException, UnhandledOpcodeException {
        VarbitConfig def = new VarbitConfig(file.getFileId());
        file.decode((stream, opcode) -> {
            switch (opcode) {
                case 1 -> {
                    def.setVarPlayer(stream.readUnsignedShort());
                    def.setLeastSignificantBit(stream.readUnsignedByte());
                    def.setMostSignificantBit(stream.readUnsignedByte());
                }
                default -> throw new UnhandledOpcodeException(opcode, ConfigType.VARBIT);
            }
        });
        return def;
    }
}
