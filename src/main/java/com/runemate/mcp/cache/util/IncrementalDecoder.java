package com.runemate.mcp.cache.util;

import com.runemate.mcp.cache.io.*;
import java.io.*;

@FunctionalInterface
public interface IncrementalDecoder {

    void accept(Js5InputStream js5InputStream, Integer integer) throws IOException, UnhandledOpcodeException;
}
