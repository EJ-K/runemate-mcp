package com.runemate.mcp.cache.definition;

import com.runemate.mcp.cache.*;
import lombok.*;

@Data
public class DBTableConfig {

    private final int id;
    private CS2VarType[][] types;
    private Object[] defaultColumnValues;
}
