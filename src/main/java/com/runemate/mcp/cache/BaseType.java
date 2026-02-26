package com.runemate.mcp.cache;

import lombok.*;

@AllArgsConstructor
public enum BaseType {
    INT(0, Integer.class),
    LONG(1, Long.class),
    STRING(2, String.class);

    @Getter
    private final int id;
    private final Class<?> clazz;

    public static BaseType by(int id) {
        for (BaseType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    public Class<?> getType() {
        return clazz;
    }
}
