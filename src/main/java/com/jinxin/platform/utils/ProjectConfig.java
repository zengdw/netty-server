package com.jinxin.platform.utils;

import java.io.File;

/**
 * 项目配置工具类
 *
 * @author zengd
 * @version 1.0
 * @date 2022/9/19 10:50
 */
public class ProjectConfig {
    public static String getProjectHome() {
        return System.getProperty("user.dir");
    }

    public static String getProjectConfDir() {
        return getProjectHome() + File.separator + "config";
    }

}
