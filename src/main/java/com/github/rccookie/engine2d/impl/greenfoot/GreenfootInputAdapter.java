package com.github.rccookie.engine2d.impl.greenfoot;

import java.util.HashSet;
import java.util.Set;

import com.github.rccookie.engine2d.impl.InputAdapter;
import com.github.rccookie.engine2d.impl.MouseData;
import com.github.rccookie.engine2d.util.annotations.Continuous;
import com.github.rccookie.engine2d.util.annotations.OverrideTarget;
import com.github.rccookie.event.BiParamEvent;
import com.github.rccookie.event.ParamEvent;
import com.github.rccookie.event.SimpleBiParamEvent;
import com.github.rccookie.event.SimpleParamEvent;
import com.github.rccookie.event.action.BiParamAction;
import com.github.rccookie.event.action.ParamAction;
import com.github.rccookie.geometry.performance.int2;

import greenfoot.Greenfoot;
import greenfoot.MouseInfo;

public abstract class GreenfootInputAdapter implements InputAdapter {

    /**
     * All keys that were pressed in the last frame.
     */
    private final Set<String> pressed = new HashSet<>();
    /**
     * Received key event to be fired.
     */
    private final BiParamEvent<String, Boolean> keyEvents = new SimpleBiParamEvent<>();
    /**
     * Received mouse event to be fired.
     */
    private final ParamEvent<MouseData> mouseEvents = new SimpleParamEvent<>();

    private final boolean allowMultiClicks;

    protected GreenfootInputAdapter(boolean allowMultiPresses) {
        this.allowMultiClicks = allowMultiPresses;
    }


    @Override
    public void attachKeyEvent(BiParamAction<String, Boolean> event) {
        keyEvents.add(event);
    }

    @Override
    public void attachMouseEvent(ParamAction<MouseData> event) {
        mouseEvents.add(event);
    }

    @Override
    public int2 getMousePos() {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        // Happens online sometimes
        if(mouse == null) return applyMouseOffset(int2.zero());
        return applyMouseOffset(new int2(mouse.getX(), mouse.getY()));
    }

    @Override
    public boolean isKeyDataAvailable() {
        return true;
    }

    @Override
    public boolean isMouseDataAvailable() {
        return Greenfoot.getMouseInfo() != null;
    }

    /**
     * Updates the pressed states of the keys and fires press and release events.
     */
    @Continuous
    @OverrideTarget
    protected void update() {
        if(Greenfoot.mousePressed(null))
            mouseEvents.invoke(getMouseData(true));
        if(Greenfoot.mouseClicked(null))
            mouseEvents.invoke(getMouseData(false));
    }

    protected void keyPressed(String key) {
        key = toValidName(key);
        if(pressed.add(key) || allowMultiClicks)
            keyEvents.invoke(key, true);
    }

    protected void keyReleased(String key) {
        key = toValidName(key);
        if(pressed.remove(key))
            keyEvents.invoke(key, false);
    }

    /**
     * Creates a mouse data object from the current mouse info.
     *
     * @param includeButton Whether the button should be registered or ignored and set to 0
     * @return The mouse data representing the current mouse info
     */
    private MouseData getMouseData(boolean includeButton) {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        // Happens online sometimes
        if(mouse == null) return new MouseData(applyMouseOffset(int2.zero()), 0);
        return new MouseData(applyMouseOffset(new int2(mouse.getX(), mouse.getY())), includeButton ? mouse.getButton() : 0);
    }

    @OverrideTarget
    protected int2 applyMouseOffset(int2 pos) {
        return pos;
    }

    /**
     * Converts greenfoot.Greenfoot key names to valid names.
     *
     * @param key The key to convert
     * @return The valid key name
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static String toValidName(String key) {
        switch(key.toLowerCase()) {
            case "escape":     return "esc";
            case "control":    return "ctrl";
            case "space":      return " ";
            case "\n":         return "enter";
            case "altgraph":   return "alt"; // Only online, map altgr to ctrl+alt. ctrl is already invoked by event
            case "page up":    return "pageup";
            case "page down":  return "pagedown";
            case "arrowleft":  return "left";
            case "arrowright": return "right";
            case "arrowup":    return "up";
            case "arrowdown":  return "down";
            default:           return key.toLowerCase();
        }
    }
}
