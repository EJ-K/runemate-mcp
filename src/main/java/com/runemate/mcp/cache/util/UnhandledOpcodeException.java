package com.runemate.mcp.cache.util;

import  com.runemate.mcp.cache.*;
import lombok.*;

@Getter
public class UnhandledOpcodeException extends Exception {

    private final int opcode;
    private final ConfigType config;

    public UnhandledOpcodeException(int opcode, ConfigType config) {
        super("Unhandled opcode in " + config.name() + ": " + opcode);
        this.opcode = opcode;
        this.config = config;
    }
}
