package com.github.rccookie.engine2d.impl.greenfoot.online;

import com.github.rccookie.engine2d.impl.Display;
import com.github.rccookie.engine2d.impl.IOManager;
import com.github.rccookie.engine2d.impl.ImageManager;
import com.github.rccookie.engine2d.impl.InputAdapter;
import com.github.rccookie.engine2d.impl.OnlineManager;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootImplementation;

/**
 * Online based greenfoot.Greenfoot Engine2D implementation.
 */
public class OnlineGreenfootImplementation extends GreenfootImplementation {

    private final IOManager ioManager = new OnlineGreenfootIOManager();
    private final Display display = new OnlineGreenfootDisplay();

    @Override
    public ImageManager getImageFactory() {
        return OnlineGreenfootImageManager.INSTANCE;
    }

    @Override
    public Display getDisplay() {
        return display;
    }

    @Override
    public InputAdapter getInputAdapter() {
        return OnlineGreenfootInputAdapter.INSTANCE;
    }

    @Override
    public OnlineManager getOnlineManager() {
        return OnlineGreenfootOnlineManager.INSTANCE;
    }

    @Override
    public IOManager getIOManager() {
        return ioManager;
    }

    @Override
    public boolean supportsMultithreading() {
        return false;
    }

    @Override
    public boolean supportsNativeIO() {
        return false;
    }

    @Override
    public boolean supportsAWT() {
        return false;
    }

    @Override
    public boolean supportsSleeping() {
        return false;
    }

    @Override
    public void sleep(long millis, int nanos) {
        // Only used if supportsSleeping is false
        long targetTime = System.nanoTime() + (millis * 1000000) + nanos;
        //noinspection StatementWithEmptyBody
        while(System.nanoTime() < targetTime);
    }

    @Override
    public void setMainThread() throws IllegalStateException {
        // Nothing to do; there is only one thread.
    }

    @Override
    public boolean isMainThread() {
        return true;
    }
}
