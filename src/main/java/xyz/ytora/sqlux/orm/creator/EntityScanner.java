package xyz.ytora.sqlux.orm.creator;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import xyz.ytora.sqlux.orm.AbsEntity;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 基于 ClassGraph 的实体扫描器。
 *
 * @author ytora
 * @since 1.0
 */
public final class EntityScanner {

    private EntityScanner() {
    }

    /**
     * 扫描指定路径下所有 {@link AbsEntity} 子类。
     *
     * <p>路径支持逗号、分号或空白分隔；支持 {@code *} 和 {@code **} 通配符。</p>
     *
     * @param entityPath 实体扫描路径，例如 {@code com.demo.entity.**}
     * @return 符合条件的实体类型列表
     */
    public static List<Class<? extends AbsEntity>> scan(String entityPath) {
        List<PathPattern> patterns = parsePatterns(entityPath);
        if (patterns.isEmpty()) {
            return new ArrayList<>();
        }

        ClassGraph classGraph = new ClassGraph().enableClassInfo().ignoreClassVisibility();
        List<String> roots = rootPackages(patterns);
        if (!roots.isEmpty()) {
            classGraph.acceptPackages(roots.toArray(new String[0]));
        }

        try (ScanResult scanResult = classGraph.scan()) {
            List<Class<?>> loaded = scanResult
                    .getSubclasses(AbsEntity.class.getName())
                    .loadClasses(true);
            return filter(loaded, patterns);
        }
    }

    private static List<Class<? extends AbsEntity>> filter(List<Class<?>> loaded,
                                                           List<PathPattern> patterns) {
        List<Class<? extends AbsEntity>> result = new ArrayList<>();
        for (Class<?> entityClass : loaded) {
            if (entityClass == null || entityClass.isInterface() || !AbsEntity.class.isAssignableFrom(entityClass)
                    || Modifier.isAbstract(entityClass.getModifiers())) {
                continue;
            }
            if (matches(entityClass.getName(), patterns)) {
                result.add(entityClass.asSubclass(AbsEntity.class));
            }
        }
        return result;
    }

    private static boolean matches(String className, List<PathPattern> patterns) {
        for (PathPattern pattern : patterns) {
            if (pattern.matches(className)) {
                return true;
            }
        }
        return false;
    }

    private static List<PathPattern> parsePatterns(String entityPath) {
        List<PathPattern> patterns = new ArrayList<>();
        if (entityPath == null || entityPath.trim().isEmpty()) {
            return patterns;
        }
        String[] parts = entityPath.split("[,;\\s]+");
        for (String part : parts) {
            String path = part.trim();
            if (!path.isEmpty()) {
                patterns.add(new PathPattern(path));
            }
        }
        return patterns;
    }

    private static List<String> rootPackages(List<PathPattern> patterns) {
        Set<String> roots = new LinkedHashSet<>();
        for (PathPattern pattern : patterns) {
            if (!pattern.rootPackage.isEmpty()) {
                roots.add(pattern.rootPackage);
            }
        }
        return new ArrayList<>(roots);
    }

}
