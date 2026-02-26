package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.definition.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.util.*;
import java.io.*;
import java.util.*;

public class EnumLoader extends ConfigLoader<EnumConfig> {

    public EnumLoader(JagexCache storage) {
        super(ConfigType.ENUM, storage);
    }


    @Override
    protected EnumConfig decode(final Archive.File file) throws IOException, UnhandledOpcodeException {
        EnumConfig def = new EnumConfig(file.getFileId());
        file.decode((stream, opcode) -> {
            switch (opcode) {
                case 1 -> def.setKeyType(CS2VarType.fromChar((char) stream.readUnsignedByte()));
                case 2 -> def.setValType(CS2VarType.fromChar((char) stream.readUnsignedByte()));
                case 3 -> def.setDefaultString(stream.readString());
                case 4 -> def.setDefaultInt(stream.readInt());
                case 5 -> {
                    int size = stream.readUnsignedShort();
                    Map<Integer, Serializable> map = new HashMap<>(size);
                    int[] keys = new int[size];
                    String[] stringVals = new String[size];
                    for (int index = 0; index < size; ++index) {
                        int key = stream.readInt();
                        String value = stream.readString();
                        keys[index] = key;
                        stringVals[index] = value;
                        map.put(key, value);
                    }
                    def.setMap(map);
                    def.setSize(size);
                    def.setKeys(keys);
                    def.setStringVals(stringVals);
                }
                case 6 -> {
                    int size = stream.readUnsignedShort();
                    Map<Integer, Serializable> map = new HashMap<>(size);
                    int[] keys = new int[size];
                    int[] intVals = new int[size];
                    for (int index = 0; index < size; ++index) {
                        int key = stream.readInt();
                        int value = stream.readInt();
                        keys[index] = key;
                        intVals[index] = value;
                        map.put(key, value);
                    }
                    def.setMap(map);
                    def.setSize(size);
                    def.setKeys(keys);
                    def.setIntVals(intVals);
                }
            }
        });
        return def;
    }
}
