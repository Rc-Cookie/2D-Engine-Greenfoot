package com.github.rccookie.engine2d.util.greenfoot.build;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.github.rccookie.util.Args;
import com.github.rccookie.util.ArgsParser;
import com.github.rccookie.util.Console;
import com.github.rccookie.util.login.Login;
import com.github.rccookie.util.login.Passwords;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

/**
 * Utility console application for building a (maven or src folder) project
 * into a normal greenfoot.Greenfoot project with all necessary dependencies included.
 */
public final class Main {

    /**
     * Dependencies to include, relative to the user home folder.
     */
    private static final String[] DEPENDENCIES = {
            "Documents\\Code\\Java\\packages\\2D-Engine",
            "Documents\\Code\\Java\\packages\\2D-Engine-greenfoot.Greenfoot",
            "Documents\\Code\\Java\\packages\\event",
            "Documents\\Code\\Java\\packages\\geometry",
            "Documents\\Code\\Java\\packages\\json",
            "Documents\\Code\\Java\\packages\\util"
    };

    private Main() {
        throw new UnsupportedOperationException();
    }

    /**
     * Runs the builder.
     *
     * @param args Get information using -h
     * @throws IOException If an IOException occurs
     */
    public static void main(String[] args) throws IOException, InterruptedException, AWTException {
        ArgsParser parser = new ArgsParser();
        parser.addDefaults();
        parser.addOption('d', "dir", true, "Root directory of the project");
        parser.addOption(null, "dep", true, "Additional dependencies, seperated by ';'");
        parser.addOption(null, "noMaven", false, "Indicates the project has no maven layout");
        parser.addOption('x', "export", false, "Automatically deploys the project to the greenfoot website");
        parser.addOption('o', "open", false, "Open the project in greenfoot.Greenfoot after it was built");
        parser.addOption('t', "test", false, "Export using test account ('[test]greenfoot.org' as host)");
        Args options = parser.parse(args);
        Console.debug(options.get("dir"), args);

        File project = new File(options.getOr("dir", ".")).getCanonicalFile();
        String name = project.getName();
        boolean maven = !options.is("noMaven");

        Console.map("Building project", name);
        Console.map("Maven", !options.is("noMaven"));
        Console.mapDebug("Root directory", project);

        File buildDir = new File(project, name);
        buildDir.mkdir();

        String userHome = System.getProperty("user.home");
        String[] deps;
        if(options.is("dep") && !options.get("dep").isBlank()) {
            String[] extraDeps = options.get("dep").split(";");
            deps = new String[DEPENDENCIES.length + extraDeps.length];
            System.arraycopy(DEPENDENCIES, 0, deps, 0, DEPENDENCIES.length);
            for(int i=0; i<DEPENDENCIES.length; i++)
                deps[i] = userHome + "\\" + DEPENDENCIES[i];
            System.arraycopy(extraDeps, 0, deps, DEPENDENCIES.length, extraDeps.length);
        }
        else {
            deps = new String[DEPENDENCIES.length];
            for(int i=0; i<DEPENDENCIES.length; i++)
                deps[i] = userHome + "\\" + DEPENDENCIES[i];
        }

        Console.mapDebug("Dependencies", deps);

        Arrays.stream(deps).parallel().forEach(dep -> {
//        for(String dep : deps) {#
            synchronized(Console.class) {
                Console.log("Copying dependency '{}'...", dep.substring(Math.max(dep.lastIndexOf('\\'), dep.lastIndexOf('/')) + 1));
            }
            try {
                copyDir(new File(dep, "src\\main\\java"), buildDir, f -> f.getName().endsWith(".java"));
                copyDir(new File(dep, "target\\classes"), buildDir, f -> f.getName().endsWith(".class"));
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        });

        Console.log("Copying project files...");
        copyDir(new File(project, maven ? "src\\main\\java" : "src"), buildDir, f -> f.getName().endsWith(".java") || f.getName().endsWith(".class"));
        copyDir(new File(project, maven ? "target\\classes" : "out\\production\\" + name), buildDir, f -> f.getName().endsWith(".java") || f.getName().endsWith(".class"));

        if(maven) {
            File resources = new File(project, "src\\main\\resources");
            if(resources.exists()) {
                Console.log("Copying resources...");
                copyDir(resources, buildDir, f -> !f.getName().equals("standalone.properties"));
            }
        }

        File gFile = new File(buildDir, "project.greenfoot");
        if(!gFile.exists()) {
            Console.log("Creating project.greenfoot file...");
            Files.writeString(gFile.toPath(), "height=611\n" +
                    "project.charset=windows-1252\n" +
                    "simulation.speed=50\n" +
                    "version=3.0.0\n" +
                    "width=853\n" +
                    "world.lastInstantiated=GreenfootLoader\n" +
                    "xPosition=662\n" +
                    "yPosition=292\n");
        }

        Console.log("Indexing resources...");

        if(new File(buildDir, "resources").isDirectory()) {
            StringBuilder loaderFile = new StringBuilder();
            loaderFile.append("package com.github.rccookie.engine2d.impl.greenfoot.sessionstorage;\n");
            loaderFile.append("import static com.github.rccookie.engine2d.impl.greenfoot.GreenfootBase64.decode;\n");
            loaderFile.append("import static com.github.rccookie.engine2d.impl.greenfoot.GreenfootFileLookup.set;\n");
            loaderFile.append("public final class GreenfootFileLoader {\n");
            loaderFile.append("    private GreenfootFileLoader() { throw new UnsupportedOperationException(); }\n");
            loaderFile.append("    public static void init() { }\n");
            loaderFile.append("    public static void load() {\n");

            Base64.Encoder encoder = Base64.getEncoder();

            Path root = Path.of(buildDir.toString(), "resources");
            Files.walkFileTree(root, new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String key = root.relativize(file).normalize().toString();
                    String value = Files.readString(file);
                    loaderFile.append("        set(\"");
                    loaderFile.append(escape(key)).append("\", decode(\"");
                    loaderFile.append(new String(encoder.encode(value.getBytes())));
                    loaderFile.append("\"));\n");
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    Console.error(exc);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });

            loaderFile.append("    }\n}\n");
            Files.writeString(Path.of(buildDir + "/com/github/rccookie/engine2d/impl/greenfoot/sessionstorage/GreenfootFileLoader.java"), loaderFile.toString());
            Files.deleteIfExists(Path.of(buildDir + "/com/github/rccookie/engine2d/impl/greenfoot/sessionstorage/GreenfootFileLoader.class"));

            Console.debug("Compiling indexer class...");
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            compiler.run(null, null, null, "--release", "11", "-cp", buildDir.getAbsolutePath(), buildDir.getAbsolutePath() + "/com/github/rccookie/engine2d/impl/greenfoot/sessionstorage/GreenfootFileLoader.java");
        }

        if(options.is("open") || options.is("export")) {
            String username, password;
            if(options.is("export")) {
                Login login = Passwords.get((options.is("test") ? "[test]" : "") + "greenfoot.org");
                username = login.username;
                password = login.password;
            } else username = password = null;

            openGreenfoot(buildDir, username, password);
        }

        Console.log("Done.");
    }

