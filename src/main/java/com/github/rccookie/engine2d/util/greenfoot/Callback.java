package com.github.rccookie.engine2d.util.greenfoot;

import org.teavm.jso.JSObject;

public interface Callback extends JSObject, Runnable {

    @Override
    void run();
}
