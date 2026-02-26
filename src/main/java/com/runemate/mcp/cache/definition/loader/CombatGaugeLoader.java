
package com.runemate.mcp.cache.definition.loader;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.definition.*;
import com.runemate.mcp.cache.fs.*;
import com.runemate.mcp.cache.util.*;
import java.io.*;

public class CombatGaugeLoader extends ConfigLoader<CombatGaugeConfig> {

    public CombatGaugeLoader(JagexCache storage) {
        super(ConfigType.HEALTHBAR, storage);
    }


    @Override
    protected CombatGaugeConfig decode(final Archive.File file) throws IOException, UnhandledOpcodeException {
        CombatGaugeConfig def = new CombatGaugeConfig();
        def.setId(file.getFileId());

        file.decode((stream, opcode) -> {
            switch (opcode) {
                case 1 -> {
                    /*field3276 = */stream.readUnsignedShort();
                }
                case 2 -> {
                    /*field3277 = */stream.readUnsignedByte();
                }
                case 3 -> {
                    /*field3278 = */stream.readUnsignedByte();
                }
                case 4 -> {
                    //field3283 = 0
                }
                case 5 -> {
                    /*field3275 = */stream.readUnsignedShort();
                }
                case 6 -> {
                    // Read but don't store
                    stream.readUnsignedByte();
                }
                case 7 -> def.setHealthBarFrontSpriteId(stream.readBigSmart2());
                case 8 -> def.setHealthBarBackSpriteId(stream.readBigSmart2());
                case 11 -> {
                    /*field3283 = */stream.readUnsignedShort();
                }
                case 14 -> def.setHealthScale(stream.readUnsignedByte());
                case 15 -> def.setHealthBarPadding(stream.readUnsignedByte());
                default -> throw new UnhandledOpcodeException(opcode, ConfigType.HEALTHBAR);
            }
        });

        return def;
    }
}
