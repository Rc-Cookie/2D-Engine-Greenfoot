package com.github.rccookie.engine2d.impl.greenfoot;

import java.util.List;

import com.github.rccookie.engine2d.Application;
import com.github.rccookie.engine2d.Camera;
import com.github.rccookie.engine2d.core.DrawObject;
import com.github.rccookie.engine2d.image.Color;
import com.github.rccookie.engine2d.impl.Display;
import com.github.rccookie.engine2d.impl.DisplayController;
import com.github.rccookie.engine2d.impl.greenfoot.online.OnlineGreenfootImplementation;
import com.github.rccookie.engine2d.util.Pool;
import com.github.rccookie.geometry.performance.int2;

import greenfoot.Actor;
import greenfoot.Greenfoot;
import greenfoot.GreenfootImage;
import greenfoot.World;

/**
 * greenfoot.Greenfoot implementation of a {@link Display}, which uses a world to show output.
 */
public abstract class GreenfootDisplay implements Display {

    /**
     * The world currently used as output. Changes when the resolution changes.
     */
    protected World world;
    /**
     * Last background color. Avoid recreating a new background image if not needed.
     */
    private Color lastBackground = null;

    /**
     * Pool of actors to be used to display draw objects.
     */
    private final Pool<Actor> actorPool = new Pool<>(() -> new Actor() {});
    /**
     * The current display controller.
     */
    DisplayController displayController;


    /**
     * Creates the greenfoot.Greenfoot display.
     */
    protected GreenfootDisplay() {
        world = new ApplicationUpdateWorld(Display.DEFAULT_RESOLUTION);
        if(GreenfootImplementation.instance().started)
            Greenfoot.setWorld(world);
    }

    @Override
    public void draw(DrawObject[] objects, Color background) {

        if(background != lastBackground) {
            GreenfootImage backgroundImage = world.getBackground();
            backgroundImage.setColor(GreenfootImageImpl.toGreenfootColor(background));
            backgroundImage.fill();
            lastBackground = background;
        }

        int addCount = objects.length - world.numberOfObjects();
        for(int i=0; i<addCount; i++)
            world.addObject(actorPool.get(), 0, 0);
        if(addCount < 0) {
            List<Actor> removed = world.getObjects(Actor.class).subList(0, -addCount);
            world.removeObjects(removed);
            actorPool.returnObjects(removed);
        }

        List<Actor> actors = world.getObjects(null);
        for(int i=0; i<objects.length; i++) {
            Actor a = actors.get(i);
            DrawObject d = objects[i];
            a.setLocation(d.screenLocation.x, d.screenLocation.y);
            a.setRotation((int) (d.rotation + 0.5f));
            a.setImage(((GreenfootImageImpl) d.image).image);
        }
    }

    @Override
    public void setResolution(int2 resolution) {
        actorPool.returnObjects(world.getObjects(null));
        world = new ApplicationUpdateWorld(resolution);
        if(lastBackground != null) {
            // Invalidate background color cache: the world has changed, and with it
            // the background image. Don't already fill the new world's image with
            // the last color here, because the change of resolution may be because
            // of a camera change, and with it a different color. This way duplicate
            // filling is avoided.
            lastBackground = null;
            // Avoid flickering: Instantly re-render the camera
            Camera.getActive().prepareRender();
            Camera.getActive().render();
        }
        if(OnlineGreenfootImplementation.instance().started)
            Greenfoot.setWorld(world);
    }

    public World getWorld() {
        return world;
    }

    /**
     * World that runs an application frame every act cycle.
     */
    class ApplicationUpdateWorld extends World {

        /**
         * Creates a new application update world.
         *
         * @param size The size of the world
         */
        public ApplicationUpdateWorld(int2 size) {
            super(size.x, size.y, 1, false);
        }

        @Override
        public void act() {
            ((GreenfootInputAdapter) Application.getImplementation().getInputAdapter()).update();
            displayController.runApplicationFrame();
        }
    }
}
