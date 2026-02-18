package org.msuo.data2java;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;

public final class GroovyDeserializer extends AbstractScriptDeserializer {

    @Override
    public <T> T deserialize(Path file, Charset charset, Class<T> targetClass)
        throws IOException {
        Path normalized = file.toAbsolutePath().normalize();
        String source = Files.readString(normalized, charset);
        return ObjectMapper.deserialize(
            parse(
                source,
                Collections.emptyMap(),
                Collections.emptyMap(),
                normalized
            ),
            targetClass
        );
    }

    @Override
    public <T> T deserialize(Path file, Class<T> targetClass) throws IOException {
        return deserialize(file, StandardCharsets.UTF_8, targetClass);
    }

    @Override
    public <T> T deserialize(File file, Class<T> targetClass) throws IOException {
        return deserialize(file.toPath(), targetClass);
    }

    @Override
    protected DataValue parse(
        String source,
        Map<String, String> environment,
        Map<String, Object> globals
    ) {
        return parse(source, environment, globals, null);
    }

    private DataValue parse(
        String source,
        Map<String, String> environment,
        Map<String, Object> globals,
        Path currentFile
    ) {
        try {
            EvalContext context = new EvalContext(environment, globals);
            Object root = evaluate(source, context, currentFile);
            return new GroovyDataValue(root);
        } catch (DataSourceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new DataSourceException("Groovy", "evaluate", e.getMessage(), e);
        }
    }

    private static Object evaluate(
        String source,
        EvalContext context,
        Path currentFile
    ) {
        if (currentFile != null) {
            context.fileStack.push(currentFile.toAbsolutePath().normalize());
        }
        try {
            Binding binding = new Binding();
            binding.setVariable(
                "ENV",
                EnvironmentValues.withSystemFallback(context.environment)
            );
            for (Map.Entry<String, Object> e : context.globals.entrySet()) {
                binding.setVariable(e.getKey(), e.getValue());
            }
            binding.setVariable(
                "load",
                new Closure<Object>(GroovyDeserializer.class) {
                    public Object doCall(Object loadPath) {
                        return loadScript(context, loadPath);
                    }
                }
            );
            return new GroovyShell(binding).evaluate(source);
        } finally {
            if (currentFile != null) {
                context.fileStack.pop();
            }
        }
    }

    private static Object loadScript(EvalContext context, Object loadPath) {
        if (!(loadPath instanceof String)) {
            throw new DataSourceException(
                "Groovy",
                "evaluate",
                "load(path) requires a string path",
                null
            );
        }
        Path resolved = resolveLoadPath(context, (String) loadPath);
        try {
            String nestedSource = Files.readString(resolved, StandardCharsets.UTF_8);
            return evaluate(nestedSource, context, resolved);
        } catch (IOException e) {
            throw new DataSourceException(
                "Groovy",
                "evaluate",
                "Failed to load '" + loadPath + "': " + e.getMessage(),
                e
            );
        }
    }

    private static Path resolveLoadPath(EvalContext context, String loadPath) {
        Path candidate = Paths.get(loadPath);
        if (!candidate.isAbsolute()) {
            Path base = context.currentDirectory();
            if (base != null) {
                candidate = base.resolve(candidate);
            } else {
                candidate = candidate.toAbsolutePath();
            }
        }
        return candidate.normalize();
    }

    private static final class EvalContext {
        private final Map<String, String> environment;
        private final Map<String, Object> globals;
        private final Deque<Path> fileStack = new ArrayDeque<>();

        EvalContext(
            Map<String, String> environment,
            Map<String, Object> globals
        ) {
            this.environment = environment;
            this.globals = globals;
        }

        Path currentDirectory() {
            if (fileStack.isEmpty()) return null;
            Path current = fileStack.peek();
            return current == null ? null : current.getParent();
        }
    }

    private static final class GroovyDataValue extends MapListDataValue {

        GroovyDataValue(Object value) {
            super(value);
        }

        @Override
        protected DataValue wrap(Object value) {
            return new GroovyDataValue(value);
        }
    }

}
