package com.github.rccookie.engine2d.impl.greenfoot.online;

import com.github.rccookie.engine2d.image.Color;
import com.github.rccookie.engine2d.image.Image;
import com.github.rccookie.engine2d.impl.ImageImpl;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootImageImpl;
import com.github.rccookie.geometry.performance.int2;
import greenfoot.GreenfootImage;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link GreenfootImageImpl} for online use.
 */
class OnlineGreenfootImageImpl extends GreenfootImageImpl {

    /**
     * The color last used, to avoid unnecessary conversions.
     */
    Color currentColor = null;

    /**
     * Creates a new greenfoot.Greenfoot image implementation for the specified
     * {@link GreenfootImage}.
     *
     * @param image The GreenfootImage to use
     */
    OnlineGreenfootImageImpl(GreenfootImage image) {
        super(image);
    }

    /**
     * Creates a new greenfoot.Greenfoot image implementation with the specified size.
     *
     * @param size This size for the image
     */
    OnlineGreenfootImageImpl(int2 size) {
        super(size);
    }

    /**
     * Creates a new greenfoot.Greenfoot image implementation by loading the specified file
     *
     * @param file The path to the file to load
     */
    OnlineGreenfootImageImpl(String file) {
        super(file);
    }

    @Override
    @NotNull
    public ImageImpl clone() {
        return new OnlineGreenfootImageImpl(new GreenfootImage(image));
    }

    @Override
    public void fillRect(int2 topLeft, int2 size, Color color) {
        updateColor(color);
        image.fillRect(topLeft.x, topLeft.y, size.x, size.y);
    }

    @Override
    public void drawRect(int2 topLeft, int2 size, Color color) {
        updateColor(color);
        image.drawRect(topLeft.x, topLeft.y, size.x - 1, size.y - 1);
    }

    @Override
    public void fillOval(int2 topLeft, int2 size, Color color) {
        updateColor(color);
        image.fillOval(topLeft.x, topLeft.y, size.x-1, size.y-1);
    }

    @Override
    public void drawOval(int2 topLeft, int2 size, Color color) {
        updateColor(color);
        image.drawOval(topLeft.x, topLeft.y, size.x-1, size.y-1);
    }

    @Override
    public void drawLine(int2 from, int2 to, Color color) {
        updateColor(color);
        image.drawLine(from.x, from.y, to.x, to.y);
    }

    @Override
    public ImageImpl scaled(int2 newSize, Image.AntialiasingMode aaMode) {
        // No way of changing antialiasing mode here...
        GreenfootImage scaled = new GreenfootImage(image);
        scaled.scale(newSize.x, newSize.y);
        return new OnlineGreenfootImageImpl(scaled);
    }

    /**
     * Updates the image's color to the given one, if needed.
     *
     * @param color The color to use
     */
    private void updateColor(Color color) {
        if(currentColor == color) return;
        currentColor = color;
        image.setColor(toGreenfootColor(color));
    }
}
