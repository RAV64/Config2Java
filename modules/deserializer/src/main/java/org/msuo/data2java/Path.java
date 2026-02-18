package org.msuo.data2java;

final class Path {

    private final Path parent;
    private final SegmentKind kind;
    private final Object value;
    private Path(Path parent, SegmentKind kind, Object value) {
        this.parent = parent;
        this.kind = kind;
        this.value = value;
    }

    static Path root() {
        return new Path(null, SegmentKind.ROOT, "$");
    }

    Path field(String name) {
        return new Path(this, SegmentKind.FIELD, name);
    }

    Path index(int i) {
        return new Path(this, SegmentKind.INDEX, i);
    }

    Path mapKey(Object key) {
        return new Path(this, SegmentKind.MAP_KEY, key);
    }

    Path rawKey(Object rawKey) {
        return new Path(this, SegmentKind.RAW_KEY, rawKey);
    }

    Segment[] segments() {
        int depth = 0;
        for (Path p = this; p != null; p = p.parent) {
            depth++;
        }

        Segment[] out = new Segment[depth];
        int i = depth - 1;
        for (Path p = this; p != null; p = p.parent) {
            out[i--] = new Segment(p.kind, p.value);
        }
        return out;
    }

    enum SegmentKind {
        ROOT,
        FIELD,
        INDEX,
        MAP_KEY,
        RAW_KEY,
    }

    static final class Segment {
        final SegmentKind kind;
        final Object value;

        Segment(SegmentKind kind, Object value) {
            this.kind = kind;
            this.value = value;
        }
    }
}
