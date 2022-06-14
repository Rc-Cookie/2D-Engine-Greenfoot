import com.github.rccookie.engine2d.Camera;
import com.github.rccookie.engine2d.Debug;
import com.github.rccookie.engine2d.ILoader;
import com.github.rccookie.engine2d.UI;
import com.github.rccookie.engine2d.image.Color;
import com.github.rccookie.engine2d.util.awt.AWTApplicationLoader;
import com.github.rccookie.engine2d.util.awt.AWTStartupPrefs;
import com.github.rccookie.engine2d.ui.BackgroundPanel;
import com.github.rccookie.engine2d.ui.Button;
import com.github.rccookie.engine2d.ui.Dimension;
import com.github.rccookie.engine2d.ui.IntInputField;
import com.github.rccookie.engine2d.ui.Switch;
import com.github.rccookie.engine2d.ui.TextButton;
import com.github.rccookie.engine2d.ui.TextEditor;
import com.github.rccookie.engine2d.ui.Toggle;
import com.github.rccookie.engine2d.util.Num;
import com.github.rccookie.geometry.performance.int2;

class SimpleTestLoader implements ILoader {

    @SuppressWarnings("SpellCheckingInspection")
    String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Est ultricies integer quis auctor elit sed vulputate mi. Est lorem ipsum dolor sit amet consectetur. Sit amet dictum sit amet justo donec enim diam vulputate. Purus in mollis nunc sed id semper risus in. Proin fermentum leo vel orci porta. Quis enim lobortis scelerisque fermentum dui faucibus in ornare. Sagittis purus sit amet volutpat consequat mauris. Scelerisque eleifend donec pretium vulputate sapien nec sagittis aliquam. Maecenas ultricies mi eget mauris pharetra et ultrices neque ornare. Eu tincidunt tortor aliquam nulla facilisi cras. Felis eget velit aliquet sagittis id consectetur purus ut faucibus. Eu volutpat odio facilisis mauris sit amet massa vitae. Ultrices neque ornare aenean euismod elementum nisi quis eleifend. Purus viverra accumsan in nisl nisi.\n" +
            "\n" +
            "Massa tincidunt nunc pulvinar sapien et ligula. Cras sed felis eget velit aliquet sagittis id. Netus et malesuada fames ac turpis egestas maecenas. Elementum sagittis vitae et leo duis ut. Egestas fringilla phasellus faucibus scelerisque eleifend donec pretium. Tincidunt vitae semper quis lectus nulla. Est velit egestas dui id. At tempor commodo ullamcorper a lacus vestibulum. Tortor dignissim convallis aenean et tortor at. Eu sem integer vitae justo eget. Adipiscing elit ut aliquam purus. Eget nullam non nisi est sit amet facilisis. Lobortis mattis aliquam faucibus purus in massa tempor. Duis ut diam quam nulla porttitor massa id neque.\n" +
            "\n" +
            "Hendrerit gravida rutrum quisque non tellus orci ac auctor. Elementum nibh tellus molestie nunc non blandit massa enim. Dolor morbi non arcu risus quis varius. Tempor orci eu lobortis elementum nibh. Sed arcu non odio euismod lacinia at quis risus. Sociis natoque penatibus et magnis dis. Tempor commodo ullamcorper a lacus vestibulum sed arcu non. Eget nunc lobortis mattis aliquam faucibus purus. Nibh ipsum consequat nisl vel pretium lectus quam id leo. Nibh venenatis cras sed felis eget velit aliquet. Eu feugiat pretium nibh ipsum consequat nisl. In ornare quam viverra orci sagittis. Nunc id cursus metus aliquam eleifend mi in nulla. Aliquam nulla facilisi cras fermentum. Id velit ut tortor pretium viverra suspendisse potenti nullam.\n" +
            "\n" +
            "Ullamcorper a lacus vestibulum sed arcu non odio. Auctor eu augue ut lectus arcu. Risus at ultrices mi tempus imperdiet nulla malesuada pellentesque. Praesent elementum facilisis leo vel fringilla est ullamcorper. Et netus et malesuada fames ac turpis egestas integer. Ultrices dui sapien eget mi proin sed libero enim. Odio ut enim blandit volutpat maecenas volutpat blandit aliquam etiam. Sapien nec sagittis aliquam malesuada bibendum. Sit amet facilisis magna etiam tempor orci eu lobortis elementum. Nec sagittis aliquam malesuada bibendum arcu vitae elementum curabitur vitae.\n" +
            "\n" +
            "Eget dolor morbi non arcu risus quis. Volutpat ac tincidunt vitae semper quis lectus nulla at volutpat. Habitant morbi tristique senectus et netus et malesuada fames. Semper viverra nam libero justo. Amet massa vitae tortor condimentum lacinia. Commodo sed egestas egestas fringilla phasellus faucibus scelerisque. Lectus quam id leo in vitae turpis massa sed elementum. Eget dolor morbi non arcu. Quam viverra orci sagittis eu volutpat odio. Enim nunc faucibus a pellentesque. Consectetur adipiscing elit ut aliquam purus sit. Ornare massa eget egestas purus viverra accumsan in. Imperdiet massa tincidunt nunc pulvinar sapien et ligula. Eget nullam non nisi est. Orci ac auctor augue mauris augue neque. Tortor aliquam nulla facilisi cras fermentum odio eu feugiat pretium. Quis imperdiet massa tincidunt nunc pulvinar sapien et ligula ullamcorper. Sed risus ultricies tristique nulla aliquet enim tortor at auctor. Id nibh tortor id aliquet.";

    @Override
    public void load() {
        Camera camera = new Camera(600, 400);
        UI ui = new UI(camera);
        ui.setThemedCameraBackground(true);
        Debug.bindOverlayToggle();

        Dimension textArea = new Dimension(ui, ui.getSize().subed(20, 20));
        textArea.onParentSizeChange.add(s -> textArea.setSize(s.subed(20, 20)));

        new BackgroundPanel(textArea, Color.DARK_GRAY.darker());

        new TextEditor(textArea);

        IntInputField widthInput = new IntInputField(ui, camera.getResolution().x, l -> Num.clamp(l, 10, 1000));
        IntInputField heightInput = new IntInputField(ui, camera.getResolution().y, l -> Num.clamp(l, 10, 1000));
        widthInput.relativeLoc.x = -0.5f;
        heightInput.relativeLoc.x = 0.5f;

        Button resizeButton = new TextButton(ui, "Resize");
        resizeButton.relativeLoc.y = 0.5f;
        resizeButton.onClick.add(() -> camera.setResolution(new int2((int)(long)widthInput.getValue(), (int)(long)heightInput.getValue())));

        Toggle allowResizeToggle = new Switch(ui);
        allowResizeToggle.relativeLoc.y = 0.5f;
        allowResizeToggle.offset.y = 50;
        allowResizeToggle.setOn(true);
        allowResizeToggle.onToggle.add(Camera::setAllowExternalResizing);
    }

    public static void main(String[] args) {
        new AWTApplicationLoader(new SimpleTestLoader(), new AWTStartupPrefs());
    }
}
