package com.novystxr.classysk.api.util;

import org.bukkit.Bukkit;
import org.bukkit.util.LoggerUtils;

import java.util.logging.Level;

public class Logger {

    private static final org.slf4j.Logger logger = Bukkit.getBetterLogger();
    private static final String prefix = "<GRAY>[<#83A4FF>ClassySK<GRAY>] <WHITE>";

    private static String buildMessage(Object... objects) {
        StringBuilder builder = new StringBuilder();

        builder.append(prefix);

        for (Object object : objects) {
            builder.append(object);
            builder.append(" ");
        }

        return builder.toString();
    }

    public static void log(Object... values) {
        LoggerUtils.log(logger, Level.INFO, buildMessage(values));
    }

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void severe(String msg) {
        logger.error(msg);
    }

    public static void warning(String msg) {
        logger.warn(msg);
    }
}
