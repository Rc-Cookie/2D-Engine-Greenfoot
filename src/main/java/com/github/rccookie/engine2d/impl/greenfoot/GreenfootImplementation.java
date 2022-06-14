package com.github.rccookie.engine2d.impl.greenfoot;

import com.github.rccookie.engine2d.Properties;
import com.github.rccookie.engine2d.impl.DisplayController;
import com.github.rccookie.engine2d.impl.Implementation;

import greenfoot.Greenfoot;

/**
 * An abstract greenfoot.Greenfoot-based Engine2D implementation.
 */
public abstract class GreenfootImplementation implements Implementation {

    /**
     * The implementation instance.
     */
    private static GreenfootImplementation instance;


    /**
     * Has the implementation been started?
     */
    boolean started = false;


    /**
     * Creates a new greenfoot implementation.
     */
    protected GreenfootImplementation() {
        instance = this;
    }

    /**
     * Returns the singleton instance of the implementation.
     *
     * @return The implementation instance
     */
    public static GreenfootImplementation instance() {
        return instance;
    }

    @Override
    public boolean hasExternalUpdateLoop() {
        return true;
    }

    @Override
    public void runExternalUpdateLoop() {
        started = true;
        Greenfoot.setWorld(((GreenfootDisplay) getDisplay()).world);
    }

    @Override
    public void initProperties(Properties properties) {
        properties.set("greenfoot.session", GreenfootSession.REAL);
    }

    @Override
    public void setDisplayController(DisplayController displayController) {
        ((GreenfootDisplay) getDisplay()).displayController = displayController;
    }
}
