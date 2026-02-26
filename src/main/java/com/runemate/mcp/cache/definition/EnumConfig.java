package com.runemate.mcp.cache.definition;

import com.runemate.mcp.cache.*;
import java.io.*;
import java.util.*;
import lombok.*;

@Data
public class EnumConfig {

    private final int id;
    private int[] intVals;
    private CS2VarType keyType;
    private CS2VarType valType;
    private String defaultString = "null";
    private int defaultInt;
    private int size;
    private int[] keys;
    private String[] stringVals;
    private Map<Integer, Serializable> map;

}
