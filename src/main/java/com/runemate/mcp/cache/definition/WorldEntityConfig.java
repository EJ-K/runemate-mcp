package com.runemate.mcp.cache.definition;

import lombok.*;

@Data
@ToString(of = {"id", "category", "name"})
@EqualsAndHashCode(callSuper = false)
public class WorldEntityConfig {

    private final int id;
    private String name;
    private int category;

    private final String[] actions = new String[5];

    private int field2370;
    private short field2381;
    private short field2382;
    private short field2384;
    private short field2378;
    private int field2387;
    private int field2388;
    private boolean field2373;
    private int field2394;
    private int field2393;
    private int field2389;
    private int field2396;
    private int field2377;
}
