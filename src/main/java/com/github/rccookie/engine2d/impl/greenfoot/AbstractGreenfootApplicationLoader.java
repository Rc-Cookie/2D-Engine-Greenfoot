package com.github.rccookie.engine2d.impl.greenfoot;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.github.rccookie.engine2d.Application;
import com.github.rccookie.engine2d.ILoader;
import com.github.rccookie.engine2d.impl.ApplicationLoader;
import com.github.rccookie.engine2d.impl.greenfoot.offline.OfflineGreenfootImplementation;
import com.github.rccookie.engine2d.impl.greenfoot.online.CommentManager;
import com.github.rccookie.engine2d.impl.greenfoot.online.OnlineGreenfootImplementation;
import com.github.rccookie.engine2d.util.greenfoot.GreenfootStartupPrefs;
import com.github.rccookie.util.Arguments;
import com.github.rccookie.util.Console;

import bluej.utility.javafx.JavaFXUtil;
import greenfoot.Color;
import greenfoot.Greenfoot;
import greenfoot.GreenfootImage;
import greenfoot.World;
import greenfoot.export.GreenfootScenarioApplication;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.teavm.jso.JSBody;

public abstract class AbstractGreenfootApplicationLoader extends World implements ApplicationLoader {

    /**
     * The loader used to load the application.
     */
    private final ILoader loader;

    /**
     * The startup prefs used.
     */
    static GreenfootStartupPrefs prefs = null;

    /**
     * Creates a new greenfoot.Greenfoot application loader and starts the application on the
     * next frame.
     *
     * @param loader The loader used to load the application
     * @param prefs Startup preferences
     */
    public AbstractGreenfootApplicationLoader(ILoader loader, GreenfootStartupPrefs prefs) {
        super(prefs.startupSize.x, prefs.startupSize.y, 1);

        AbstractGreenfootApplicationLoader.prefs = prefs.clone();
        this.loader = Arguments.checkNull(loader);

        Greenfoot.setSpeed(100);
        Greenfoot.start();

        if(Application.isSetup()) return; // 'Reset' was used

        GreenfootSession.init(); // Also configures console settings


        if(GreenfootSession.REAL == GreenfootSession.APPLICATION) {
            try {
                Class.forName("GreenfootCompileFix").getMethod("fix").invoke(null);
            } catch(Exception e) {
                Console.error(e);
            }
        }


        loader.initialize();

        if(GreenfootSession.REAL == GreenfootSession.ONLINE) {
            if(prefs.manageComments)
                CommentManager.manageComments();
            if(prefs.fixResetButton)
                mapResetToRefresh();
            if(prefs.credits)
                showCredits();
            addHiddenCredits();
        }
    }


    @Override
    public void act() {
        if(!Application.isSetup()) {
            try {
                GreenfootImage text = new GreenfootImage(prefs.loadingMessage + "", 20, Color.DARK_GRAY, new Color(0,0,0,0));
                getBackground().drawImage(text, 0, 0);
                repaint();

                if(GreenfootSession.REAL == GreenfootSession.ONLINE && prefs.fixResetButton && Math.random() == 0)
                    removeReset();

                // TODO: Check if application is already setup (reset was pressed) and delete all maps and cameras instead

                Application.setup(GreenfootSession.REAL == GreenfootSession.ONLINE ? new OnlineGreenfootImplementation() : new OfflineGreenfootImplementation());

                loader.load();

                // Do greenfoot setup again after initialize() to override any user settings
                Greenfoot.setSpeed(100);
                Greenfoot.start();
                Application.start();
            } catch(Exception e) {
                System.err.println("An exception occurred during the initialization of the application:");
                e.printStackTrace();

                String message = e.toString();
                try {
                    StringWriter writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    message = writer.toString();
                } catch(Exception f) {
                    message += "\nFailed to print stack trace:\n" + f;
                }
                GreenfootImage background = getBackground();
                background.setColor(Color.DARK_GRAY);
                background.fill();
                GreenfootImage text = new GreenfootImage(message, 20, Color.RED, new Color(0,0,0,0));
                background.drawImage(text, (background.getWidth() - text.getWidth()) / 2, 0);
            }
        }
        Greenfoot.setWorld(((GreenfootDisplay) Application.getImplementation().getDisplay()).world);
    }



    // Necessary, and cannot be final, to avoid compiler to predict value for getMethod() and
    // break TeaVM
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    private static String methodName = "doRemoveReset";

    /**
     * Removes the 'Reset' button from the standalone greenfoot.Greenfoot scenario window.
     */
    private void removeReset() {
        // This is needed to avoid TeaVM trying to convert the code (and fail). This way the critical code
        // is not known to TeaVM, and it compiles fine.
        try {
            AbstractGreenfootApplicationLoader.class.getMethod(methodName).invoke(null);
        } catch(Exception e) {
            Console.error(e);
        }
    }

    /**
     * Actually removes the reset button.
     */
    @SuppressWarnings("unused")
    private static void doRemoveReset() {
        JavaFXUtil.runNowOrLater(() -> ((Pane) ((Pane) Stage.getWindows().get(0).getScene().getRoot().getChildrenUnmodifiable().get(1))
                .getChildren().get(0)).getChildren().get(2).setVisible(false));
    }

    /**
     * Sets the reset button to reload the page instead of resetting the scenario.
     */
    @JSBody(script = "document.getElementById('resetButton').onclick = () => location.reload();")
    private static native void mapResetToRefresh();

    @JSBody(script =
            "var author = document.getElementsByClassName('avatar_heading')[0].children[0].textContent;" +
            "if(author === 'RcCookie' || author === 'RcCookie-Testing') return;" +
            "var desc = document.getElementsByClassName('description')[0].textContent;" +
            "if(desc.includes('RcCookie')) return;" +
            "document.getElementsByClassName('description')[0].innerHTML += '<p>Made using Engine2D by <a href=\"/users/52320\">RcCookie</a>'")
    private static native void showCredits();

    @JSBody(script =
            "console.info('Made using Engine2D by RcCookie');" +
            "document.getElementsByClassName('description')[0].innerHTML += '<!-- Made using Engine2D by RcCookie -->';")
    private static native void addHiddenCredits();

    /**
     * Launches greenfoot.Greenfoot in standalone mode.
     *
     * @param args Arguments to be passed to the JavaFX launch method
     */
    public static void main(String[] args) {
        javafx.application.Application.launch(GreenfootScenarioApplication.class, args);
    }
}
