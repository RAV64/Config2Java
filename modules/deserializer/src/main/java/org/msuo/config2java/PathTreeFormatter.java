package org.msuo.config2java;

final class PathTreeFormatter {

    private PathTreeFormatter() {}

    static String format(ConfigDeserializationException.PathNode root) {
        StringBuilder sb = new StringBuilder();
        sb.append(labelWithErrors(root)).append('\n');
        java.util.List<ConfigDeserializationException.PathNode> top = root.getChildren();
        for (int i = 0; i < top.size(); i++) {
            appendNode(sb, top.get(i), "", i == top.size() - 1);
        }
        return sb.toString();
    }

    private static void appendNode(
        StringBuilder sb,
        ConfigDeserializationException.PathNode node,
        String prefix,
        boolean last
    ) {
        sb
            .append(prefix)
            .append(last ? "└─ " : "├─ ")
            .append(labelWithErrors(node))
            .append('\n');

        java.util.List<ConfigDeserializationException.PathNode> children = node.getChildren();
        String childPrefix = prefix + (last ? "   " : "|  ");
        for (int i = 0; i < children.size(); i++) {
            appendNode(sb, children.get(i), childPrefix, i == children.size() - 1);
        }
    }

    private static String labelWithErrors(ConfigDeserializationException.PathNode node) {
        java.util.List<ConfigDeserializationException.ConfigError> errors = node.getErrors();
        if (errors.isEmpty()) return node.getSegment();

        StringBuilder sb = new StringBuilder(node.getSegment()).append(" -> ");
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) sb.append("; ");
            ConfigDeserializationException.ConfigError e = errors.get(i);
            sb.append(e.getMessage());
        }
        return sb.toString();
    }
}
