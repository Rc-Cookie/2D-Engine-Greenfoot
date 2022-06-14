package com.github.rccookie.engine2d.impl.greenfoot.offline;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import com.github.rccookie.engine2d.image.Color;
import com.github.rccookie.engine2d.image.Font;
import com.github.rccookie.engine2d.impl.ImageImpl;
import com.github.rccookie.engine2d.impl.ImageManager;
import com.github.rccookie.engine2d.impl.awt.AWTImageImpl;
import com.github.rccookie.engine2d.impl.awt.AWTImageManager;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootImageImpl;
import com.github.rccookie.engine2d.util.RuntimeIOException;
import com.github.rccookie.geometry.performance.int2;

import greenfoot.GreenfootImage;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for {@link OfflineGreenfootImageImpl}s.
 */
enum OfflineGreenfootImageManager implements ImageManager {

    /**
     * The factory instance.
     */
    INSTANCE;

    private final AWTImageManager awtManager = new AWTImageManager();

    @Override
    public @NotNull ImageImpl createNew(@NotNull int2 size) {
        return new OfflineGreenfootImageImpl(size);
    }

    @Override
    public @NotNull ImageImpl createNew(@NotNull String file) throws RuntimeIOException {
        return new OfflineGreenfootImageImpl(file);
    }

    @Override
    public @NotNull ImageImpl createText(@NotNull String text, int size, @NotNull Color color) {
        return new OfflineGreenfootImageImpl(new GreenfootImage(text, size, GreenfootImageImpl.toGreenfootColor(color), new greenfoot.Color(0, 0, 0, 0)));
    }

    @Override
    public @NotNull ImageImpl createCharacter(char character, @NotNull Font font, @NotNull Color color) {
        try {
            ImageImpl awtImage = awtManager.createCharacter(character, font, color);
            Field field = AWTImageImpl.class.getDeclaredField("image");
            field.setAccessible(true);
            BufferedImage img = (BufferedImage) field.get(awtImage);

            GreenfootImage image = new GreenfootImage(img.getWidth(), img.getHeight());
            Graphics2D g = image.getAwtImage().createGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();

            return new OfflineGreenfootImageImpl(image);
        } catch (IllegalAccessException|NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull String getDefaultFont() {
        return awtManager.getDefaultFont();
    }

    @Override
    public @NotNull String getDefaultSerifFont() {
        return awtManager.getDefaultSerifFont();
    }

    @Override
    public @NotNull String getDefaultMonospaceFont() {
        return awtManager.getDefaultMonospaceFont();
    }

    @Override
    public boolean isFontSupported(String font) {
        return awtManager.isFontSupported(font);
    }
}
