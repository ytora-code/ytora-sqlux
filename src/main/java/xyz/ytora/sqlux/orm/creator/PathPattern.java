package xyz.ytora.sqlux.orm.creator;

import java.util.regex.Pattern;

/**
 * 路径匹配模式
 *
 * @author ytora
 * @since 1.0
 */
public class PathPattern {

    final String rootPackage;

    final Pattern pattern;

    PathPattern(String path) {
        String normalized = normalize(path);
        this.rootPackage = rootPackage(normalized);
        this.pattern = Pattern.compile(toRegex(normalized));
    }

    boolean matches(String className) {
        return pattern.matcher(className).matches();
    }

    static String normalize(String path) {
        String normalized = path.replace('/', '.').replace('\\', '.');
        while (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    static String rootPackage(String path) {
        int wildcard = firstWildcard(path);
        String root = wildcard < 0 ? path : path.substring(0, wildcard);
        while (root.endsWith(".")) {
            root = root.substring(0, root.length() - 1);
        }
        return root;
    }

    static int firstWildcard(String path) {
        int star = path.indexOf('*');
        return star < 0 ? -1 : star;
    }

    static String toRegex(String path) {
        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '*') {
                if (i + 1 < path.length() && path.charAt(i + 1) == '*') {
                    regex.append(".*");
                    i++;
                } else {
                    regex.append("[^.]*");
                }
                continue;
            }
            if (".[]{}()+-^$?|".indexOf(c) >= 0) {
                regex.append('\\');
            }
            regex.append(c);
        }
        if (path.indexOf('*') < 0) {
            regex.append("(\\..*)?");
        }
        regex.append("$");
        return regex.toString();
    }

}
