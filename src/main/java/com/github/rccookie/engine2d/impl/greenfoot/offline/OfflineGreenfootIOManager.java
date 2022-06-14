package com.github.rccookie.engine2d.impl.greenfoot.offline;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.github.rccookie.engine2d.impl.IOManager;
import com.github.rccookie.engine2d.impl.greenfoot.CompileSensitive;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootFileLookup;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootSession;
import com.github.rccookie.engine2d.impl.greenfoot.sessionstorage.GreenfootFileLoader;
import com.github.rccookie.engine2d.util.RuntimeIOException;
import com.github.rccookie.util.Console;
import com.github.rccookie.util.Future;
import com.github.rccookie.util.UncheckedException;

import org.jetbrains.annotations.NotNull;

@CompileSensitive
public class OfflineGreenfootIOManager implements IOManager {

    static {
        loadFiles();
    }

    @Override
    public String read(String file) throws RuntimeIOException {
        return GreenfootFileLookup.get(file);
    }

    @NotNull
    @Override
    public String @NotNull [] listFiles() {
        return GreenfootFileLookup.list();
    }

    @Override
    public Future<String> getClipboard() {
        try {
            return Future.of((String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getContents(this)
                    .getTransferData(DataFlavor.stringFlavor));
        } catch (Exception e) {
            Console.error(e);
            return Future.of("");
        }
    }

    @Override
    public void setClipboard(@NotNull String content) {
        try {
            StringSelection str = new StringSelection(content);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(str, str);
        } catch(Exception e) {
            throw new UncheckedException(e);
        }
    }

    private static void loadFiles() {
        try {
            boolean maven = Files.exists(Path.of("pom.xml"));
            Path root;
            if(maven)
                root = Path.of("src/main/resources/resources");
            else root = Path.of("resources");

            Map<String, String> files = new HashMap<>();

            if(Files.exists(root)) {
                Files.walkFileTree(root, new FileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String key = root.relativize(file).normalize().toString();
                        String value = Files.readString(file);
                        files.put(key, value);
                        GreenfootFileLookup.set(key, value);
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
            }

            if(GreenfootSession.REAL != GreenfootSession.APPLICATION) {
                Console.debug("Skipping file indexing: Not in greenfoot.Greenfoot application");
                return;
            }

            Class.forName(GreenfootFileLoader.class.getName());

            new Thread(() -> {

                StringBuilder loaderFile = new StringBuilder();
                loaderFile.append("package com.github.rccookie.engine2d.impl.greenfoot.sessionstorage;\n");
                loaderFile.append("import static com.github.rccookie.engine2d.impl.greenfoot.GreenfootBase64.decode;\n");
                loaderFile.append("import static com.github.rccookie.engine2d.impl.greenfoot.GreenfootFileLookup.set;\n");
                loaderFile.append("public final class GreenfootFileLoader {\n");
                loaderFile.append("    private GreenfootFileLoader() { throw new UnsupportedOperationException(); }\n");
                loaderFile.append("    public static void init() { }\n");
                loaderFile.append("    public static void load() {\n");

                Base64.Encoder encoder = Base64.getEncoder();

                files.forEach((f, d) -> {
                    loaderFile.append("        set(\"");
                    loaderFile.append(escape(f)).append("\", decode(\"");
                    loaderFile.append(new String(encoder.encode(d.getBytes())));
                    loaderFile.append("\"));\n");
                });

                loaderFile.append("    }\n}\n");

                String dir;
                if(maven) dir = "src/main/java/";
                else if(Files.exists(Path.of("src"))) dir = "src/";
                else dir = "";

                try {
                    Files.writeString(Path.of(dir + "com/github/rccookie/engine2d/impl/greenfoot/sessionstorage/GreenfootFileLoader.java"), loaderFile.toString());
                    Console.debug("Files indexed");
//                    Files.deleteIfExists(Path.of(dir + "com/github/rccookie/engine2d/impl/greenfoot/sessionstorage/GreenfootFileLoader.class"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, "File Indexer Thread").start();

        } catch(Exception e) {
            Console.error(e);
        }
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
}
