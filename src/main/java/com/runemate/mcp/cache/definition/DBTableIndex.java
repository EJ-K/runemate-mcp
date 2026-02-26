package com.runemate.mcp.cache.definition;

import  com.runemate.mcp.cache.*;
import com.runemate.mcp.cache.*;
import java.util.*;
import lombok.*;

@Data
public class DBTableIndex {

    private final int tableId;
    private final int columnId;
    private BaseType[] tupleTypes;
    private List<Map<Object, List<Integer>>> tupleIndexes;
}
