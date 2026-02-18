package org.msuo.data2java;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Deserializer {

    <T> T deserialize(String source, Class<T> targetClass);

    default <T> T deserialize(Path file, Charset charset, Class<T> targetClass)
        throws IOException {
        return deserialize(Files.readString(file, charset), targetClass);
    }

    default <T> T deserialize(Path file, Class<T> targetClass)
        throws IOException {
        return deserialize(file, StandardCharsets.UTF_8, targetClass);
    }

    default <T> T deserialize(File file, Class<T> targetClass)
        throws IOException {
        return deserialize(file.toPath(), targetClass);
    }
}
