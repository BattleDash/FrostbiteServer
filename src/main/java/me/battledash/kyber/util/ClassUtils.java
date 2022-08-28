package me.battledash.kyber.util;

import com.google.common.reflect.ClassPath;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

}
