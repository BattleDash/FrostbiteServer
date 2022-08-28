package me.battledash.kyber.util;

import com.google.common.reflect.ClassPath;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ClassUtils {

    public static List<Class<?>> findAllClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();

        ClassPath classPath;
        try {
            classPath = ClassPath.from(ClassLoader.getSystemClassLoader());
        } catch (IOException e) {
            log.error("Error while reading class names from package: {}", packageName, e);
            return classes;
        }

        for (ClassPath.ClassInfo classInfo : classPath.getAllClasses()) {
            if (classInfo.getPackageName().startsWith(packageName)) {
                classes.add(classInfo.load());
            }
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
