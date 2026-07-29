package com.qituo.dcc.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExceptionHandler {

    private static final Logger LOGGER = LogManager.getLogger("TwelveTalismans");

    public static boolean handleException(String context, Exception e) {
        LOGGER.error("[十二符咒] 异常发生在: {}", context, e);
        return true;
    }

    public static <T> T handleExceptionWithDefault(String context, Exception e, T defaultValue) {
        handleException(context, e);
        return defaultValue;
    }

    public static boolean handleNetworkException(String context, Exception e) {
        LOGGER.error("[十二符咒] 网络异常发生在: {}", context, e);
        return true;
    }

    public static boolean handleReflectionException(String context, Exception e) {
        LOGGER.error("[十二符咒] 反射异常发生在: {}", context, e);
        return true;
    }

    public static boolean handleJsonException(String context, Exception e) {
        LOGGER.error("[十二符咒] JSON解析异常发生在: {}", context, e);
        return true;
    }

    public static boolean handleAIException(String context, Exception e) {
        LOGGER.error("[十二符咒] AI异常发生在: {}", context, e);
        return true;
    }
}