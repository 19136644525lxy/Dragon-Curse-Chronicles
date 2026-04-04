package com.qituo.dcc.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 统一异常处理工具类
 * 用于处理和记录模组中的异常情况
 */
public class ExceptionHandler {
    
    private static final Logger LOGGER = LogManager.getLogger("TwelveTalismans");
    
    /**
     * 处理并记录异常
     * @param context 异常发生的上下文
     * @param e 异常对象
     * @return 是否成功处理异常
     */
    public static boolean handleException(String context, Exception e) {
        LOGGER.error("[十二符咒] 异常发生在: {}", context, e);
        return true;
    }
    
    /**
     * 处理并记录异常，返回默认值
     * @param <T> 返回类型
     * @param context 异常发生的上下文
     * @param e 异常对象
     * @param defaultValue 默认返回值
     * @return 异常处理后的返回值
     */
    public static <T> T handleExceptionWithDefault(String context, Exception e, T defaultValue) {
        handleException(context, e);
        return defaultValue;
    }
    
    /**
     * 处理并记录网络异常
     * @param context 异常发生的上下文
     * @param e 异常对象
     * @return 是否成功处理异常
     */
    public static boolean handleNetworkException(String context, Exception e) {
        LOGGER.error("[十二符咒] 网络异常发生在: {}", context, e);
        return true;
    }
    
    /**
     * 处理并记录反射异常
     * @param context 异常发生的上下文
     * @param e 异常对象
     * @return 是否成功处理异常
     */
    public static boolean handleReflectionException(String context, Exception e) {
        LOGGER.error("[十二符咒] 反射异常发生在: {}", context, e);
        return true;
    }
    
    /**
     * 处理并记录JSON解析异常
     * @param context 异常发生的上下文
     * @param e 异常对象
     * @return 是否成功处理异常
     */
    public static boolean handleJsonException(String context, Exception e) {
        LOGGER.error("[十二符咒] JSON解析异常发生在: {}", context, e);
        return true;
    }
    
    /**
     * 处理并记录AI异常
     * @param context 异常发生的上下文
     * @param e 异常对象
     * @return 是否成功处理异常
     */
    public static boolean handleAIException(String context, Exception e) {
        LOGGER.error("[十二符咒] AI异常发生在: {}", context, e);
        return true;
    }
}