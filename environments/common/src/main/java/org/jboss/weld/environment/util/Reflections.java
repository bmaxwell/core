/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.environment.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * Reflection utilities.
 *
 * @author Pete Muir
 * @author Martin Kouba
 */
public final class Reflections {

    private Reflections() {
    }

    public static boolean containsAnnotation(Class<?> javaClass, Class<? extends Annotation> requiredAnnotation) {
        for (Class<?> clazz = javaClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            // class level annotations
            if (clazz == javaClass || requiredAnnotation.isAnnotationPresent(Inherited.class)) {
                if (containsAnnotations(clazz.getAnnotations(), requiredAnnotation)) {
                    return true;
                }
            }
            // fields
            for (Field field : clazz.getDeclaredFields()) {
                if (containsAnnotations(field.getAnnotations(), requiredAnnotation)) {
                    return true;
                }
            }
            // constructors
            for (Constructor<?> constructor : clazz.getConstructors()) {
                if (containsAnnotations(constructor.getAnnotations(), requiredAnnotation)) {
                    return true;
                }
                for (Annotation[] parameterAnnotations : constructor.getParameterAnnotations()) {
                    if (containsAnnotations(parameterAnnotations, requiredAnnotation)) {
                        return true;
                    }
                }
            }
            // methods
            for (Method method : clazz.getDeclaredMethods()) {
                if (containsAnnotations(method.getAnnotations(), requiredAnnotation)) {
                    return true;
                }
                for (Annotation[] parameterAnnotations : method.getParameterAnnotations()) {
                    if (containsAnnotations(parameterAnnotations, requiredAnnotation)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    /**
     *
     * @param resourceLoader
     * @param className
     * @return <code>true</code> if a class with the given name can be loaded, <code>false</code> otherwise
     * @see #loadClass(ResourceLoader, String)
     */
    public static boolean isClassLoadable(ResourceLoader resourceLoader, String className) {
        return loadClass(resourceLoader, className) != null;
    }

    /**
     *
     * @param resourceLoader
     * @param className
     * @return the loaded class or null if the given class cannot be loaded
     * @see #classForName(ResourceLoader, String)
     */
    public static <T> Class<T> loadClass(ResourceLoader resourceLoader, String className) {
        try {
            return classForName(resourceLoader, className);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * First try to load a class using the specified ResourceLoader. If not successful, try {@link Class#forName(String)} as a fallback.
     *
     * @param resourceLoader
     * @param className
     * @return the loaded class
     */
    public static <T> Class<T> classForName(ResourceLoader resourceLoader, String className) {
        try {
            return cast(resourceLoader.classForName(className));
        } catch (Exception e) {
            CommonLogger.LOG.cannotLoadClassUsingResourceLoader(className);
            CommonLogger.LOG.catchingTrace(e);
        }
        try {
            return cast(Class.forName(className));
        } catch (Exception e) {
            throw CommonLogger.LOG.cannotLoadClass(className, e);
        }
    }

    /**
     *
     * @param annotations
     * @param metaAnnotationType
     * @return <code>true</code> if any of the annotations specified has the given meta annotation type specified, <code>false</code> otherwise
     */
    public static boolean hasBeanDefiningMetaAnnotationSpecified(Annotation[] annotations, Class<? extends Annotation> metaAnnotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(metaAnnotationType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAnnotations(Annotation[] annotations, Class<? extends Annotation> requiredAnnotation) {
        return containsAnnotation(annotations, requiredAnnotation, true);
    }

    private static boolean containsAnnotation(Annotation[] annotations, Class<? extends Annotation> requiredAnnotation, boolean checkMetaAnnotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (requiredAnnotation.equals(annotationType)) {
                return true;
            }
            if (checkMetaAnnotations && containsAnnotation(annotationType.getAnnotations(), requiredAnnotation, false)) {
                return true;
            }
        }
        return false;
    }
}