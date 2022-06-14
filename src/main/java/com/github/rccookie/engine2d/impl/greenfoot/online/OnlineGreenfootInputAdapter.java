package com.github.rccookie.engine2d.impl.greenfoot.online;

import com.github.rccookie.engine2d.impl.InputAdapter;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootInputAdapter;
import com.github.rccookie.engine2d.util.greenfoot.JS;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

@SuppressWarnings("JSUnresolvedFunction")
public class OnlineGreenfootInputAdapter extends GreenfootInputAdapter {

    static final InputAdapter INSTANCE = new OnlineGreenfootInputAdapter();

    private OnlineGreenfootInputAdapter() {
        super(true);
        JS.eval("document.getElementById('scenarioCanvas').addEventListener('keydown', e=>e.preventDefault())");
        attachOnPress0(this::keyPressed);
        attachOnRelease0(this::keyReleased);
    }

    @JSBody(params = "listener", script = "document.getElementById('scenarioCanvas').onkeydown = e => listener.onKey(e.key)")
    private static native void attachOnPress0(InputListener listener);

    @JSBody(params = "listener", script = "document.getElementById('scenarioCanvas').onkeyup = e => listener.onKey(e.key)")
    private static native void attachOnRelease0(InputListener listener);

    private interface InputListener extends JSObject {
        @SuppressWarnings("unused")
        void onKey(String key);
    }
}
