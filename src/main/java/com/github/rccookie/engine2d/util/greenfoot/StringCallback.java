package com.github.rccookie.engine2d.util.greenfoot;

import java.util.function.Consumer;

import org.teavm.jso.JSObject;

public interface StringCallback extends JSObject, Consumer<String> {

    @Override
    void accept(String s);
}
