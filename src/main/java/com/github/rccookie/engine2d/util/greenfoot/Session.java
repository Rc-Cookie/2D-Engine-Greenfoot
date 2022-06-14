package com.github.rccookie.engine2d.util.greenfoot;

import com.github.rccookie.engine2d.Application;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootImplementation;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootSession;

public final class Session {

    static {
        if(!Application.isSetup())
            throw new IllegalStateException("Session cannot be used before the application was setup. Reference GreenfootSession directly for StartupPrefs");
    }

    private Session() {
        throw new UnsupportedOperationException();
    }

    /**
     * The application is running online on the greenfoot.Greenfoot website.
     */
    public static final GreenfootSession ONLINE = GreenfootSession.ONLINE;
    /**
     * The application is running inside the greenfoot.Greenfoot application.
     */
    public static final GreenfootSession APPLICATION = GreenfootSession.APPLICATION;
    /**
     * The application is running in standalone mode, for example from
     * within an IDE.
     */
    public static final GreenfootSession STANDALONE = GreenfootSession.STANDALONE;
    /**
     * The real session state, ignoring any overrides. {@code null} means the application
     * is not running using a greenfoot.Greenfoot based implementation.
     */
    public static final GreenfootSession REAL;
    /**
     * The current (possibly overridden) session state. {@code null} means the application
     * is not running using a greenfoot.Greenfoot based implementation.
     */
    public static final GreenfootSession CURRENT;

    static {
        if(Application.getImplementation() instanceof GreenfootImplementation) {
            REAL = GreenfootSession.REAL;
            CURRENT = GreenfootSession.current();
        }
        else {
            REAL = CURRENT = null;
        }
    }
}
