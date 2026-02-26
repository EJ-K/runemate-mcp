package com.runemate.mcp.cache;

import java.util.*;
import lombok.*;

@Getter
@AllArgsConstructor
public enum CS2VarType {
    INTEGER(0, 'i', "integer"),
    BOOLEAN(1, '1', "boolean"),
    SEQ(6, 'A', "seq"),
    COLOUR(7, 'C', "colour"),
    LOC_SHAPE(8, 'H', "locshape"),
    COMPONENT(9, 'I', "component"),
    IDKIT(10, 'K', "idkit"),
    MIDI(11, 'M', "midi"),
    /**
     * An {@code OBJ} with an allocated internal name
     */
    NAMEDOBJ(13, 'O', "namedobj"),
    SYNTH(14, 'P', "synth"),
    STAT(17, 'S', "stat"),
    COORDGRID(22, 'c', "coordgrid"),
    GRAPHIC(23, 'd', "graphic"),
    FONTMETRICS(25, 'f', "fontmetrics"),
    ENUM(26, 'g', "enum"),
    JINGLE(28, 'j', "jingle"),
    /**
     * a.k.a {@code Object}.
     */
    LOC(30, 'l', "loc"),
    MODEL(31, 'm', "model"),
    NPC(32, 'n', "npc"),
    /**
     * a.k.a. {@code Item}.
     */
    OBJ(33, 'o', "obj"),
    STRING(36, 's', "string"),
    SPOTANIM(37, 't', "spotanim"),
    INV(39, 'v', "inv"),
    TEXTURE(40, 'x', "texture"),
    CATEGORY(41, 'y', "category"),
    CHAR(42, 'z', "char"),
    MAPSCENEICON(55, '£', "mapsceneicon"),
    MAPELEMENT(59, 'µ', "mapelement"),
    HITMARK(62, '×', "hitmark"),
    STRUCT(73, 'J', "struct"),
    DBROW(74, 'Ð', "dbrow"),
    DBTABLE(118, 'Ø', "dbtable"),
    VARP(209, '7', "varp"),
    ;

    private static final Map<Integer, CS2VarType> typeById = new HashMap<>();
    private static final Map<Character, CS2VarType> typeByKey = new HashMap<>();

    static {
        for (CS2VarType type : values()) {
            if (type.id != -1) {
                typeById.put(type.id, type);
            }
            typeByKey.put(type.keyChar, type);
        }
    }

    private final int id;
    private final char keyChar;
    private final String typeName;

    public static CS2VarType fromId(int id) {
        return typeById.get(id);
    }

    public static CS2VarType fromChar(char key) {
        return typeByKey.get(key);
    }

}
