package com.qituo.dcc.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射缓存工具类
 * 用于缓存反射操作的结果，减少反射开销
 */
public class ReflectionCache {
    
    private static final Map<String, Method> methodCache = new ConcurrentHashMap<>();
    private static final Map<String, Field> fieldCache = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
    
    /**
     * 获取类对象，使用缓存
     * @param className 类名
     * @return 类对象
     */
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
    
    /**
     * 获取方法，使用缓存
     * @param clazz 类对象
     * @param methodName 方法名
     * @param parameterTypes 参数类型
     * @return 方法对象
     */
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
    
    /**
     * 获取字段，使用缓存
     * @param clazz 类对象
     * @param fieldName 字段名
     * @return 字段对象
     */
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
    
    /**
     * 执行方法
     * @param obj 目标对象
     * @param method 方法对象
     * @param args 参数
     * @return 方法返回值
     */
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
    
    /**
     * 获取字段值
     * @param obj 目标对象
     * @param field 字段对象
     * @return 字段值
     */
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
    
    /**
     * 设置字段值
     * @param obj 目标对象
     * @param field 字段对象
     * @param value 新值
     * @return 是否成功
     */
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
    
    /**
     * 清除缓存
     */
    public static void clearCache() {
        methodCache.clear();
        fieldCache.clear();
        classCache.clear();
    }
    
    /**
     * 获取参数类型字符串
     * @param parameterTypes 参数类型数组
     * @return 参数类型字符串
     */
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