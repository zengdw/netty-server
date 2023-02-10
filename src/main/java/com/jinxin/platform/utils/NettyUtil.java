package com.jinxin.platform.utils;

import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

/**
 * netty 工具类
 *
 * @author zengd
 * @version 1.0
 * @date 2022/10/10 15:52
 */
public class NettyUtil {
    public static String getRemoteIp(ChannelHandlerContext ctx) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        return inetSocketAddress.getAddress().getHostAddress();
    }
}
