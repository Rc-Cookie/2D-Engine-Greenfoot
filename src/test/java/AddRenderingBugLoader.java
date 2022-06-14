import com.github.rccookie.engine2d.Camera;
import com.github.rccookie.engine2d.GameObject;
import com.github.rccookie.engine2d.ILoader;
import com.github.rccookie.engine2d.Input;
import com.github.rccookie.engine2d.Map;
import com.github.rccookie.engine2d.Mouse;
import com.github.rccookie.engine2d.UI;
import com.github.rccookie.engine2d.UIObject;
import com.github.rccookie.engine2d.image.Color;
import com.github.rccookie.engine2d.image.Image;
import com.github.rccookie.engine2d.util.awt.AWTApplicationLoader;
import com.github.rccookie.engine2d.util.awt.AWTStartupPrefs;
import com.github.rccookie.engine2d.impl.greenfoot.AbstractGreenfootApplicationLoader;
import com.github.rccookie.engine2d.util.greenfoot.GreenfootStartupPrefs;
import com.github.rccookie.engine2d.ui.ColorPanel;
import com.github.rccookie.geometry.performance.float2;
import com.github.rccookie.geometry.performance.int2;
import com.github.rccookie.util.Console;

import javafx.stage.Stage;
import javafx.stage.Window;

class AddRenderingBugLoader implements ILoader {

    @Override
    public void load() {
        Camera camera = new Camera(600, 400);
        GameObject cameraObject = new GameObject();
        Map map = new Map();
        cameraObject.setMap(map);
        camera.setGameObject(cameraObject);
        UI ui = new UI(camera);

        for(int i=-5; i<=5; i++)
            new TestObject(map, new float2(i * 50, 0), Color.RED);

        for(int i=-5; i<=5; i++)
            new TestUI(ui, new int2(i * 50, 150), Color.ORANGE);

        camera.update.add(() -> {
            Mouse m = camera.input.getMouse();
            if(!m.pressed) return;
            Console.log("Adding...");
//            new TestObject(map, camera.pixelToPoint(m.pixel), Color.GREEN);
            new TestUI(ui, m.pixel.subed(camera.getResolution().dived(2)), Color.GREEN).moveToBack();
        });


        Input.addKeyPressListener(() -> Stage.getWindows().stream().filter(Window::isShowing).findAny().get().widthProperty().addListener(($, oldValue, newValue) -> Console.log("Resized", oldValue, "to", newValue)), " ");

        camera.setBackgroundColor(Color.DARK_GRAY);
    }

    static class TestObject extends GameObject {
        public TestObject(Map map, float2 location, Color color) {
            setMap(map);
            this.location.set(location);
            setImage(new Image(25, 25, color));
        }
    }

    static class TestUI extends ColorPanel {
        public TestUI(UIObject parent, int2 offset, Color color) {
            super(parent, new int2(1, 1), color);
            this.offset.set(offset);
        }
    }

    public static void main(String[] args) {
        new AWTApplicationLoader(new AddRenderingBugLoader(), new AWTStartupPrefs());
    }

    public static class GreenfootLoader extends AbstractGreenfootApplicationLoader {
        public GreenfootLoader() {
            super(new AddRenderingBugLoader(), new GreenfootStartupPrefs());
        }
    }
}
