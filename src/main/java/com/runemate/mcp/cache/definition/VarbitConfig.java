package com.runemate.mcp.cache.definition;

import lombok.*;

@Data
public class VarbitConfig {

    private final int id;
    private int varPlayer;
    private int leastSignificantBit;
    private int mostSignificantBit;
}
