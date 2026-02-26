package com.runemate.mcp.cache.definition;

import java.util.*;
import lombok.*;

@Data
public class StructConfig {

    public final int id;
    public Map<Integer, Object> params = null;
}
