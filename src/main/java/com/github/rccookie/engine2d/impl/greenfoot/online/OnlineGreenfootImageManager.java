package com.github.rccookie.engine2d.impl.greenfoot.online;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.rccookie.engine2d.image.Color;
import com.github.rccookie.engine2d.image.Font;
import com.github.rccookie.engine2d.impl.ImageImpl;
import com.github.rccookie.engine2d.impl.ImageManager;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootImageImpl;
import com.github.rccookie.engine2d.util.Num;
import com.github.rccookie.engine2d.util.RuntimeIOException;
import com.github.rccookie.engine2d.util.greenfoot.JS;
import com.github.rccookie.geometry.performance.int2;
import com.github.rccookie.util.Utils;

import greenfoot.GreenfootImage;
import org.jetbrains.annotations.NotNull;
import org.teavm.jso.JSBody;

/**
 * Factory for {@link OnlineGreenfootImageImpl}s.
 */
@SuppressWarnings("SpellCheckingInspection")
public enum OnlineGreenfootImageManager implements ImageManager {

    /**
     * Factory instance.
     */
    INSTANCE;

    private static final float FONT_SCALE = 0.86f;
    private static final float BASELINE_OFFSET = 0.3f;
    private static final Map<FontChar, Integer> WIDTHS = new HashMap<>();

    /**
     * A set of known monospace fonts that don't include the literals
     * 'mono' or 'code'.
     */
    private static final Set<String> MONOSPACE_FONTS;
    static {
        Set<String> fonts = new HashSet<>();
        fonts.add("courier");
        fonts.add("consolas");
        fonts.add("iosevka");
        fonts.add("inconsolata");
        fonts.add("cousine");
        fonts.add("anonymous pro");
        fonts.add("lekton");
        fonts.add("hack");
        fonts.add("monaco");
        fonts.add("courier std");
        fonts.add("operator");
        fonts.add("pragmata pro");
        fonts.add("cartograph");
        MONOSPACE_FONTS = Utils.view(fonts);

        initIsFontSupported();
    }

    @Override
    public @NotNull ImageImpl createNew(@NotNull int2 size) {
        return new OnlineGreenfootImageImpl(size);
    }

    @Override
    public @NotNull ImageImpl createNew(@NotNull String file) throws RuntimeIOException {
        return new OnlineGreenfootImageImpl(file);
    }

    @Override
    public @NotNull ImageImpl createText(@NotNull String text, int size, @NotNull Color color) {
        return new OnlineGreenfootImageImpl(new GreenfootImage(text, size, GreenfootImageImpl.toGreenfootColor(color), new greenfoot.Color(0,0,0,0)));
    }

    @Override
    public @NotNull ImageImpl createCharacter(char character, @NotNull Font font, @NotNull Color color) {
        int width = getWidth(new FontChar(font, character));

        greenfoot.Font gFont = new greenfoot.Font(font.name, font.bold, font.italic, Num.round(font.size * FONT_SCALE));
        GreenfootImage image = new GreenfootImage(width, font.size);

        int baseline = Num.round(font.size * (0.5f + BASELINE_OFFSET * FONT_SCALE));

        image.setFont(gFont);
        image.setColor(GreenfootImageImpl.toGreenfootColor(color));
        image.drawString(character + "", 0, baseline);

        int count = font.size / (font.bold ? 5 : 8);
        if(font.strikethrough)
            image.fillRect(0, font.size/2 - 1, width, count-1);
        if(font.underlined)
            image.fillRect(0, baseline + 1, width, count-1);

        return new OnlineGreenfootImageImpl(image);
    }

    private static int getWidth(FontChar c) {
        Integer width = WIDTHS.get(c);
        if(width == null) WIDTHS.put(c, width = calcWidth(c));
        return width;
    }

    private static int calcWidth(FontChar c) {
        if(c.character != 'W' && isMonospace(c.font.name)) return getWidth(new FontChar(c.font, 'W'));
        return calcWidthOnChar(c);
    }

    private static int calcWidthOnChar(FontChar c) {
        int fontSize = Num.round(c.font.size * FONT_SCALE);
        greenfoot.Font gFont = new greenfoot.Font(c.font.name, c.font.bold, c.font.italic, fontSize);
        // Assertion: no char is wider than the biggest char can be tall
        GreenfootImage area = new GreenfootImage(c.font.size, fontSize);

        area.setFont(gFont);
        area.setColor(greenfoot.Color.BLACK);
        area.drawString(c.character + "", 0, Num.round(c.font.size * (0.5f + BASELINE_OFFSET)));

        // Seach for the first pixel that is part of the character
        int w = area.getWidth() - 1;
        outer: for(; w>0; w--) for(int i=0; i<c.font.size; i++)
            if(area.getColorAt(w, i).getAlpha() != 0) break outer;
        w++;

        // Backup for space character (and similar?)
        if(w == 1)
            return c.font.size / 4;
//            w = getWidth(new FontChar(c.font, '{'));

        return w;
    }

    private static boolean isMonospace(String font) {
        font = font.toLowerCase();
        if(font.contains("mono") || font.contains("code")) return true;
        for(String f : MONOSPACE_FONTS)
            if(font.contains(f)) return true;
        return false;
    }


    private static class FontChar {

        final Font font;
        final char character;

        private FontChar(Font font, char character) {
            this.font = font;
            this.character = character;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            FontChar fontChar = (FontChar) o;
            return character == fontChar.character && font.size == fontChar.font.size
                    && font.italic == fontChar.font.italic && font.name.equals(fontChar.font.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(font.name, font.size, font.bold, font.italic, character);
        }
    }

    @Override
    public @NotNull String getDefaultFont() {
        return "Arial";
    }

    @Override
    public @NotNull String getDefaultSerifFont() {
        return "Sans Serif";
    }

    @Override
    public @NotNull String getDefaultMonospaceFont() {
        return "Courier";
    }

    @Override
    public boolean isFontSupported(String font) {
        return isFontSupported0(font);
    }

    @JSBody(params = "font", script = "return isFontAvailable(font)")
    private static native boolean isFontSupported0(String font);

    private static void initIsFontSupported() {
        JS.eval("(function(d){function c(c){b.style.fontFamily=c;e.appendChild(b);f=b.clientWidth;e.removeChild(b);return f}var f,e=d.body,b=d.createElement('span');" +
                "b.innerHTML=Array(100).join('wi');b.style.cssText=['position:absolute','width:auto','font-size:128px','left:-99999px'].join(' !important;');var g=c('monospace')," +
                "h=c('serif'),k=c('sans-serif');window.isFontAvailable=function(b){return g!==c(b+',monospace')||k!==c(b+',sans-serif')||h!==c(b+',serif')}})(document);");
    }
}
