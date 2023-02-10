package com.jinxin.platform.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 日志操作
 *
 * @author zengd
 * @version 1.0
 * @date 2022/9/23 11:10
 */
public class LogUtils {
    public static void logLevel(int index) {
        Level[] levelArr = new Level[]{Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR};
        // #1.get logger context
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<Logger> loggerList = loggerContext.getLoggerList();
        // #2.filter the Logger object
        List<Logger> packageLoggerList = loggerList.stream().filter(a -> a.getName().startsWith("com.jinxin.platform")).collect(Collectors.toList());
        // #3.set level to logger
        for (ch.qos.logback.classic.Logger logger : packageLoggerList) {
            logger.setLevel(levelArr[index]);
        }
    }
}
