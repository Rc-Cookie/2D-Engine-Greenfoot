package com.github.rccookie.engine2d.util.greenfoot;

import com.github.rccookie.engine2d.ILoader;
import com.github.rccookie.engine2d.impl.greenfoot.AbstractGreenfootApplicationLoader;

/**
 * greenfoot.Greenfoot implementation of an application loader. Extend this class and
 * set it as greenfoot.Greenfoot main class. Example:
 * <pre>
 *     public class GreenfootLoader extends GreenfootApplicationLoader {
 *         public GreenfootLoader() {
 *             super(new MyLoader(), new GreenfootStartupPrefs());
 *         }
 *     }
 * </pre>
 * In standalone mode this class can also serve as main class, starting up
 * greenfoot.Greenfoot as JavaFX application while avoiding having to specify modules and
 * the module path to JavaFX in the command args.
 */
public abstract class GreenfootApplicationLoader extends AbstractGreenfootApplicationLoader {
    public GreenfootApplicationLoader(ILoader loader, GreenfootStartupPrefs prefs) {
        super(loader, prefs);
    }
}
