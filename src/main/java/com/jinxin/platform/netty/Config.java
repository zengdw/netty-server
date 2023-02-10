package com.jinxin.platform.netty;


import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zengd
 * @version 1.0
 * @date 2022/9/21 14:05
 */
public class Config {
    public static Map<String, Channel> con = new ConcurrentHashMap<>();

    public static int getOnlineUserCount() {
        return con.size();
    }
}
