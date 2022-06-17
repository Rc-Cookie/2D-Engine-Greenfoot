package com.github.rccookie.engine2d.util.greenfoot;

import com.github.rccookie.engine2d.impl.StartupPrefs;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootSession;
import com.github.rccookie.engine2d.impl.greenfoot.online.CommentManager;
import com.github.rccookie.geometry.performance.int2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Startup preferences for the greenfoot application loader.
 */
public class GreenfootStartupPrefs implements StartupPrefs<GreenfootStartupPrefs> {

    /**
     * The startup world size, does not affect the actual resolution
     * of the camera rendered then. Remember that online the size of
     * the first world shown is the biggest size that can be shown in
     * that session.
     */
    public final int2 startupSize = new int2(600, 400);

    /**
     * Overrides the session state. {@code null} means no override.
     */
    public GreenfootSession sessionOverride = null;

    /**
     * Should comments be managed using {@link CommentManager}? (On by default)
     */
    public boolean manageComments = true;

    /**
     * Should the reset button be fixed, as it does not work by default?
     * Offline this will cause the reset button to recompile the scenario,
     * online it will reload the page. (On by default)
     */
    public boolean fixResetButton = true;

    /**
     * The message shown while the application is loading. ("Loading..." by default)
     */
    public String loadingMessage = "Loading...";

    /**
     * The author of the application.
     */
    public boolean credits = true;

    /**
     * Creates a copy of these startup prefs with no reference to this
     * instance.
     *
     * @return A complete copy of the startup prefs
     */
    @Override
    public @NotNull GreenfootStartupPrefs clone() {
        GreenfootStartupPrefs prefs = new GreenfootStartupPrefs();
        prefs.startupSize.set(startupSize);
        prefs.sessionOverride = sessionOverride;
        prefs.manageComments = manageComments;
        prefs.fixResetButton = fixResetButton;
        prefs.loadingMessage = loadingMessage;
        return prefs;
    }

    /**
     * Sets the {@link #startupSize} parameter.
     *
     * @param size The new value
     * @return This instance
     */
    public GreenfootStartupPrefs startupSize(@Nullable int2 size) {
        startupSize.set(size != null ? size : new int2(600, 400));
        return this;
    }

    /**
     * Sets the {@link #sessionOverride} parameter.
     *
     * @param sessionOverride The new value
     * @return This instance
     */
    public GreenfootStartupPrefs sessionOverride(@Nullable GreenfootSession sessionOverride) {
        this.sessionOverride = sessionOverride;
        return this;
    }

    /**
     * Sets the {@link #manageComments} parameter.
     *
     * @param manage The new value
     * @return This instance
     */
    public GreenfootStartupPrefs manageComments(boolean manage) {
        manageComments = manage;
        return this;
    }

    /**
     * Sets the {@link #fixResetButton} parameter.
     *
     * @param fix The new value
     * @return This instance
     */
    public GreenfootStartupPrefs fixResetButton(boolean fix) {
        fixResetButton = fix;
        return this;
    }

    public GreenfootStartupPrefs loadingMessage(String message) {
        loadingMessage = message;
        return this;
    }

    public GreenfootStartupPrefs credits(boolean credits) {
        this.credits = credits;
        return this;
    }
}
