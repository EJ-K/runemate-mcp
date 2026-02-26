package com.runemate.mcp.cache.definition;

import com.runemate.mcp.cache.*;
import lombok.*;

@Data
public class DBRowConfig {

    private final int id;
    private int tableId;
    private CS2VarType[][] columnTypes;
    private Object[][] columnValues;
}
