package it.paspiz85.nanobot.scripting;

import it.paspiz85.nanobot.platform.Platform;
import it.paspiz85.nanobot.util.BuildInfo;
import it.paspiz85.nanobot.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

/**
 * Script Manager.
 *
 * @author paspiz85
 *
 */
public final class ScriptManager {

    private static ScriptManager instance;

    public static ScriptManager instance() {
        if (instance == null) {
            instance = new ScriptManager();
        }
        return instance;
    }

    private Consumer<String> alert;

    private Function<String, Boolean> confirm;

    private final Map<String, Path> embeddedScripts;

    private final ScriptEngineManager factory;

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private Function<String, String> prompt;

    private ScriptManager() {
        factory = new ScriptEngineManager();
        embeddedScripts = new TreeMap<>();
        try {
            Utils.withClasspathFolder(Utils.getParentResource(getClass(), "scripts").toURI(), (path) -> {
                ScriptManager.this.embeddedScripts.putAll(scanPath(path));
            });
        } catch (final URISyntaxException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    protected ScriptContext buildContext() {
        final SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("alert", alert, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("confirm", confirm, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("prompt", prompt, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("buildInfo", BuildInfo.instance(), ScriptContext.ENGINE_SCOPE);
        context.setAttribute("platform", Platform.instance(), ScriptContext.ENGINE_SCOPE);
        return context;
    }

    public Set<String> getScripts() {
        return getScriptsMap().keySet();
    }

    private Map<String, Path> getScriptsMap() {
        Map<String, Path> scripts = new TreeMap<>();
        final Path extScripts = Paths.get("scripts");
        if (extScripts.toFile().exists()) {
            scripts = scanPath(extScripts);
            scripts.putAll(embeddedScripts);
        } else {
            scripts = Collections.unmodifiableMap(embeddedScripts);
        }
        return scripts;
    }

    public void run(final String script) {
        final String msg = "Running script " + script;
        try {
            final Path path = getScriptsMap().get(script);
            if (path == null) {
                throw new IllegalArgumentException("Script not found");
            }
            logger.info(msg);
            final ScriptEngine engine = factory.getEngineByName("nashorn");
            final ScriptContext context = buildContext();
            try (InputStream in = path.toUri().toURL().openStream()) {
                engine.eval(new InputStreamReader(in), context);
            }
            logger.info(msg + " completed");
        } catch (IOException | ScriptException e) {
            logger.log(Level.SEVERE, msg + " failed: " + e.getMessage(), e);
        }
    }

    private Map<String, Path> scanPath(final Path path) {
        final Map<String, Path> scripts = new TreeMap<>();
        try (Stream<Path> walk = Files.walk(path, 1)) {
            for (final Iterator<Path> it = walk.iterator(); it.hasNext();) {
                final Path next = it.next();
                if (Files.isDirectory(next)) {
                    continue;
                }
                scripts.put(next.getFileName().toString(), next);
            }
        } catch (final IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return scripts;
    }

    public void setAlert(final Consumer<String> alert) {
        this.alert = alert;
    }

    public void setConfirm(final Function<String, Boolean> confirm) {
        this.confirm = confirm;
    }

    public void setPrompt(final Function<String, String> prompt) {
        this.prompt = prompt;
    }
}
