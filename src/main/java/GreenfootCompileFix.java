import com.github.rccookie.engine2d.impl.greenfoot.sessionstorage.GreenfootFileLoader;
import com.github.rccookie.util.Console;

public final class GreenfootCompileFix {

    private GreenfootCompileFix() {
        throw new UnsupportedOperationException();
    }

    public static void fix() {
        GreenfootFileLoader.init();
        Console.debug("Fixed");
    }
}
