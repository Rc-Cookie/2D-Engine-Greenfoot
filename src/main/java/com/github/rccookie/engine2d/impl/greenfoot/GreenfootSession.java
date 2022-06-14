package com.github.rccookie.engine2d.impl.greenfoot;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import com.github.rccookie.engine2d.impl.StartupPrefs;
import com.github.rccookie.engine2d.util.annotations.Constant;
import com.github.rccookie.util.Console;

import greenfoot.platforms.standalone.GreenfootUtilDelegateStandAlone;
import greenfoot.util.GreenfootUtil;

/**
 * Describes the state of a session using {@link AbstractGreenfootApplicationLoader}.
 */
public enum GreenfootSession {
    /**
     * The application is running online on the greenfoot.Greenfoot website.
     */
    ONLINE,
    /**
     * The application is running inside the greenfoot.Greenfoot application.
     */
    APPLICATION,
    /**
     * The application is running in standalone mode, for example from
     * within an IDE.
     */
    STANDALONE;

    /**
     * The real session state, ignoring any overrides.
     */
    public static final GreenfootSession REAL = calcRealSession();

    /**
     * The current (possibly overridden) session state.
     */
    private static GreenfootSession current = null;

    /**
     * Returns the type of this session. The session state may be overridden
     * using according {@link StartupPrefs}.
     *
     * @return The current (possibly emulated) session
     */
    @Constant
    public static GreenfootSession current() {
        return current;
    }

    /**
     * Initializes the session class and computes real and emulated session
     * states.
     *
     * @param prefs The startup prefs
     */
    static void init() {
        if(AbstractGreenfootApplicationLoader.prefs == null)
            return;
        if(AbstractGreenfootApplicationLoader.prefs.sessionOverride != null) {
            current = AbstractGreenfootApplicationLoader.prefs.sessionOverride;
            Console.split("Session: {} (overridden)", current);
        }
        else {
            current = REAL;
            Console.split("Session: {}", current);
        }
        Console.Config.colored = (current == STANDALONE && REAL == STANDALONE);
    }

    /**
     * Tests the real state of the session.
     *
     * @return The current session type
     */
    private static GreenfootSession calcRealSession() {
        try {
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            try {
                Field delegateField = GreenfootUtil.class.getDeclaredField("delegate");
                delegateField.setAccessible(true);
                return delegateField.get(null) instanceof GreenfootUtilDelegateStandAlone ? GreenfootSession.STANDALONE : GreenfootSession.APPLICATION;
            } catch(Exception e) {
                return GreenfootSession.APPLICATION;
            }
        } catch(Throwable t) {
            return GreenfootSession.ONLINE;
        }
    }
}