    /**
     * Copies the specified directory's content into the target directory.
     *
     * @param src The directory to copy all contents of
     * @param target The directory to paste the contents into
     * @param filter Filter for which files to include
     * @throws IOException If an IOException occurs
     */
    private static void copyDir(File src, File target, Predicate<File> filter) throws IOException {
        Path srcPath = src.toPath(), targetPath = target.toPath();
        Files.walk(srcPath).forEach(f -> {
            try {
                File file = f.toFile();
                if(file.isFile() && filter.test(file)) {
                    Path currentTarget = targetPath.resolve(srcPath.relativize(f));
                    new File(currentTarget.toFile().getParent()).mkdirs();
                    Files.copy(f, currentTarget, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static String escape(String s){
        return s.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\f", "\\f")
                .replace("'", "\\'")
                .replace("\"", "\\\"");
    }

    /**
     * Opens the specified greenfoot.Greenfoot project (if not already opened). If {@code username}
     * is not null, the scenario will also be exported, unless it is new and not an update.
     *
     * @param path The path to the greenfoot.Greenfoot project directory
     * @param username The username for greenfoot.org, or {@code null} if the project should
     *                 not be exported
     * @param password The password for greenfoot.org. Ignored if {@code username} is
     *                 {@code null}
     */
    private static void openGreenfoot(File path, String username, String password) throws IOException, InterruptedException, AWTException, UnsatisfiedLinkError {

        WinDef.HWND window = User32.INSTANCE.FindWindow(null, "greenfoot.Greenfoot: " + path.getName());

        if(!User32.INSTANCE.IsWindowVisible(window)) {
            Console.debug("Window not found, starting new instance...");
            Console.log("Starting greenfoot.Greenfoot...");
            Runtime.getRuntime().exec(new String[] { "C:\\Program Files\\greenfoot.Greenfoot\\greenfoot.Greenfoot.exe", path.toString() });

            do {
                Console.debug("Waiting...");
                //noinspection BusyWait
                Thread.sleep(500);
                window = User32.INSTANCE.FindWindow(null, "greenfoot.Greenfoot: " + path.getName());
            } while(!User32.INSTANCE.IsWindowVisible(window));

            Console.debug("Started");
            if(username == null) return;

            User32.INSTANCE.MoveWindow(window, 600, 400, 700, 400, true);
            WinDef.RECT rect = new WinDef.RECT();
            do {
                Console.debug("Waiting for compile...");
                //noinspection BusyWait
                Thread.sleep(500);
                User32.INSTANCE.GetWindowRect(window, rect);
                rect.read();
            } while(rect.left == 600 && rect.top == 400 && rect.right == 600+700 && rect.bottom == 400+400);
            User32.INSTANCE.SetForegroundWindow(window); // Over terminal window

            Console.debug("Compiled");
        }
        else Console.debug("Window found");

        User32.INSTANCE.ShowWindow(window, User32.SW_SHOWNORMAL);
        User32.INSTANCE.SetForegroundWindow(window);
        if(username == null) return;

        Thread.sleep(10);
        Robot robot = new Robot();

        WinDef.HWND exportWindow = User32.INSTANCE.GetForegroundWindow();

        if(exportWindow.getPointer().equals(window.getPointer())) {
            Console.debug("Opening export window...");

            User32.INSTANCE.MoveWindow(window, 600, 400, 830, 580, true);
            Thread.sleep(10);
            click(robot, 1340, 485);

            do {
                //noinspection BusyWait
                Thread.sleep(250);
                exportWindow = User32.INSTANCE.GetForegroundWindow();
            } while(exportWindow.getPointer().equals(window.getPointer()));

            Console.debug("Opened");
        }
        else Console.debug("Export window already open");

        User32.INSTANCE.MoveWindow(exportWindow, 700, 350, 630, 700, true);
        Thread.sleep(10);

        click(robot, 920, 920);
        robot.keyPress(KeyEvent.VK_CONTROL);
        type(robot, "a");
        robot.keyRelease(KeyEvent.VK_CONTROL);
        type(robot, username);

        click(robot, 1120, 920);
        robot.keyPress(KeyEvent.VK_CONTROL);
        type(robot, "a");
        robot.keyRelease(KeyEvent.VK_CONTROL);
        type(robot, password);

        Thread.sleep(500);

        if(robot.getPixelColor(1130, 650).equals(Color.WHITE)) {
            Console.debug("New scenario, not uploading");
            return;
        }
        click(robot, 750, 990);

        Console.log("Uploading...");
    }

    private static void click(Robot robot, int x, int y) {
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private static void type(Robot robot, String chars) {
        for(char c : chars.toCharArray())
            KeyStroke.STROKE_MAP.get(c).type(robot);
    }



    private static class KeyStroke {

        private static final Map<Character, KeyStroke> STROKE_MAP = new HashMap<>() {{
            put('\n',new KeyStroke(KeyEvent.VK_ENTER,false));
            put('\t',new KeyStroke(KeyEvent.VK_TAB,false));
            put('\r',new KeyStroke(KeyEvent.VK_HOME,false));
            put(' ',new KeyStroke(KeyEvent.VK_SPACE,false));
            put('!',new KeyStroke(KeyEvent.VK_1,true));
            put('"',new KeyStroke(KeyEvent.VK_QUOTE,true));
            put('#',new KeyStroke(KeyEvent.VK_3,true));
            put('$',new KeyStroke(KeyEvent.VK_4,true));
            put('%',new KeyStroke(KeyEvent.VK_5,true));
            put('&',new KeyStroke(KeyEvent.VK_7,true));
            put('\'',new KeyStroke(KeyEvent.VK_QUOTE,false));
            put('(',new KeyStroke(KeyEvent.VK_9,true));
            put(')',new KeyStroke(KeyEvent.VK_0,true));
            put('*',new KeyStroke(KeyEvent.VK_8,true));
            put('+',new KeyStroke(KeyEvent.VK_EQUALS,true));
            put(',',new KeyStroke(KeyEvent.VK_COMMA,false));
            put('-',new KeyStroke(KeyEvent.VK_MINUS,false));
            put('.',new KeyStroke(KeyEvent.VK_PERIOD,false));
            put('/',new KeyStroke(KeyEvent.VK_SLASH,false));
            for(int i='0';i<=(int)'9';i++){
                put((char)i,new KeyStroke(i,false));
            }
            put(':',new KeyStroke(KeyEvent.VK_SEMICOLON,true));
            put(';',new KeyStroke(KeyEvent.VK_SEMICOLON,false));
            put('<',new KeyStroke(KeyEvent.VK_COMMA,true));
            put('=',new KeyStroke(KeyEvent.VK_EQUALS,false));
            put('>',new KeyStroke(KeyEvent.VK_PERIOD,true));
            put('?',new KeyStroke(KeyEvent.VK_SLASH,true));
            put('@',new KeyStroke(KeyEvent.VK_2,true));
            for(int i='A';i<=(int)'Z';i++){
                put((char)i,new KeyStroke(i,true));
            }
            put('[',new KeyStroke(KeyEvent.VK_OPEN_BRACKET,false));
            put('\\',new KeyStroke(KeyEvent.VK_BACK_SLASH,false));
            put(']',new KeyStroke(KeyEvent.VK_CLOSE_BRACKET,false));
            put('^',new KeyStroke(KeyEvent.VK_6,true));
            put('_',new KeyStroke(KeyEvent.VK_MINUS,true));
            put('`',new KeyStroke(KeyEvent.VK_BACK_QUOTE,false));
            for(int i='A';i<=(int)'Z';i++){
                put((char)(i+((int)'a'-(int)'A')),new KeyStroke(i,false));
            }
            put('{',new KeyStroke(KeyEvent.VK_OPEN_BRACKET,true));
            put('|',new KeyStroke(KeyEvent.VK_BACK_SLASH,true));
            put('}',new KeyStroke(KeyEvent.VK_CLOSE_BRACKET,true));
            put('~',new KeyStroke(KeyEvent.VK_BACK_QUOTE,true));
        }};

        int code;
        boolean isShifted;
        public KeyStroke(int keyCode,boolean shift){
            code=keyCode;
            isShifted=shift;
        }
        public void type(Robot robot){
            try{
                if (isShifted) {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                }
                robot.keyPress(code);
                robot.keyRelease(code);
                if (isShifted) {
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                }
                if(code==KeyEvent.VK_ENTER){
                    robot.keyPress(KeyEvent.VK_HOME);
                    robot.keyRelease(KeyEvent.VK_HOME);
                }

            } catch(IllegalArgumentException ex){
                String ch="";
                for(char key: STROKE_MAP.keySet()){
                    if(STROKE_MAP.get(key)==this){
                        ch=""+key;
                        break;
                    }
                }
                System.err.println("Key Code Not Recognized: '"+ch+"'->"+code);
            }
        }
    }
}
