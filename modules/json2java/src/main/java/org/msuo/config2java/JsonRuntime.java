package org.msuo.config2java;

import com.fasterxml.jackson.databind.ObjectMapper;

final class JsonRuntime {

    static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonRuntime() {}
}
