import java.util.List;
import java.util.stream.Collectors;

import com.github.rccookie.engine2d.Application;
import com.github.rccookie.engine2d.Camera;
import com.github.rccookie.engine2d.Collider;
import com.github.rccookie.engine2d.Execute;
import com.github.rccookie.engine2d.GameObject;
import com.github.rccookie.engine2d.ILoader;
import com.github.rccookie.engine2d.Input;
import com.github.rccookie.engine2d.Map;
import com.github.rccookie.engine2d.Mouse;
import com.github.rccookie.engine2d.Time;
import com.github.rccookie.engine2d.UI;
import com.github.rccookie.engine2d.UIObject;
import com.github.rccookie.engine2d.image.Color;
import com.github.rccookie.engine2d.image.Font;
import com.github.rccookie.engine2d.image.Image;
import com.github.rccookie.engine2d.physics.BoxCollider;
import com.github.rccookie.engine2d.ui.ColorPanel;
import com.github.rccookie.geometry.performance.float2;
import com.github.rccookie.geometry.performance.int2;
import com.github.rccookie.util.Console;

import greenfoot.Greenfoot;
import greenfoot.UserInfo;

class TestLoader implements ILoader {

    @Override
    public void load() {
        Map map = new Map();
        map.setGravity(new float2(0, -9.81f));
        map.setGravity(float2.zero);

        GameObject cameraObject = new GameObject();
        cameraObject.setImage(new Image(new int2(20, 40), Color.RED));
        cameraObject.location.x = 250;
        cameraObject.velocity.x = -100;
        cameraObject.usePhysics(true);
        new BoxCollider(cameraObject, float2.one.scaled(10));
        cameraObject.setMap(map);
        cameraObject.rotation = 90;

        GameObject gameObject = new GameObject();
        gameObject.setImage(new Image(int2.one.scaled(32), Color.BLUE));
        gameObject.setImage(Font.defaultF(32).render("Hello\nWorld!", Color.BLUE));
        gameObject.usePhysics(true);
        gameObject.getComponent(Collider.class).setRestitution(1);
        gameObject.setMap(map);

//        Settings.maxTranslation = 200f;
//        Settings.maxTranslationSquared = 200f * 200f;

        UI ui = new UI();
//        UIObject debug = new DebugPanel(ui);
//        debug.relativeLoc.set(1, -1);
        UIObject uiObject = new ColorPanel(ui, new int2(400, 100), Color.DARK_GRAY);
        uiObject.relativeLoc.y = 1;
        UIObject uiObject1 = new ColorPanel(ui, int2.one.scaled(16), Color.PINK);
        uiObject1.relativeLoc.set(-1, -1);

//        new TextEditor(ui).setFont(new Font("Fira Code", 20));

        Camera camera = new Camera(new int2(600, 400));

        GameObject cameraManager = new GameObject();
        cameraManager.setImage(new Image(int2.one.scaled(4), Color.BLACK));
        cameraManager.getComponent(Collider.class).setRestitution(0);
        cameraManager.setMap(map);
        Input.addKeyListener(() -> camera.setGameObject(cameraObject), "1");
        Input.addKeyListener(() -> camera.setGameObject(gameObject), "2");
        cameraManager.input.addKeyListener(() -> Console.map("Velocity", cameraObject.velocity, cameraObject.rotation), "t");
        cameraManager.input.addKeyPressListener(() -> cameraManager.velocity.y += 300, "s", "down");
        cameraManager.input.addKeyPressListener(() -> cameraManager.velocity.y -= 300, "w", "up");
        cameraManager.input.addKeyPressListener(() -> cameraManager.velocity.x += 300, "d", "right");
        cameraManager.input.addKeyPressListener(() -> cameraManager.velocity.x -= 300, "a", "left");
        cameraManager.input.addKeyReleaseListener(() -> cameraManager.velocity.y += -300, "s", "down");
        cameraManager.input.addKeyReleaseListener(() -> cameraManager.velocity.y -= -300, "w", "up");
        cameraManager.input.addKeyReleaseListener(() -> cameraManager.velocity.x += -300, "d", "right");
        cameraManager.input.addKeyReleaseListener(() -> cameraManager.velocity.x -= -300, "a", "left");
        cameraManager.input.addKeyListener(() -> cameraManager.angle += 90 * Time.delta(), "e");
        cameraManager.input.addKeyListener(() -> cameraManager.angle -= 90 * Time.delta(), "q");

        camera.update.add(() -> {
            Mouse m = camera.input.getMouse();
            if(!m.pressed) return;
            GameObject o = new GameObject();
            o.setImage(new Image(int2.one.scaled(10), Color.GREEN));
            o.location.set(camera.pixelToPoint(m.pixel));
            o.setMap(map);
        });

        camera.setGameObject(cameraManager);
        camera.setUI(ui);

        Application.setMaxFps(-1);

        Input.keyPressed .add(k -> Console.map(k, "Pressed"));
        Input.keyReleased.add(k -> Console.map(k, "Released"));

        Input.addKeyPressListener(() -> Console.map("^ pressed", Greenfoot.isKeyDown("^")), "ß");

        Application.lateUpdate.add(() -> {
            String key = Greenfoot.getKey();
            if(key != null) Console.map("Key pressed", key);
        });

        Execute.when(() -> uiObject1.update.add(() -> uiObject1.relativeLoc.x = Math.min(1, uiObject1.relativeLoc.x + Time.delta())), () -> Input.getKeyState(" "));
        Execute.when(cameraObject::remove, () -> Input.getKeyState("r"));
        Execute.when(() -> camera.setBackgroundColor(Color.DARK_GRAY.setBlue(1f)), () -> Input.getKeyState("c"));
        Execute.when(() -> camera.setGameObject(null), () -> Input.getKeyState("n"));



        Input.addKeyPressListener(() -> Console.log(((List<UserInfo>) UserInfo.getTop(0)).stream().map(u -> u.getUserName() + ": " + u.getString(0)).collect(Collectors.joining())), "#");
    }
}
