package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.definition.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.util.*;
import java.io.*;

public class DBRowLoader extends ConfigLoader<DBRowConfig> {

    public DBRowLoader(JagexCache storage) {
        super(ConfigType.DBROW, storage);
    }


    @Override
    protected DBRowConfig decode(final Archive.File file) throws IOException, UnhandledOpcodeException {
        DBRowConfig def = new DBRowConfig(file.getFileId());
        file.decode((stream, opcode) -> {
            switch (opcode) {
                case 3 -> {
                    int numColumns = stream.readUnsignedByte();
                    CS2VarType[][] types = new CS2VarType[numColumns][];
                    Object[][] columnValues = new Object[numColumns][];

                    for (int columnId = stream.readUnsignedByte(); columnId != 255; columnId = stream.readUnsignedByte()) {
                        CS2VarType[] columnTypes = new CS2VarType[stream.readUnsignedByte()];
                        for (int i = 0; i < columnTypes.length; i++) {
                            columnTypes[i] = CS2VarType.fromId(stream.readUnsignedShortSmart());
                        }
                        types[columnId] = columnTypes;
                        columnValues[columnId] = DBTableLoader.decodeColumnFields(stream, columnTypes);
                    }

                    def.setColumnTypes(types);
                    def.setColumnValues(columnValues);
                }
                case 4 -> def.setTableId(stream.readVarInt2());
                default -> throw new UnhandledOpcodeException(opcode, ConfigType.DBROW);
            }
        });
        return def;
    }
}
