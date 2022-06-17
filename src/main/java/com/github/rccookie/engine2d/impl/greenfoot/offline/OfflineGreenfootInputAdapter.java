package com.github.rccookie.engine2d.impl.greenfoot.offline;

import com.github.rccookie.engine2d.coroutine.Execute;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootDisplay;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootInputAdapter;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootSession;
import com.github.rccookie.geometry.performance.int2;

import greenfoot.Greenfoot;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * greenfoot.Greenfoot based input adapter implementation.
 */
class OfflineGreenfootInputAdapter extends GreenfootInputAdapter {

    /**
     * A list of all keys registered.
     */
    private static final String[] KEYS = {
            "escape",
            "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10", "f11", "f12",
            // "^", "&acute;", // Not supported because greenfoot.Greenfoot currently does not report key release for these keys: (^,`)
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "\u00DF", "\u00E4", "\u00F6", "\u00FC", // ß, ä, ö, ü
            "backspace", "insert", "delete", "home", "end", "page up", "page down",
            "+", "-", "*", "/","<",
            "tab", "shift", "control", "alt", "space", "enter",
            "left", "right", "up", "down",
            ".", ",", "#",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
            "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
    };

    private final int2 windowSize = new int2(600, 400);
    private final GreenfootDisplay display;

    OfflineGreenfootInputAdapter(GreenfootDisplay display) {
        super(false);
        this.display = display;
        if(GreenfootSession.REAL == GreenfootSession.STANDALONE)
            applyWindowListener();
    }

    private void applyWindowListener() {
        Stage.getWindows().stream().filter(Window::isShowing).findAny().ifPresentOrElse(w -> {
            w.widthProperty().addListener(($0, $1, x) -> windowSize.x = x.intValue());
            w.heightProperty().addListener(($0, $1, y) -> windowSize.y = y.intValue());
        }, () -> Execute.later(this::applyWindowListener));
    }

    @Override
    protected void update() {
        for(String key : KEYS) {
            if(Greenfoot.isKeyDown(key)) keyPressed(key);
            else keyReleased(key);
        }
        super.update();
    }

    @Override
    protected int2 applyMouseOffset(int2 pos) {
        if(GreenfootSession.REAL == GreenfootSession.STANDALONE) {
            pos.x += (display.getWorld().getWidth() - windowSize.x) / 2 + 8;
            pos.y += (display.getWorld().getHeight() - windowSize.y) / 2 + 50;
        }
        return pos;
    }
}
