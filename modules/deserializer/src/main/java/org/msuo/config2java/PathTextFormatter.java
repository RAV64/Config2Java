package org.msuo.config2java;

final class PathTextFormatter {

    private PathTextFormatter() {}

    static String label(Path.Segment segment) {
        if (segment.kind == Path.SegmentKind.ROOT) return "$";
        if (segment.kind == Path.SegmentKind.FIELD) return String.valueOf(segment.value);
        if (segment.kind == Path.SegmentKind.INDEX) return "[" + String.valueOf(segment.value) + "]";
        if (segment.kind == Path.SegmentKind.MAP_KEY) return "[" + String.valueOf(segment.value) + "]";
        return "{" + String.valueOf(segment.value) + "}";
    }
}
