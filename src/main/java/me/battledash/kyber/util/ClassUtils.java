package me.battledash.kyber.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ClassUtils {

    public static List<Class<?>> findAllClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(".class")) {
                    String className = line.substring(0, line.lastIndexOf('.'));
                    className = className.replaceAll("/", ".");
                    Class<?> clazz = getClass(className, packageName);
                    if (clazz != null) {
                        classes.add(clazz);
                    }
                } else {
                    String subPackageName = line.substring(0, line.lastIndexOf('/') == -1 ? line.length() : line.lastIndexOf('/'));
                    subPackageName = subPackageName.replaceAll("/", ".");
                    classes.addAll(findAllClasses(packageName + "." + subPackageName));
                }
            }
        } catch (Exception e) {
            log.error("Error while reading class names from package: {}", packageName, e);
        }
        return classes;
    }

    private static Class<?> getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.') == -1 ? className.length() : className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
