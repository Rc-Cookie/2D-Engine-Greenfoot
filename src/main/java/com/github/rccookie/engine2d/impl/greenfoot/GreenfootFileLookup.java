package com.github.rccookie.engine2d.impl.greenfoot;

import java.util.HashMap;
import java.util.Map;

import com.github.rccookie.engine2d.util.RuntimeIOException;
import com.github.rccookie.util.Arguments;
import com.github.rccookie.util.Console;

public final class GreenfootFileLookup {

    private GreenfootFileLookup() {
        throw new UnsupportedOperationException();
    }

    private static final Map<String,String> FILES = new HashMap<>();

    public static String get(String file) {
        String data = FILES.get(file.replace('\\', '/'));
        if(data == null) throw new RuntimeIOException("File not found: " + file);
        return data;
    }

    public static void set(String file, String data) {
        Console.mapDebug("Loaded file", file);
        FILES.put(file.replace('\\', '/'), Arguments.checkNull(data, "data"));
    }

    public static String[] list() {
        return FILES.values().toArray(new String[0]);
    }
}
