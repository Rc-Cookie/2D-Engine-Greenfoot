import com.github.rccookie.engine2d.impl.greenfoot.AbstractGreenfootApplicationLoader;
import com.github.rccookie.engine2d.util.greenfoot.GreenfootStartupPrefs;
import com.github.rccookie.geometry.performance.int2;

public class GreenfootLoader extends AbstractGreenfootApplicationLoader {

    public GreenfootLoader() {
        super(new SimpleTestLoader(), new GreenfootStartupPrefs().startupSize(new int2(600, 400)));
    }
}
