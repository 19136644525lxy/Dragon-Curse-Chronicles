package com.qituo.dcc.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionCache {

    private static final Map<String, Method> methodCache = new ConcurrentHashMap<>();
    private static final Map<String, Field> fieldCache = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();

    public static Class<?> getClass(String className) {
        try {
            return classCache.computeIfAbsent(className, name -> {
                try {
                    return Class.forName(name);
                } catch (ClassNotFoundException e) {
                    ExceptionHandler.handleReflectionException("获取类: " + name, e);
                    return null;
                }
            });
        } catch (Exception e) {
            ExceptionHandler.handleReflectionException("获取类: " + className, e);
            return null;
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        if (clazz == null) {
            return null;
        }

        String key = clazz.getName() + "." + methodName + getParameterTypesString(parameterTypes);
        try {
            return methodCache.computeIfAbsent(key, k -> {
                try {
                    return clazz.getMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException e) {
                    try {
                        Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                        method.setAccessible(true);
                        return method;
                    } catch (NoSuchMethodException ex) {
                        ExceptionHandler.handleReflectionException("获取方法: " + key, ex);
                        return null;
                    }
                }
            });
        } catch (Exception e) {
            ExceptionHandler.handleReflectionException("获取方法: " + key, e);
            return null;
        }
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        if (clazz == null) {
            return null;
        }

        String key = clazz.getName() + "." + fieldName;
        try {
            return fieldCache.computeIfAbsent(key, k -> {
                try {
                    Field field = clazz.getField(fieldName);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException e) {
                    try {
                        Field field = clazz.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        return field;
                    } catch (NoSuchFieldException ex) {
                        ExceptionHandler.handleReflectionException("获取字段: " + key, ex);
                        return null;
                    }
                }
            });
        } catch (Exception e) {
            ExceptionHandler.handleReflectionException("获取字段: " + key, e);
            return null;
        }
    }

    public static Object invokeMethod(Object obj, Method method, Object... args) {
        if (obj == null || method == null) {
            return null;
        }

        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            ExceptionHandler.handleReflectionException("执行方法: " + method.getName(), e);
            return null;
        }
    }

    public static Object getFieldValue(Object obj, Field field) {
        if (obj == null || field == null) {
            return null;
        }

        try {
            return field.get(obj);
        } catch (Exception e) {
            ExceptionHandler.handleReflectionException("获取字段值: " + field.getName(), e);
            return null;
        }
    }

    public static boolean setFieldValue(Object obj, Field field, Object value) {
        if (obj == null || field == null) {
            return false;
        }

        try {
            field.set(obj, value);
            return true;
        } catch (Exception e) {
            ExceptionHandler.handleReflectionException("设置字段值: " + field.getName(), e);
            return false;
        }
    }

    public static void clearCache() {
        methodCache.clear();
        fieldCache.clear();
        classCache.clear();
    }

    private static String getParameterTypesString(Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Class<?> type : parameterTypes) {
            sb.append(":").append(type.getName());
        }
        return sb.toString();
    }
}