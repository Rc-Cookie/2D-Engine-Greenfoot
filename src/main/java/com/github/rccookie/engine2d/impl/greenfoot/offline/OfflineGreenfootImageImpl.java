package com.github.rccookie.engine2d.impl.greenfoot.offline;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.github.rccookie.engine2d.image.Color;
import com.github.rccookie.engine2d.image.Image;
import com.github.rccookie.engine2d.impl.ImageImpl;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootImageImpl;
import com.github.rccookie.geometry.performance.int2;
import greenfoot.GreenfootImage;
import org.jetbrains.annotations.NotNull;

/**
 * Offline optimized version of {@link GreenfootImageImpl}.
 */
public class OfflineGreenfootImageImpl extends GreenfootImageImpl {

    /**
     * Creates a new greenfoot.Greenfoot image implementation for the specified
     * {@link GreenfootImage}.
     *
     * @param image The GreenfootImage to use
     */
    OfflineGreenfootImageImpl(GreenfootImage image) {
        super(image);
    }

    /**
     * Creates a new greenfoot.Greenfoot image implementation with the specified size.
     *
     * @param size This size for the image
     */
    OfflineGreenfootImageImpl(int2 size) {
        super(size);
    }

    /**
     * Creates a new greenfoot.Greenfoot image implementation by loading the specified file
     *
     * @param file The path to the file to load
     */
    OfflineGreenfootImageImpl(String file) {
        super(file);
    }

    @Override
    public @NotNull ImageImpl clone() {
        return new OfflineGreenfootImageImpl(new GreenfootImage(image));
    }

    @Override
    public void fillRect(int2 topLeft, int2 size, Color color) {
        Graphics2D g = image.getAwtImage().createGraphics();
        g.setColor(color.getAwtColor());
        g.fillRect(topLeft.x, topLeft.y, size.x, size.y);
        g.dispose();
    }

    @Override
    public void drawRect(int2 topLeft, int2 size, Color color) {
        Graphics2D g = image.getAwtImage().createGraphics();
        g.setColor(color.getAwtColor());
        g.drawRect(topLeft.x, topLeft.y, size.x - 1, size.y - 1);
        g.dispose();
    }

    @Override
    public void fillOval(int2 topLeft, int2 size, Color color) {
        Graphics2D g = image.getAwtImage().createGraphics();
        g.setColor(color.getAwtColor());
        g.fillOval(topLeft.x, topLeft.y, size.x-1, size.y-1);
        g.dispose();
    }

    @Override
    public void drawOval(int2 topLeft, int2 size, Color color) {
        Graphics2D g = image.getAwtImage().createGraphics();
        g.setColor(color.getAwtColor());
        g.drawOval(topLeft.x, topLeft.y, size.x-1, size.y-1);
        g.dispose();
    }

    @Override
    public void drawLine(int2 from, int2 to, Color color) {
        Graphics2D g = image.getAwtImage().createGraphics();
        g.setColor(color.getAwtColor());
        g.drawLine(from.x, from.y, to.x, to.y);
        g.dispose();
    }

    /**
     * Private constructor of {@link GreenfootImage} with no parameters, cached.
     */
    private static Constructor<GreenfootImage> ctor;
    /**
     * The private 'bufferedImage' field in {@link GreenfootImage}, cached.
     */
    private static Field imageField;

    @Override
    public ImageImpl scaled(int2 newSize, Image.AntialiasingMode aaMode) {
        // GreenfootImage creates a copy of it's BufferedImage and reassigns it to
        // itself. Another copy had to be created to return a new instance. Instead,
        // the copy is created manually and assigned to a fresh GreenfootImage using
        // reflection.
        BufferedImage scaled = new BufferedImage(newSize.x, newSize.y, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setComposite(AlphaComposite.Src);
        if(aaMode == Image.AntialiasingMode.OFF)
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        else if(aaMode == Image.AntialiasingMode.LOW)
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        else
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        GreenfootImage scaledGI;
        try {
            if(ctor == null) {
                ctor = GreenfootImage.class.getDeclaredConstructor();
                ctor.setAccessible(true);
                imageField = GreenfootImage.class.getDeclaredField("image");
                imageField.setAccessible(true);
            }
            g.drawImage((BufferedImage) imageField.get(image), 0, 0, newSize.x, newSize.y, null);
            g.dispose();
            scaledGI = ctor.newInstance();
            imageField.set(scaledGI, scaled);
        } catch(Exception e) {
            e.printStackTrace();
            scaledGI = new GreenfootImage(image);
            image.scale(newSize.x, newSize.y);
        }
        return new OfflineGreenfootImageImpl(scaledGI);
    }
}
