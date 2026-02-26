package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.definition.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.util.*;
import java.io.*;
import java.util.*;

public class StructLoader extends ConfigLoader<StructConfig> {

    public StructLoader(JagexCache storage) {
        super(ConfigType.STRUCT, storage);
    }


    @Override
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    protected StructConfig decode(final Archive.File file) throws IOException, UnhandledOpcodeException {
        StructConfig def = new StructConfig(file.getFileId());

        file.decode((stream, opcode) -> {
            switch (opcode) {
                case 249 -> {
                    int length = stream.readUnsignedByte();
                    Map<Integer, Object> params = new HashMap<>(length);

                    for (int i = 0; i < length; i++) {
                        boolean isString = stream.readUnsignedByte() == 1;
                        int key = stream.read24BitInt();
                        Object value;

                        if (isString) {
                            value = stream.readString();
                        } else {
                            value = stream.readInt();
                        }

                        params.put(key, value);
                    }

                    def.setParams(params);
                }
                default -> throw new UnhandledOpcodeException(opcode, ConfigType.STRUCT);
            }
        });

        return def;
    }
}
