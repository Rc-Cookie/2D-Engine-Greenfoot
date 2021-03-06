package com.github.rccookie.engine2d.impl.greenfoot.offline;

import com.github.rccookie.engine2d.impl.Display;
import com.github.rccookie.engine2d.impl.IOManager;
import com.github.rccookie.engine2d.impl.ImageManager;
import com.github.rccookie.engine2d.impl.InputAdapter;
import com.github.rccookie.engine2d.impl.OnlineManager;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootDisplay;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootImplementation;
import com.github.rccookie.engine2d.impl.greenfoot.Sensitive;

/**
 * Offline optimized greenfoot.Greenfoot based Engine2D implementation.
 */
public class OfflineGreenfootImplementation extends GreenfootImplementation {

    private final OnlineManager onlineManager = new OfflineGreenfootOnlineManager();
    private Thread mainThread = null;
    private final IOManager ioManager = Sensitive.load("com.github.rccookie.engine2d.impl.greenfoot.offline.OfflineGreenfootIOManager");
    private final GreenfootDisplay display = new OfflineGreenfootDisplay();
    private final InputAdapter inputAdapter = new OfflineGreenfootInputAdapter(display);

    @Override
    public ImageManager getImageFactory() {
        return OfflineGreenfootImageManager.INSTANCE;
    }

    @Override
    public Display getDisplay() {
        return display;
    }

    @Override
    public InputAdapter getInputAdapter() {
        return inputAdapter;
    }

    @Override
    public OnlineManager getOnlineManager() {
        return onlineManager;
    }

    @Override
    public IOManager getIOManager() {
        return ioManager;
    }

    @Override
    public boolean supportsMultithreading() {
        return true;
    }

    @Override
    public boolean supportsNativeIO() {
        return true;
    }

    @Override
    public boolean supportsAWT() {
        return true;
    }

    @Override
    public boolean supportsSleeping() {
        return true;
    }

    @Override
    public void sleep(long millis, int nanos) {
        try { Thread.sleep(millis, nanos); }
        catch(InterruptedException ignored) { }
    }

    @Override
    public void setMainThread() throws IllegalStateException {
        if(mainThread != null) throw new IllegalStateException("Main thread already set");
        mainThread = Thread.currentThread();
    }

    @Override
    public boolean isMainThread() {
        return Thread.currentThread() == mainThread;
    }
}
