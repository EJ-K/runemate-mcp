package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.definition.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.io.*;
import com.runemate.mcp.cache.util.*;
import java.io.*;

public class DBTableLoader extends ConfigLoader<DBTableConfig> {

    public DBTableLoader(JagexCache storage) {
        super(ConfigType.DBTABLE, storage);
    }


    public static Object[] decodeColumnFields(Js5InputStream stream, CS2VarType[] types) {
        int fieldCount = stream.readUnsignedShortSmart();
        Object[] values = new Object[fieldCount * types.length];

        for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++) {
            for (int typeIndex = 0; typeIndex < types.length; typeIndex++) {
                CS2VarType type = types[typeIndex];
                int valuesIndex = fieldIndex * types.length + typeIndex;

                if (type == CS2VarType.STRING) {
                    values[valuesIndex] = stream.readString();
                } else {
                    values[valuesIndex] = stream.readInt();
                }
            }
        }

        return values;
    }

    @Override
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    protected DBTableConfig decode(final Archive.File file) throws IOException, UnhandledOpcodeException {
        DBTableConfig def = new DBTableConfig(file.getFileId());
        file.decode((stream, opcode) -> {
            switch (opcode) {
                case 1 -> {
                    int numColumns = stream.readUnsignedByte();
                    CS2VarType[][] types = new CS2VarType[numColumns][];
                    Object[][] defaultValues = null;

                    for (int setting = stream.readUnsignedByte(); setting != 255; setting = stream.readUnsignedByte()) {
                        int columnId = setting & 0x7F;
                        boolean hasDefault = (setting & 0x80) != 0;
                        CS2VarType[] columnTypes = new CS2VarType[stream.readUnsignedByte()];
                        for (int i = 0; i < columnTypes.length; i++) {
                            columnTypes[i] = CS2VarType.fromId(stream.readUnsignedShortSmart());
                        }
                        types[columnId] = columnTypes;

                        if (hasDefault) {
                            if (defaultValues == null) {
                                defaultValues = new Object[types.length][];
                            }

                            defaultValues[columnId] = decodeColumnFields(stream, columnTypes);
                        }
                    }

                    def.setTypes(types);
                    def.setDefaultColumnValues(defaultValues);
                }
                default -> throw new UnhandledOpcodeException(opcode, ConfigType.DBTABLE);
            }
        });
        return def;
    }
}
