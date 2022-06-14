package com.github.rccookie.engine2d.impl.greenfoot;

import com.github.rccookie.engine2d.image.Color;
import com.github.rccookie.engine2d.impl.ImageImpl;
import com.github.rccookie.engine2d.util.RuntimeIOException;
import com.github.rccookie.geometry.performance.int2;
import greenfoot.GreenfootImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * An abstract implementation of an image using {@link GreenfootImage}.
 */
public abstract class GreenfootImageImpl implements ImageImpl {

    /**
     * The {@link GreenfootImage} backing this image implementation.
     */
    public final GreenfootImage image;
    /**
     * The size of the image, cached.
     */
    protected final int2 size;


    /**
     * Creates a new greenfoot image implementation using the given
     * {@link GreenfootImage}.
     *
     * @param image The GreenfootImage to use
     */
    protected GreenfootImageImpl(GreenfootImage image) {
        this.image = image;
        size = new int2(image.getWidth(), image.getHeight());
    }

    /**
     * Creates a new greenfoot image implementation with the given size.
     *
     * @param size The size for the image
     */
    protected GreenfootImageImpl(int2 size) {
        this(new GreenfootImage(size.x, size.y));
    }

    /**
     * Creates a new greenfoot image implementation by loading the specified
     * file.
     *
     * @param file The path to the file to load
     */
    protected GreenfootImageImpl(String file) {
        this(getImageFromFile(file));
    }

    @Override
    public abstract @NotNull ImageImpl clone();

    @Override
    public @Range(from = 0L, to = 255L) int getAlpha() {
        return image.getTransparency();
    }

    @Override
    public void setAlpha(@Range(from = 0L, to = 255L) int a) {
        image.setTransparency(a);
    }

    @Override
    public void setPixel(int2 location, Color color) {
        image.setColorAt(location.x, location.y, toGreenfootColor(color));
    }

    @Override
    public void clear() {
        image.clear();
    }

    @Override
    public Color getPixel(int2 location) {
        return fromGreenfootColor(image.getColorAt(location.x, location.y));
    }

    @Override
    public void drawImage(ImageImpl image, int2 topLeft) {
        this.image.drawImage(((GreenfootImageImpl) image).image, topLeft.x, topLeft.y);
    }

    @Override
    public int2 getSize() {
        return size;
    }


    /**
     * Converts a color to a greenfoot color.
     *
     * @param color The color to convert
     * @return An equivalent greenfoot color
     */
    public static greenfoot.Color toGreenfootColor(Color color) {
        return new greenfoot.Color(color.r, color.g, color.b, color.a);
    }

    /**
     * Converts a greenfoot color to a color.
     *
     * @param color The greenfoot color to convert
     * @return An equivalent color
     */
    public static Color fromGreenfootColor(greenfoot.Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    /**
     * Loads a {@link GreenfootImage} from the specified file.
     *
     * @param file The path to the file to load
     * @return The loaded image
     * @throws RuntimeIOException If an exception occurs
     */
    private static GreenfootImage getImageFromFile(String file) throws RuntimeIOException {
        try {
            return new GreenfootImage(file);
        } catch (IllegalArgumentException e) {
            throw new RuntimeIOException(e);
        }
    }
}
